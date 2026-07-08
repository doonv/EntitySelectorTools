package doonv.entityselectortools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.predicate.ConditionTreeWalker;
import doonv.entityselectortools.predicate.PredicateKeyHolder;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements PredicateKeyHolder {
    @Shadow
    @Final
    @Nullable
    private MinMaxBounds.Doubles range;

    @Unique
    private ResourceKey<LootItemCondition> predicateKey;

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
        int viewDistanceChunks = sender.getServer().getPlayerList().getViewDistance();
        double range = viewDistanceChunks * 16.0;
        if (this.predicateKey != null) {
            var lookup = sender.getServer().reloadableRegistries().lookup();
            lookup.get(this.predicateKey).ifPresent(holder -> {
                var condition = holder.value();
                List<AABB> boxes = ConditionTreeWalker.walk(condition);
                for (AABB predicateBox : boxes) {
                    Vec3 predicateCenter = predicateBox.getCenter();
                    ServerSelectorPayload predicatePayload = new ServerSelectorPayload(
                            new EntitySelectorVolume(
                                    predicateCenter,
                                    Optional.of(predicateBox),
                                    MinMaxBounds.Doubles.ANY.bounds(),
                                    true,
                                    sender.isSilent()
                            )
                    );
                    for (ServerPlayer player : sender.getLevel().players()) {
                        if (player.distanceToSqr(predicateCenter) > (range * range)) continue;
                        if (ServerPlayNetworking.canSend(player, ServerSelectorPayload.TYPE)) {
                            ServerPlayNetworking.send(player, predicatePayload);
                        }
                    }
                }
            });
        }

        // Jank method of filtering out command blocks
        if (!sender.isSilent()) {
            return;
        }

        Optional<AABB> box = Optional.ofNullable(absoluteAabb);

        if (this.range != null) {
            box = Optional.empty();
        }

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

    @Override
    public void entitySelectorTools$setPredicateKey(ResourceKey<LootItemCondition> key) {
        this.predicateKey = key;
    }
}
