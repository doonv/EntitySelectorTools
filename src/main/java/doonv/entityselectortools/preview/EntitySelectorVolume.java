package doonv.entityselectortools.preview;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record EntitySelectorVolume(Vec3 center, Optional<AABB> aabb, MinMaxBounds.Bounds<Double> distance,
                                   boolean fromPredicate, boolean isServer) {
    public EntitySelectorVolume(Vec3 center, Optional<AABB> aabb, MinMaxBounds.Bounds<Double> distance) {
        this(center, aabb, distance, false, true);
    }

    public EntitySelectorVolume(Vec3 center, Optional<AABB> aabb, MinMaxBounds.Bounds<Double> distance, boolean fromPredicate) {
        this(center, aabb, distance, fromPredicate, true);
    }
}
