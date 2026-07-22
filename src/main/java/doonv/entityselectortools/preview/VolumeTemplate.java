package doonv.entityselectortools.preview;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record VolumeTemplate(
        Axis x, Axis y, Axis z,
        Optional<Vec3> boxDelta,
        MinMaxBounds.Bounds<Double> distance
) {
    public EntitySelectorVolume evaluate(Vec3 pos) {
        Vec3 origin = new Vec3(x.resolve(pos.x), y.resolve(pos.y), z.resolve(pos.z));
        Optional<AABB> aabb = boxDelta.map(d ->
                new AABB(origin.x, origin.y, origin.z, origin.x + d.x, origin.y + d.y, origin.z + d.z));
        return new EntitySelectorVolume(origin, aabb, distance, false, false);
    }

    public record Axis(boolean isRelative, boolean aligned, double value) {
        public double resolve(double contextValue) {
            double resolved = isRelative ? contextValue + value : value;
            return aligned ? Math.floor(resolved) : resolved;
        }
    }
}
