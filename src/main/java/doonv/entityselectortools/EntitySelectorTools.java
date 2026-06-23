package doonv.entityselectortools;

import doonv.entityselectortools.network.ServerSelectorPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySelectorTools implements ModInitializer {
    public static final String MOD_ID = "entityselectortools";

    public static final Identifier HANDSHAKE_CHANNEL = path("handshake");

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier path(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        //~ if >=26.1 'playS2C' -> 'clientboundPlay'
        PayloadTypeRegistry.clientboundPlay().register(ServerSelectorPayload.TYPE, ServerSelectorPayload.CODEC);

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
                sender.sendPacket(HANDSHAKE_CHANNEL, new FriendlyByteBuf(Unpooled.EMPTY_BUFFER))
        );

        // Required for .canSend() to work.
        ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL,
                (server, handler, understood, buf, synchronizer, responseSender) -> {
                });
    }
}
