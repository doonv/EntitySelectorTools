package doonv.entityselectortools.network;

import doonv.entityselectortools.EntitySelectorTools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientVersionPayload(String version) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientVersionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    EntitySelectorTools.path("version"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientVersionPayload> CODEC = StreamCodec.of(
            (buf, val) -> buf.writeUtf(val.version(), 64),
            buf -> new ClientVersionPayload(buf.readUtf(64))
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
