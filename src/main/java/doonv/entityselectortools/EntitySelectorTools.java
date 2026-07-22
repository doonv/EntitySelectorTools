package doonv.entityselectortools;

import doonv.entityselectortools.network.ClientVersionPayload;
import doonv.entityselectortools.network.LegacyDatapackSelectorPayload;
import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.preview.EntitySelectorVolume;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntitySelectorTools implements ModInitializer {
    public static final String MOD_ID = "entityselectortools";

    public static final Identifier HANDSHAKE_CHANNEL = EntitySelectorTools.path("handshake");

    private static final Map<UUID, SemanticVersion> PLAYER_MOD_VERSIONS = new HashMap<>();

    /// Batches all selectors at once to send at the end of a tick.
    private static final Map<UUID, List<EntitySelectorVolume>> TICK_BATCH = new HashMap<>();

    private static double maxSelectorRange = -1;

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier path(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isModCapable(ServerPlayer player) {
        return PLAYER_MOD_VERSIONS.containsKey(player.getUUID());
    }

    @Nullable
    public static SemanticVersion getModVersion(ServerPlayer player) {
        return PLAYER_MOD_VERSIONS.get(player.getUUID());
    }

    public static void addToBatch(UUID playerUuid, EntitySelectorVolume volume) {
        TICK_BATCH.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(volume);
    }

    public static double getMaxSelectorRange() {
        return maxSelectorRange;
    }

    @Override
    public void onInitialize() {
        //~ if >=26.1 'playS2C' -> 'clientboundPlay' {
        PayloadTypeRegistry.playS2C().register(ServerSelectorPayload.TYPE, ServerSelectorPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LegacyDatapackSelectorPayload.TYPE,
                LegacyDatapackSelectorPayload.CODEC);
        //~ }

        //~ if >=26.1 'playC2S' -> 'serverboundPlay'
        PayloadTypeRegistry.playC2S().register(ClientVersionPayload.TYPE, ClientVersionPayload.CODEC);

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
                sender.sendPacket(HANDSHAKE_CHANNEL, new FriendlyByteBuf(Unpooled.EMPTY_BUFFER))
        );

        // Required for .canSend() to work.
        ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL,
                (server, handler, understood, buf, synchronizer, responseSender) -> {
                });

        ServerPlayNetworking.registerGlobalReceiver(ClientVersionPayload.TYPE, (payload, context) -> {
            try {
                SemanticVersion version = SemanticVersion.parse(payload.version());
                LOGGER.info("'{}' joined with Entity Selector Tools {}", context.player().getGameProfile().name(),
                        version.getFriendlyString());
                PLAYER_MOD_VERSIONS.put(context.player().getUUID(), version);
            } catch (VersionParsingException e) {
                LOGGER.error("Failed to parse mod version from {} ({}): {}", context.player().getGameProfile().name(),
                        context.player().getStringUUID(), payload.version());
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player,
                    ServerSelectorPayload.TYPE) || ServerPlayNetworking.canSend(handler.player,
                    LegacyDatapackSelectorPayload.TYPE)) {
                PLAYER_MOD_VERSIONS.put(handler.player.getUUID(), null);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PLAYER_MOD_VERSIONS.remove(handler.player.getUUID());
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            maxSelectorRange = server.getPlayerList().getViewDistance() * 16.0;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                List<EntitySelectorVolume> volumes = TICK_BATCH.remove(player.getUUID());
                if (volumes == null || volumes.isEmpty()) continue;
                if (PLAYER_MOD_VERSIONS.get(player.getUUID()) == null) {
                    for (EntitySelectorVolume vol : volumes) {
                        ServerPlayNetworking.send(player, new LegacyDatapackSelectorPayload(vol));
                    }
                } else {
                    ServerPlayNetworking.send(player, new ServerSelectorPayload(volumes));
                }
            }
            TICK_BATCH.clear();
        });
    }
}
