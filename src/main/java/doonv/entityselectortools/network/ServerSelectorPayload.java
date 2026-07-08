package doonv.entityselectortools.network;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ServerSelectorPayload(EntitySelectorVolume volume) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerSelectorPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(EntitySelectorTools.MOD_ID, "datapack_selector"));

    private static final int FROM_PREDICATE = 1;

    private static final int IS_SERVER = 2;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSelectorPayload> CODEC = StreamCodec.composite(
            StreamCodec.of((buf, val) -> {
                buf.writeDouble(val.center().x());
                buf.writeDouble(val.center().y());
                buf.writeDouble(val.center().z());
                val.aabb().ifPresentOrElse(aabb -> {
                    buf.writeDouble(aabb.minX);
                    buf.writeDouble(aabb.minY);
                    buf.writeDouble(aabb.minZ);
                    buf.writeDouble(aabb.maxX);
                    buf.writeDouble(aabb.maxY);
                    buf.writeDouble(aabb.maxZ);
                }, () -> {
                    for (int i = 0; i < 6; i++) buf.writeDouble(Double.NaN);
                });
                val.distance().min().ifPresentOrElse(buf::writeDouble, () -> buf.writeDouble(Double.NaN));
                val.distance().max().ifPresentOrElse(buf::writeDouble, () -> buf.writeDouble(Double.NaN));
                byte flags = (byte) ((val.fromPredicate() ? FROM_PREDICATE : 0) | (val.isServer() ? IS_SERVER : 0));
                buf.writeByte(flags);
            }, buf -> {
                Vec3 center = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
                Optional<AABB> aabb = readOptionalAABB(buf);
                MinMaxBounds.Bounds<Double> distance = new MinMaxBounds.Bounds<>(readOptionalDouble(buf),
                        readOptionalDouble(buf));
                byte flags = buf.isReadable() ? buf.readByte() : 0; // fallback for backwards compatibility
                return new EntitySelectorVolume(
                        center, aabb, distance,
                        (flags & FROM_PREDICATE) != 0,
                        (flags & IS_SERVER) != 0
                );
            }),
            ServerSelectorPayload::volume,
            ServerSelectorPayload::new
    );

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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
