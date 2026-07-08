package doonv.entityselectortools.compat;

import doonv.entityselectortools.EntitySelectorTools;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import sun.misc.Unsafe;

import java.util.List;

/// Handles compatibility with Axiom without creating a hard dependency.
public class AxiomCompat {
    private static final boolean IS_AXIOM_LOADED = FabricLoader.getInstance().isModLoaded("axiom");

    private static boolean builderInitFailed = false;

    private static Method isToolSlotActiveMethod = null;

    private static boolean registrationDone = false;

    private static boolean toolsFieldPatched = false;

    private static int ourToolIndex = -1;

    private static Method getToolSlotSelectedMethod = null;

    public static boolean isAxiomLoaded() {
        return IS_AXIOM_LOADED;
    }

    /// Checks if Axiom's 10th "Builder Tool" slot is currently active.
    public static boolean isAxiomBuilderSlotActive() {
        if (!isAxiomLoaded() || builderInitFailed) return false;

        try {
            if (isToolSlotActiveMethod == null) {
                Class<?> managerClass = Class.forName("com.moulberry.axiom.buildertools.BuilderToolManager");
                isToolSlotActiveMethod = managerClass.getMethod("isToolSlotActive");
            }

            return (boolean) isToolSlotActiveMethod.invoke(null);
        } catch (Exception e) {
            EntitySelectorTools.LOGGER.error("While checking axiom builder slot active method", e);
            builderInitFailed = true;
            return false;
        }
    }

    /// Registers our creation tool as an Axiom BuilderTool in the 10th slot.
    /// Replaces `BuilderToolManager.tools` via `Unsafe.putObject` to work around JDK 12+'s
    /// restriction on `Field.set()` for `static final` fields, and Axiom's immutable list.
    public static void registerBuilderTool() {
        if (!isAxiomLoaded() || registrationDone) return;
        registrationDone = true;

        try {
            Class<?> builderToolInterface = Class.forName("com.moulberry.axiom.buildertools.BuilderTool");
            Object selectorBuilderToolProxy = Proxy.newProxyInstance(
                    AxiomCompat.class.getClassLoader(),
                    new Class<?>[]{builderToolInterface},
                    new SelectorBuilderTool()
            );

            Class<?> managerClass = Class.forName("com.moulberry.axiom.buildertools.BuilderToolManager");
            Field toolsField = managerClass.getDeclaredField("tools");
            toolsField.setAccessible(true);

            getToolSlotSelectedMethod = managerClass.getMethod("getToolSlotSelected");

            @SuppressWarnings("unchecked")
            List<Object> tools = (List<Object>) toolsField.get(null);
            List<Object> mutableTools = new ArrayList<>(tools);
            mutableTools.add(selectorBuilderToolProxy);
            ourToolIndex = mutableTools.size() - 1;

            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Object base = unsafe.staticFieldBase(toolsField);
            long offset = unsafe.staticFieldOffset(toolsField);
            unsafe.putObject(base, offset, mutableTools);
            toolsFieldPatched = true;

            EntitySelectorTools.LOGGER.info("Registered Entity Selector as Axiom BuilderTool at index {}",
                    ourToolIndex);
        } catch (Exception e) {
            EntitySelectorTools.LOGGER.error("Failed to register Axiom BuilderTool", e);
        }
    }

    /// Checks if Axiom's builder tool slot is active AND our tool is selected.
    public static boolean isSelectorBuilderToolActive() {
        if (!isAxiomLoaded() || !toolsFieldPatched || ourToolIndex < 0 || !isAxiomBuilderSlotActive()) return false;

        try {
            int selected = (int) getToolSlotSelectedMethod.invoke(null);
            return selected == ourToolIndex;
        } catch (Exception e) {
            return false;
        }
    }
}
