package doonv.entityselectortools;

import com.mojang.blaze3d.platform.InputConstants;
import doonv.entityselectortools.compat.AxiomCompat;
import doonv.entityselectortools.config.ClientConfig;
import doonv.entityselectortools.create.CreationHudRenderer;
import doonv.entityselectortools.create.CreationManager;
import doonv.entityselectortools.create.CreationRenderer;
import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.preview.CommandBlockManager;
import doonv.entityselectortools.preview.SelectorOverlayRenderer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

import static doonv.entityselectortools.EntitySelectorTools.LOGGER;

public class EntitySelectorToolsClient implements ClientModInitializer {
    public static final KeyMapping.Category CONTROLS_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(EntitySelectorTools.MOD_ID, "controls"));

    public static final KeyMapping COPY_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.%s.copyCreation".formatted(EntitySelectorTools.MOD_ID), InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C, EntitySelectorToolsClient.CONTROLS_CATEGORY));

    private volatile boolean serverHasMod = false;

    public static void systemMessage(LocalPlayer player, Component component) {
        //? if >=26.1 {
        player.sendSystemMessage(component);
        //?} else
        //player.displayClientMessage(component, false);
    }

    public static void overlayMessage(LocalPlayer player, Component component) {
        //? if >=26.1 {
        player.sendOverlayMessage(component);
        //?} else
        //player.displayClientMessage(component, true);
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.HANDLER.load();

        SelectorOverlayRenderer.register();
        CommandBlockManager.register();

        ClientPlayNetworking.registerGlobalReceiver(ServerSelectorPayload.TYPE,
                (payload, context) ->
                        context.client().execute(() -> SelectorOverlayRenderer.addServerSelector(payload.volume()))
        );

        ClientLoginNetworking.registerGlobalReceiver(EntitySelectorTools.HANDSHAKE_CHANNEL,
                (client, handler, buf, responseSender) -> {
                    serverHasMod = true;
                    return CompletableFuture.completedFuture(new FriendlyByteBuf(Unpooled.EMPTY_BUFFER));
                }
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CommandBlockManager.clear();
            serverHasMod = false;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.isLocalServer() || serverHasMod) return;

            if (client.player != null) {
                systemMessage(client.player, Component.translatable("entityselectortools.no-mod-on-server-warning"));
            }
        });

        if (ClientConfig.get().boxCreationWithAxiomTool) {
            AxiomCompat.registerBuilderTool();
        }

        CreationRenderer.register();
        CreationHudRenderer.register();
        CreationManager.register();
    }
}
