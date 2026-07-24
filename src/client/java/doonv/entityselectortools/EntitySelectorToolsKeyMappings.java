package doonv.entityselectortools;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/// Separate class so [EntitySelectorToolsClient] doesn't immediately load in Amecs.
public class EntitySelectorToolsKeyMappings {
    public static final KeyMapping COPY_AS_SELECTOR_KEY = KeyMappingHelper.registerKeyMapping(
            new AmecsKeyMappingWithKeyModifiers(
                    "key.%s.copyVolumeAsSelector".formatted(EntitySelectorTools.MOD_ID),
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C, EntitySelectorToolsClient.CONTROLS_CATEGORY,
                    new AmecsKeyModifierCombination(AmecsKeyModifiers.CONTROL)
            )
    );

    public static final KeyMapping COPY_AS_PREDICATE_KEY = KeyMappingHelper.registerKeyMapping(
            new AmecsKeyMappingWithKeyModifiers(
                    "key.%s.copyVolumeAsPredicate".formatted(EntitySelectorTools.MOD_ID),
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X, EntitySelectorToolsClient.CONTROLS_CATEGORY,
                    new AmecsKeyModifierCombination(AmecsKeyModifiers.CONTROL)
            )
    );

    public static void register() {
        // Dummy method to load the class
    }
}
