package doonv.entityselectortools.creation;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.EntitySelectorToolsKeyMappings;
import doonv.entityselectortools.compat.AxiomCompat;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

/// Draws the very much original HUD with creation controls below the crosshair.
///
/// What? It's only a _tad_ inspired by Axiom.
public class CreationHudRenderer {
    private static final Identifier AXIOM_LEFT_MOUSE = Identifier.fromNamespaceAndPath("axiom", "mouse/left.png");

    private static final Identifier AXIOM_RIGHT_MOUSE = Identifier.fromNamespaceAndPath("axiom", "mouse/right.png");

    private static final Identifier AXIOM_MIDDLE_MOUSE = Identifier.fromNamespaceAndPath("axiom", "mouse/scroll.png");

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(EntitySelectorTools.MOD_ID, "creation_hud"),
                CreationHudRenderer::render
        );
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (AxiomCompat.isSelectorBuilderToolActive()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof LocalPlayer)) return;
        if (!CreationManager.inWandCreationMode(minecraft.player)) return;

        renderHints(guiGraphics);
    }

    private static @Nullable Identifier axiomIconFor(KeyMapping keyMapping) {
        InputConstants.Key boundKey = KeyBindingHelper.getBoundKeyOf(keyMapping);
        if (boundKey.getType() == InputConstants.Type.MOUSE) {
            return switch (boundKey.getValue()) {
                case 0 -> AXIOM_LEFT_MOUSE;
                case 1 -> AXIOM_RIGHT_MOUSE;
                case 2 -> AXIOM_MIDDLE_MOUSE;
                default -> null;
            };
        }
        return null;
    }

    private static void renderHintLine(GuiGraphics guiGraphics, Font font, int x, int y, Component label, KeyMapping keyMapping) {
        Identifier axiomIcon = axiomIconFor(keyMapping);
        if (axiomIcon != null && AxiomCompat.isAxiomLoaded()) {
            int width = font.width(label);
            //~ if >=26.1 'drawString' -> 'text'
            guiGraphics.drawString(font, label, x - width / 2, y, 0x80FFFFFF);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, axiomIcon, x - width / 2 - 16, y - 4, 0, 0, 16, 16, 16, 16,
                    0x80FFFFFF);
        } else {
            Component text = Component.literal("[")
                    .append(keyMessage(keyMapping))
                    .append("] ")
                    .append(label);
            //~ if >=26.1 'drawCenteredString' -> 'centeredText'
            guiGraphics.drawCenteredString(font, text, x, y, 0x80FFFFFF);
        }
    }

    public static void renderHints(GuiGraphics guiGraphics) {
        int x = guiGraphics.guiWidth() / 2;
        int y = (guiGraphics.guiHeight() / 2) + 13;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Options options = minecraft.options;

        if (CreationManager.pos1 == null) {
            renderHintLine(guiGraphics, font, x, y,
                    Component.translatable("entityselectortools.boxCreate.hud.pos1"),
                    options.keyAttack
            );
        } else if (CreationManager.pos2 == null) {
            renderHintLine(guiGraphics, font, x, y,
                    Component.translatable("entityselectortools.boxCreate.hud.pos2"),
                    options.keyUse);
        } else {
            renderHintLine(guiGraphics, font, x, y,
                    Component.translatable("entityselectortools.boxCreate.hud.extend"),
                    options.keyPickItem);
            y += 13;
            renderHintLine(guiGraphics, font, x, y,
                    Component.translatable("entityselectortools.boxCreate.hud.copyAsSelector"),
                    EntitySelectorToolsKeyMappings.COPY_AS_SELECTOR_KEY);
            y += 13;
            renderHintLine(guiGraphics, font, x, y,
                    Component.translatable("entityselectortools.boxCreate.hud.copyAsPredicateBounds"),
                    EntitySelectorToolsKeyMappings.COPY_AS_PREDICATE_KEY);
        }
    }

    private static String keyMessage(KeyMapping key) {
        return key.getTranslatedKeyMessage().getString().replace("Control",
                "Ctrl").replace(" + ", "+");
    }
}
