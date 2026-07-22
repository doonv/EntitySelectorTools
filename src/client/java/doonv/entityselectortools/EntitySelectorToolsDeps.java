package doonv.entityselectortools;

import net.fabricmc.loader.api.FabricLoader;

/// Separate class because otherwise when it's accessed by [doonv.entityselectortools.mixin.MixinPlugin]
/// it would load classes that don't exist yet.
public class EntitySelectorToolsDeps {
    public static final boolean YACL_LOADED = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
    public static final boolean AMECS_LOADED = FabricLoader.getInstance().isModLoaded("amecs_key_modifiers");
    public static final boolean ANY_MISSING = !YACL_LOADED || !AMECS_LOADED;
}
