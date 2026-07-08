package doonv.entityselectortools.create;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.EntitySelectorToolsClient;
import doonv.entityselectortools.compat.AxiomCompat;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/// Draws the very much original HUD with creation controls below the crosshair.
///
/// What? It's only a _tad_ inspired by Axiom.
public class CreationHudRenderer {
    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(EntitySelectorTools.MOD_ID, "creation_hud"),
                CreationHudRenderer::render
        );
    }

    private static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (AxiomCompat.isSelectorBuilderToolActive()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof LocalPlayer)) return;
        if (!CreationManager.inWandCreationMode(minecraft.player)) return;

        renderHints(guiGraphics);
    }

    public static void renderHints(GuiGraphicsExtractor guiGraphics) {
        final int color = 0x88FFFFFF;

        int x = guiGraphics.guiWidth() / 2;
        int y = (guiGraphics.guiHeight() / 2) + 15;

        //~ if >=26.1 'drawCenteredString' -> 'centeredText' {
        if (CreationManager.pos1 == null) {
            guiGraphics.centeredText(Minecraft.getInstance().font,
                    Component.translatable("entityselectortools.boxCreate.hud.pos1",
                            Component.keybind("key.attack")), x,
                    y, color);
        } else if (CreationManager.pos2 == null) {
            guiGraphics.centeredText(Minecraft.getInstance().font,
                    Component.translatable("entityselectortools.boxCreate.hud.pos2", Component.keybind("key.use")),
                    x,
                    y, color);
        } else {
            guiGraphics.centeredText(Minecraft.getInstance().font,
                    Component.translatable("entityselectortools.boxCreate.hud.extend",
                            Component.keybind("key.pickItem")), x,
                    y, color);
            y += 10;
            guiGraphics.centeredText(Minecraft.getInstance().font,
                    Component.translatable("entityselectortools.boxCreate.hud.copyAsSelector",
                            keyMessage(EntitySelectorToolsClient.COPY_AS_SELECTOR_KEY)), x, y, color);
            y += 10;
            guiGraphics.centeredText(Minecraft.getInstance().font,
                    Component.translatable("entityselectortools.boxCreate.hud.copyAsPredicate",
                            keyMessage(EntitySelectorToolsClient.COPY_AS_PREDICATE_KEY)), x, y, color);
        }
        //~}
    }

    private static String keyMessage(KeyMapping key) {
        return key.getTranslatedKeyMessage().getString().replace("Control",
                "Ctrl").replace(" + ", "+");
    }
}
