package doonv.entityselectortools.mixin.client;

import com.moulberry.axiom.buildertools.BuilderTool;
import com.moulberry.axiom.buildertools.BuilderToolManager;
import doonv.entityselectortools.EntitySelectorTools;
import doonv.entityselectortools.compat.AxiomCompat;
import doonv.entityselectortools.compat.SelectorBuilderTool;
import doonv.entityselectortools.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BuilderToolManager.class)
public class AxiomBuilderToolRegistrationMixin {
    @Shadow
    @Mutable
    private static List<BuilderTool> tools;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        if (!ClientConfig.get().boxCreationWithAxiomTool) return;
        try {
            List<BuilderTool> mutable = new ArrayList<>(tools);
            mutable.add(new SelectorBuilderTool());
            tools = mutable;

            AxiomCompat.axiomBuilderToolRegistered = true;

            EntitySelectorTools.LOGGER.info("Registered Entity Selector as Axiom BuilderTool at index {}",
                    mutable.size() - 1);
        } catch (Exception e) {
            EntitySelectorTools.LOGGER.error("Failed to register Axiom BuilderTool", e);
        }
    }
}
