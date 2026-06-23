package doonv.entityselectortools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/// Mixin that gets the entity selector volumes of datapacks and sends them to clients.
@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
    @Shadow
    @Final
    @Nullable
    private MinMaxBounds.Doubles range;

    @Inject(
            method = {"findEntities", "findPlayers"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/arguments/selector/EntitySelector;getPredicate(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/flag/FeatureFlagSet;)Ljava/util/function/Predicate;"
            )
    )
    private void onSpatialSearch(
            CommandSourceStack sender,
            CallbackInfoReturnable<?> cir,
            //~ if >=26.1 '"aABB"' -> '"absoluteAabb"'
            @Local(name = "absoluteAabb") @Nullable AABB absoluteAabb
    ) {
        if (!sender.isSilent()) {
            return;
        }

        Optional<AABB> box = Optional.ofNullable(absoluteAabb);

        if (this.range != null) {
            box = Optional.empty();
        }

        int viewDistanceChunks = sender.getServer().getPlayerList().getViewDistance();
        double range = viewDistanceChunks * 16.0;
        Vec3 center = box.map(AABB::getCenter).orElse(sender.getPosition());
        ServerSelectorPayload payload = new ServerSelectorPayload(
                new EntitySelectorVolume(
                        center,
                        box,
                        Optional.ofNullable(this.range).map(MinMaxBounds.Doubles::bounds)
                                .orElse(MinMaxBounds.Doubles.ANY.bounds())
                )
        );

        for (ServerPlayer player : sender.getLevel().players()) {
            if (player.distanceToSqr(center) > (range * range)) continue;

            if (ServerPlayNetworking.canSend(player, ServerSelectorPayload.TYPE)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
