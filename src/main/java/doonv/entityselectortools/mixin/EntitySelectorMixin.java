package doonv.entityselectortools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.predicate.ConditionTreeWalker;
import doonv.entityselectortools.predicate.PredicateKeyHolder;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.minecraft.advancements.criterion.MinMaxBounds;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements PredicateKeyHolder {
    @Unique
    private static final MinMaxBounds.Bounds<Double> ANY = MinMaxBounds.Doubles.ANY.bounds();

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
            @Local(name = "aABB") @Nullable AABB absoluteAabb
    ) {
        double range = EntitySelectorTools.getMaxSelectorRange();
        if (this.predicateKey != null) {
            var lookup = sender.getServer().reloadableRegistries().lookup();
            lookup.get(this.predicateKey).ifPresent(holder -> {
                var condition = holder.value();
                List<AABB> boxes = ConditionTreeWalker.walk(condition);
                if (boxes.isEmpty()) return;

                List<EntitySelectorVolume> allVolumes = new ArrayList<>(boxes.size());
                for (AABB predicateBox : boxes) {
                    allVolumes.add(new EntitySelectorVolume(
                            predicateBox.getCenter(),
                            Optional.of(predicateBox),
                            ANY,
                            true,
                            sender.isSilent()
                    ));
                }

                for (ServerPlayer player : sender.getLevel().players()) {
                    if (!EntitySelectorTools.isModCapable(player)) continue;
                    for (EntitySelectorVolume vol : allVolumes) {
                        if (player.distanceToSqr(vol.center()) > (range * range)) continue;
                        EntitySelectorTools.addToBatch(player.getUUID(), vol);
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
        EntitySelectorVolume volume = new EntitySelectorVolume(
                center,
                box,
                Optional.ofNullable(this.range).map(MinMaxBounds.Doubles::bounds)
                        .orElse(ANY)
        );

        for (ServerPlayer player : sender.getLevel().players()) {
            if (player.distanceToSqr(center) > (range * range)) continue;
            if (EntitySelectorTools.isModCapable(player)) {
                EntitySelectorTools.addToBatch(player.getUUID(), volume);
            }
        }
    }

    @Override
    public void entitySelectorTools$setPredicateKey(ResourceKey<LootItemCondition> key) {
        this.predicateKey = key;
    }
}
