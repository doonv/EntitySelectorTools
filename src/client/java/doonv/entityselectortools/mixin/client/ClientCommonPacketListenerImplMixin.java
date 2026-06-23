package doonv.entityselectortools.mixin.client;

import doonv.entityselectortools.preview.CommandBlockManager;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundSetCommandBlockPacket updatePacket) {
            CommandBlockManager.updateBlock(updatePacket.getPos(), updatePacket.getCommand());
        } else if (packet instanceof ServerboundSetCommandMinecartPacket minecartPacket) {
            int entityId = ((ServerboundSetCommandMinecartPacketAccessor) minecartPacket).getEntity();
            CommandBlockManager.updateEntity(entityId, minecartPacket.getCommand());
        }
    }
}
