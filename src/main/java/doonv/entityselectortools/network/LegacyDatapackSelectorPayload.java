package doonv.entityselectortools.network;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record LegacyDatapackSelectorPayload(EntitySelectorVolume volume) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LegacyDatapackSelectorPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    EntitySelectorTools.path("datapack_selector"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LegacyDatapackSelectorPayload> CODEC = StreamCodec.composite(
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
            }, buf -> new EntitySelectorVolume(
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    readOptionalAABB(buf),
                    new MinMaxBounds.Bounds<>(readOptionalDouble(buf), readOptionalDouble(buf))
            )),
            LegacyDatapackSelectorPayload::volume,
            LegacyDatapackSelectorPayload::new
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
