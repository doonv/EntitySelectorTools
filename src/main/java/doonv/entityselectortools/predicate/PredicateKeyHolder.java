package doonv.entityselectortools.predicate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface PredicateKeyHolder {
    void entitySelectorTools$setPredicateKey(ResourceKey<LootItemCondition> key);
}
