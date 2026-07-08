package doonv.entityselectortools.mixin;

import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.predicate.ConditionTreeWalker;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {
    @Inject(
            method = "checkCustomPredicate",
            at = @At("HEAD")
    )
    private static void onCheckCustomPredicate(
            CommandSourceStack source,
            Holder<LootItemCondition> condition,
            CallbackInfoReturnable<Boolean> cir
    ) {
        List<AABB> boxes = ConditionTreeWalker.walk(condition.value());
        if (boxes.isEmpty()) return;

        int viewDistanceChunks = source.getServer().getPlayerList().getViewDistance();
        double range = viewDistanceChunks * 16.0;

        for (AABB box : boxes) {
            Vec3 center = box.getCenter();
            ServerSelectorPayload payload = new ServerSelectorPayload(
                    new EntitySelectorVolume(
                            center,
                            Optional.of(box),
                            MinMaxBounds.Doubles.ANY.bounds(),
                            true,
                            source.isSilent()
                    )
            );

            for (ServerPlayer player : source.getLevel().players()) {
                if (player.distanceToSqr(center) > (range * range)) continue;

                if (ServerPlayNetworking.canSend(player, ServerSelectorPayload.TYPE)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        }
    }
}
