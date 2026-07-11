package doonv.entityselectortools.compat;

import com.moulberry.axiom.buildertools.BuilderToolManager;
import com.moulberry.axiom.editor.keybinds.Keybinds;
import doonv.entityselectortools.mixin.client.AxiomKeybindAccessor;
import net.fabricmc.loader.api.FabricLoader;

public class AxiomCompat {
    private static final boolean IS_AXIOM_LOADED = FabricLoader.getInstance().isModLoaded("axiom");

    public static boolean axiomBuilderToolRegistered = false;

    public static boolean isAxiomLoaded() {
        return IS_AXIOM_LOADED;
    }

    public static boolean isSelectorBuilderToolActive() {
        if (!isAxiomLoaded() || !BuilderToolManager.isToolSlotActive()) return false;

        // todo: this relies on SelectorBuilderTool being the last tool
        return BuilderToolManager.getToolSlotSelected() == BuilderToolManager.getToolCount() - 1;
    }

    /// Prevents Axiom from copying the block the player is looking at when
    /// we use our own <kbd>Ctrl+C</kbd> copy keybind
    public static void suppressAxiomCopy() {
        ((AxiomKeybindAccessor) Keybinds.COPY_INGAME).setIngameDownLastTime(true);
    }
}
