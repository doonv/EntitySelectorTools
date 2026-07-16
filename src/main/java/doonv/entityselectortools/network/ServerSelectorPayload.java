package doonv.entityselectortools.network;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ServerSelectorPayload(List<EntitySelectorVolume> volumes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerSelectorPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    EntitySelectorTools.path("selector_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSelectorPayload> CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeVarInt(val.volumes().size());
                for (EntitySelectorVolume v : val.volumes()) {
                    EntitySelectorVolume.encode(buf, v);
                }
            },
            buf -> {
                int count = buf.readVarInt();
                List<EntitySelectorVolume> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(EntitySelectorVolume.decode(buf));
                }
                return new ServerSelectorPayload(list);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
