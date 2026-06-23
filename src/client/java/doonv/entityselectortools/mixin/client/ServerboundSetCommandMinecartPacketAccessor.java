package doonv.entityselectortools.mixin.client;

import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundSetCommandMinecartPacket.class)
public interface ServerboundSetCommandMinecartPacketAccessor {
    @Accessor("entity")
    int getEntity();
}
