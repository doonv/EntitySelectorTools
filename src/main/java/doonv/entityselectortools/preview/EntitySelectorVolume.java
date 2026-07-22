package doonv.entityselectortools.preview;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    private static final int FROM_PREDICATE = 1;

    private static final int IS_SERVER = 2;

    public static void encode(RegistryFriendlyByteBuf buf, EntitySelectorVolume vol) {
        buf.writeDouble(vol.center().x());
        buf.writeDouble(vol.center().y());
        buf.writeDouble(vol.center().z());
        vol.aabb().ifPresentOrElse(aabb -> {
            buf.writeDouble(aabb.minX);
            buf.writeDouble(aabb.minY);
            buf.writeDouble(aabb.minZ);
            buf.writeDouble(aabb.maxX);
            buf.writeDouble(aabb.maxY);
            buf.writeDouble(aabb.maxZ);
        }, () -> {
            for (int i = 0; i < 6; i++) buf.writeDouble(Double.NaN);
        });
        vol.distance().min().ifPresentOrElse(buf::writeDouble, () -> buf.writeDouble(Double.NaN));
        vol.distance().max().ifPresentOrElse(buf::writeDouble, () -> buf.writeDouble(Double.NaN));
        byte flags = (byte) ((vol.fromPredicate() ? FROM_PREDICATE : 0) | (vol.isServer() ? IS_SERVER : 0));
        buf.writeByte(flags);
    }

    public static EntitySelectorVolume decode(RegistryFriendlyByteBuf buf) {
        Vec3 center = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Optional<AABB> aabb = readOptionalAABB(buf);
        MinMaxBounds.Bounds<Double> distance = new MinMaxBounds.Bounds<>(
                readOptionalDouble(buf), readOptionalDouble(buf));
        byte flags = buf.readByte();
        return new EntitySelectorVolume(
                center, aabb, distance,
                (flags & FROM_PREDICATE) != 0,
                (flags & IS_SERVER) != 0
        );
    }

    private static Optional<AABB> readOptionalAABB(ByteBuf buf) {
        double x1 = buf.readDouble();
        double y1 = buf.readDouble();
        double z1 = buf.readDouble();
        double x2 = buf.readDouble();
        double y2 = buf.readDouble();
        double z2 = buf.readDouble();

        if (Double.isNaN(x1)) {
            return Optional.empty();
        } else {
            return Optional.of(new AABB(x1, y1, z1, x2, y2, z2));
        }
    }

    private static Optional<Double> readOptionalDouble(RegistryFriendlyByteBuf buf) {
        double v = buf.readDouble();
        return Double.isNaN(v) ? Optional.empty() : Optional.of(v);
    }
}
