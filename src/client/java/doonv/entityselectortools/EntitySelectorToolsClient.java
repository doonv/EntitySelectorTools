package doonv.entityselectortools;

import doonv.entityselectortools.config.ClientConfig;
import doonv.entityselectortools.creation.CreationHudRenderer;
import doonv.entityselectortools.creation.CreationManager;
import doonv.entityselectortools.creation.CreationRenderer;
import doonv.entityselectortools.network.ServerSelectorPayload;
import doonv.entityselectortools.preview.CommandBlockManager;
import doonv.entityselectortools.preview.SelectorOverlayRenderer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class EntitySelectorToolsClient implements ClientModInitializer {
    public static final KeyMapping.Category CONTROLS_CATEGORY = KeyMapping.Category.register(
            EntitySelectorTools.path("controls"));

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
        if (!MissingDepsScreen.YACL_LOADED || !MissingDepsScreen.AMECS_LOADED) {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof TitleScreen)
                    client.setScreenAndShow(
                            new MissingDepsScreen(Component.translatable("entityselectortools.missingDeps.title")));
            });
            return; // Cancel rest of mod load to prevent crash
        }
        EntitySelectorToolsKeyMappings.register();
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
            if (!ClientConfig.get().showModMissing || client.isLocalServer() || serverHasMod) return;

            if (client.player != null) {
                systemMessage(client.player, Component.translatable("entityselectortools.noModOnServerWarning"));
            }
        });

        CreationRenderer.register();
        CreationHudRenderer.register();
        CreationManager.register();
    }
}
