package doonv.entityselectortools.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import doonv.entityselectortools.EntitySelectorToolsDeps;
import doonv.entityselectortools.MissingDepsScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (EntitySelectorToolsDeps.ANY_MISSING)
            return parent -> new MissingDepsScreen(parent);
        else
            return YACLModMenuIntegration.getModConfigScreenFactory();
    }
}
