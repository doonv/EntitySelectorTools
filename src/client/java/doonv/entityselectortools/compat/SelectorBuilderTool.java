package doonv.entityselectortools.compat;

import doonv.entityselectortools.create.CreationHudRenderer;
import doonv.entityselectortools.create.CreationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class SelectorBuilderTool implements InvocationHandler {

    private static final List<String> EMPTY_HINTS = List.of();

    private static Class<?> cachedPermissionClass;

    private static Object cachedToolPerm;

    private static boolean permissionLookupDone;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (args == null) args = new Object[0];

        return switch (method.getName()) {
            case "getName" -> "Create Entity Selector Volume";
            case "leftClick" -> {
                handleLeftClick(args.length > 0 ? (HitResult) args[0] : null);
                yield null;
            }
            case "rightClick" -> {
                handleRightClick(args.length > 0 ? (HitResult) args[0] : null);
                yield null;
            }
            case "middleClick" -> {
                handleMiddleClick(args.length > 0 ? (HitResult) args[0] : null);
                yield null;
            }
            case "renderWorld" -> null;
            case "renderScreen" -> {
                renderScreen(args[0], (int) args[1], (int) args[2], (float) args[3]);
                yield null;
            }
            case "scroll" -> false;
            case "shouldRenderBlockOutline" -> false;
            case "setPos1" -> {
                CreationManager.pos1 = (BlockPos) args[0];
                yield true;
            }
            case "setPos2" -> {
                CreationManager.pos2 = (BlockPos) args[0];
                yield true;
            }
            case "reset" -> {
                CreationManager.clear();
                yield null;
            }
            case "requiredPermissions" -> createToolPermissionSet();
            case "canBeReset" -> false;
            case "getKeyHints" -> EMPTY_HINTS;
            case "handleInput" -> null;
            case "getSelectionRestore" -> null;
            case "applySelectionRestore" -> null;
            default -> defaultReturn(method);
        };
    }

    private void handleLeftClick(HitResult hit) {
        if (!selectionAllowed()) return;
        CreationManager.pos1 = resolvePos();
    }

    private void handleRightClick(HitResult hit) {
        if (!selectionAllowed()) return;
        CreationManager.pos2 = resolvePos();
    }

    private void handleMiddleClick(HitResult hit) {
        if (!selectionAllowed()) return;
        CreationManager.expandTo(resolvePos());
    }

    private BlockPos resolvePos() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return CreationManager.getTargetPos(player);
        }
        return BlockPos.ZERO;
    }

    private boolean selectionAllowed() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && CreationManager.canCreate(player);
    }

    @SuppressWarnings("unused")
    private void renderScreen(Object guiGraphicsObj, int screenWidth, int screenHeight, float partialTick) {
        CreationHudRenderer.renderHints((GuiGraphicsExtractor) guiGraphicsObj);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object createToolPermissionSet() {
        if (!permissionLookupDone) {
            permissionLookupDone = true;
            try {
                cachedPermissionClass = Class.forName("com.moulberry.axiom.restrictions.AxiomPermission");
                cachedToolPerm = cachedPermissionClass.getField("TOOL").get(null);
            } catch (Exception ignored) {
                cachedToolPerm = null;
            }
        }
        if (cachedToolPerm != null) {
            return java.util.EnumSet.of((Enum) cachedToolPerm);
        }
        if (cachedPermissionClass != null && cachedPermissionClass.isEnum()) {
            return java.util.EnumSet.noneOf((Class) cachedPermissionClass);
        }
        return null;
    }

    private Object defaultReturn(Method method) {
        Class<?> ret = method.getReturnType();
        if (ret == boolean.class) return false;
        if (ret == int.class) return 0;
        if (ret == long.class) return 0L;
        if (ret == float.class) return 0.0f;
        if (ret == double.class) return 0.0;
        return null;
    }
}
