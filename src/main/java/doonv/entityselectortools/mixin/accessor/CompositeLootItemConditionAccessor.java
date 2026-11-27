package doonv.entityselectortools.mixin.accessor;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//~ if >=26.3 'java.util.List' -> 'net.minecraft.core.HolderSet'
import net.minecraft.core.HolderSet;

@Mixin(CompositeLootItemCondition.class)
public interface CompositeLootItemConditionAccessor {
    @Accessor("terms")
    //~ if >=26.3 'List' -> 'HolderSet'
    HolderSet<LootItemCondition> getTerms();
}
