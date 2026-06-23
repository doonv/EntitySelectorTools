package doonv.entityselectortools.mixin.client;

import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Intercepts Axiom's `VersionUtilsClient.blit256` for two things:
/// 1. Icon override at index 7 (spritesheet has only 7 icons)
/// 2. Panel extension for >7 tools (our extension texture provides extra slot)
@Mixin(targets = "com.moulberry.axiom.VersionUtilsClient")
public class AxiomToolIconOverrideMixin {

    private static final Identifier CREATION_TOOL_ICON = EntitySelectorTools.path("gui/creation_tool_icon.png");

    private static final Identifier PANEL_EXTENSION = EntitySelectorTools.path("gui/tool_swapper_extension.png");

    private static final Identifier AXIOM_TOOL_SWAPPER = Identifier.fromNamespaceAndPath(
            "axiom", "gui/tool_swapper.png");

    @Inject(method = "blit256", at = @At("HEAD"), cancellable = true)
    private static void onBlit256(GuiGraphicsExtractor guiGraphics, Identifier resourceLocation,
                                  int x, int y, int u, int v, int w, int h,
                                  CallbackInfo ci) {
        if (!ClientConfig.get().boxCreationWithAxiomTool) return;

        if (u == 0 && v == 0 && w == 22 && h > 142 && AXIOM_TOOL_SWAPPER.equals(resourceLocation)) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation,
                    x, y, 0.0F, 0.0F, 22, 141, 22, 141, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_EXTENSION,
                    x, y + 141, 0.0F, 0.0F, 22, 21, 22, 21, 22, 21);
            ci.cancel();
            return;
        }
        if (u == 158 && v == 0 && w == 16 && h == 16) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CREATION_TOOL_ICON,
                    x, y, 0.0F, 0.0F, w, h, w, h, 16, 16);
            ci.cancel();
        }
    }
}
