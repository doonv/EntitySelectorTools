package doonv.entityselectortools.mixin.client;

import com.moulberry.axiom.editor.keybinds.Keybind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Keybind.class)
public interface AxiomKeybindAccessor {
    @Accessor("ingameDownLastTime")
    void setIngameDownLastTime(boolean value);
}
