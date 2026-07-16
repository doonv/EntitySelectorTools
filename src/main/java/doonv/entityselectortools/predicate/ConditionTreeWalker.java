package doonv.entityselectortools.predicate;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.mixin.accessor.CompositeLootItemConditionAccessor;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConditionTreeWalker {
    private static final ClassValue<Method[]> POSITION_GETTERS = new ClassValue<>() {
        @Override
        protected Method[] computeValue(Class<?> type) {
            try {
                Method[] methods = new Method[]{
                        type.getMethod("x"),
                        type.getMethod("y"),
                        type.getMethod("z")
                };
                for (Method m : methods) {
                    m.setAccessible(true);
                }
                return methods;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static List<AABB> walk(LootItemCondition condition) {
        List<AABB> boxes = new ArrayList<>();
        walk(condition, boxes);
        return boxes;
    }

    private static void walk(LootItemCondition condition, List<AABB> boxes) {
        if (condition instanceof LocationCheck loc) {
            extract(loc, boxes);
        } else if (condition instanceof CompositeLootItemCondition comp) {
            for (LootItemCondition term : ((CompositeLootItemConditionAccessor) comp).getTerms()) {
                walk(term, boxes);
            }
        }
    }

    private static void extract(LocationCheck loc, List<AABB> boxes) {
        loc.predicate().ifPresent(lp -> {
            try {
                Optional<?> pos = lp.position();
                if (pos.isEmpty()) return;
                Method[] getters = POSITION_GETTERS.get(pos.get().getClass());
                MinMaxBounds.Doubles x = (MinMaxBounds.Doubles) getters[0].invoke(pos.get());
                MinMaxBounds.Doubles y = (MinMaxBounds.Doubles) getters[1].invoke(pos.get());
                MinMaxBounds.Doubles z = (MinMaxBounds.Doubles) getters[2].invoke(pos.get());

                if (x.min().isPresent() && x.max().isPresent()
                        && y.min().isPresent() && y.max().isPresent()
                        && z.min().isPresent() && z.max().isPresent()) {
                    BlockPos offset = loc.offset();
                    boxes.add(new AABB(
                            x.min().get() + offset.getX(),
                            y.min().get() + offset.getY(),
                            z.min().get() + offset.getZ(),
                            x.max().get() + offset.getX(),
                            y.max().get() + offset.getY(),
                            z.max().get() + offset.getZ()
                    ));
                }
            } catch (Exception e) {
                EntitySelectorTools.LOGGER.warn("Failed to extract position bounds from LocationCheck", e);
            }
        });
    }
}
