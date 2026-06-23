package doonv.entityselectortools.compat;

import doonv.entityselectortools.EntitySelectorTools;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/// Handles compatibility with Axiom without creating a hard dependency.
public class AxiomCompat {
    private static final boolean IS_AXIOM_LOADED = FabricLoader.getInstance().isModLoaded("axiom");

    // Reflection cache for Keybind suppression
    private static boolean copyInitFailed = false;

    private static Object copyIngameKeybindInstance = null;

    private static Field ingameDownLastTimeField = null;

    // Reflection cache for Builder Slot check
    private static boolean builderInitFailed = false;

    private static Method isToolSlotActiveMethod = null;

    // Reflection cache for Builder Tool registration
    private static boolean registrationDone = false;

    private static boolean toolsFieldPatched = false;

    private static int ourToolIndex = -1;

    private static Method getToolSlotSelectedMethod = null;

    public static boolean isAxiomLoaded() {
        return IS_AXIOM_LOADED;
    }

    /// Prevents Axiom from overriding our own Copy.
    /// By setting Axiom's `ingameDownLastTime` to true, Axiom's
    /// rising-edge check (`down && !wasDown`) will return `false`.
    public static void suppressAxiomCopy() {
        if (!IS_AXIOM_LOADED || copyInitFailed) return;

        try {
            if (copyIngameKeybindInstance == null) {
                Class<?> keybindsClass = Class.forName("com.moulberry.axiom.editor.keybinds.Keybinds");
                copyIngameKeybindInstance = keybindsClass.getField("COPY_INGAME").get(null);

                Class<?> keybindClass = Class.forName("com.moulberry.axiom.editor.keybinds.Keybind");
                ingameDownLastTimeField = keybindClass.getDeclaredField("ingameDownLastTime");
                ingameDownLastTimeField.setAccessible(true);
            }

            // Set the state to true to "lock" the keybind from firing
            if (ingameDownLastTimeField != null && copyIngameKeybindInstance != null) {
                ingameDownLastTimeField.setBoolean(copyIngameKeybindInstance, true);
            }
        } catch (Exception e) {
            EntitySelectorTools.LOGGER.error("While suppressing Axiom's Ctrl+C copy", e);
            copyInitFailed = true;
        }
    }

    /// Checks if Axiom's 10th "Builder Tool" slot is currently active.
    public static boolean isAxiomBuilderSlotActive() {
        if (!IS_AXIOM_LOADED || builderInitFailed) return false;

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
    /// Injects into `BuilderToolManager.tools` via reflection.
    public static void registerBuilderTool() {
        if (!IS_AXIOM_LOADED || registrationDone) return;
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

            List<?> originalTools = (List<?>) toolsField.get(null);
            List<Object> newTools = new ArrayList<>(originalTools);
            newTools.add(selectorBuilderToolProxy);
            ourToolIndex = newTools.size() - 1;

            setFinalStaticField(toolsField, newTools);
            toolsFieldPatched = true;

            EntitySelectorTools.LOGGER.info("Registered Entity Selector as Axiom BuilderTool at index {}",
                    ourToolIndex);
        } catch (Exception e) {
            EntitySelectorTools.LOGGER.error("Failed to register Axiom BuilderTool", e);
        }
    }

    /// Sets a static final field to a new value using Unsafe (bypasses Java 17+ module restrictions).
    private static void setFinalStaticField(Field field, Object newValue) throws Exception {
        try {
            field.set(null, newValue);
        } catch (Exception e) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeClass.cast(unsafeField.get(null));
            Method sfb = unsafeClass.getMethod("staticFieldBase", Field.class);
            Method sfo = unsafeClass.getMethod("staticFieldOffset", Field.class);
            Method putObj = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
            Object base = sfb.invoke(unsafe, field);
            long offset = (long) sfo.invoke(unsafe, field);
            putObj.invoke(unsafe, base, offset, newValue);
        }
    }

    /// Checks if Axiom's builder tool slot is active AND our tool is selected.
    public static boolean isSelectorBuilderToolActive() {
        if (!IS_AXIOM_LOADED || !toolsFieldPatched || ourToolIndex < 0 || !isAxiomBuilderSlotActive()) return false;

        try {
            int selected = (int) getToolSlotSelectedMethod.invoke(null);
            return selected == ourToolIndex;
        } catch (Exception e) {
            return false;
        }
    }
}
