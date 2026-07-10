package doonv.entityselectortools.compat;

import com.moulberry.axiom.buildertools.BuilderTool;
//? if >=1.21.11 {
import com.moulberry.axiom.buildertools.BuilderToolSelectionState;
//?}
import com.moulberry.axiom.restrictions.AxiomPermission;
import com.moulberry.axiom.render.AxiomWorldRenderContext;
import doonv.entityselectortools.creation.CreationHudRenderer;
import doonv.entityselectortools.creation.CreationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;

import java.util.EnumSet;
import java.util.List;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}

public class SelectorBuilderTool implements BuilderTool {
    private static final List<String> EMPTY_HINTS = List.of();

    @Override
    public String getName() {
        return "Create Entity Selector Volume";
    }

    @Override
    public void leftClick(HitResult hit) {
        CreationManager.pos1 = resolvePos();
    }

    @Override
    public void rightClick(HitResult hit) {
        CreationManager.pos2 = resolvePos();
    }

    @Override
    public void middleClick(HitResult hit) {
        CreationManager.expandTo(resolvePos());
    }

    @Override
    public void renderScreen(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight, float partialTick) {
        CreationHudRenderer.renderHints(guiGraphics);
    }

    @Override
    public void renderWorld(AxiomWorldRenderContext rc) {}

    @Override
    public boolean scroll(int amount) {
        return false;
    }

    @Override
    public boolean shouldRenderBlockOutline(BlockPos pos) {
        return false;
    }

    @Override
    public boolean setPos1(BlockPos position) {
        CreationManager.pos1 = position;
        return true;
    }

    @Override
    public boolean setPos2(BlockPos position) {
        CreationManager.pos2 = position;
        return true;
    }

    @Override
    public void reset(boolean fullReset) {
        CreationManager.clear();
    }

    @Override
    public EnumSet<AxiomPermission> requiredPermissions() {
        return EnumSet.of(AxiomPermission.TOOL);
    }

    @Override
    public boolean canBeReset() {
        return false;
    }

    @Override
    public List<String> getKeyHints() {
        return EMPTY_HINTS; // We render our own hints
    }

    // handleInput, getSelectionRestore, applySelectionRestore only exist on >=1.21.11
    //? if >=1.21.11 {
    @Override
    public void handleInput(boolean leftPressed, boolean rightPressed, boolean middlePressed) {
    }
    @Override
    public BuilderToolSelectionState.Restore getSelectionRestore() {
        return null;
    }

    @Override
    public void applySelectionRestore(BuilderToolSelectionState.Restore restore) {
    }

    //? }

    private BlockPos resolvePos() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return CreationManager.getTargetPos(player);
        }
        return BlockPos.ZERO;
    }
}
