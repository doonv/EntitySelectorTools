package doonv.entityselectortools.mixin;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.predicate.ConditionTreeWalker;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
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
            Holder<LootItemCondition> predicate,
            CallbackInfoReturnable<Boolean> cir
    ) {
        List<AABB> boxes = ConditionTreeWalker.walk(predicate.value());
        if (boxes.isEmpty()) return;

        double range = EntitySelectorTools.getMaxSelectorRange();

        List<EntitySelectorVolume> allVolumes = new ArrayList<>(boxes.size());
        for (AABB predicateBox : boxes) {
            allVolumes.add(new EntitySelectorVolume(
                    predicateBox.getCenter(),
                    Optional.of(predicateBox),
                    MinMaxBounds.Doubles.ANY.bounds(),
                    true,
                    source.isSilent()
            ));
        }

        for (ServerPlayer player : source.getLevel().players()) {
            if (!EntitySelectorTools.isModCapable(player)) continue;
            for (EntitySelectorVolume vol : allVolumes) {
                if (player.distanceToSqr(vol.center()) > (range * range)) continue;
                EntitySelectorTools.addToBatch(player.getUUID(), vol);
            }
        }
    }
}
