package doonv.entityselectortools.mixin.accessor;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CompositeLootItemCondition.class)
public interface CompositeLootItemConditionAccessor {
    @Accessor("terms")
    List<LootItemCondition> getTerms();
}
