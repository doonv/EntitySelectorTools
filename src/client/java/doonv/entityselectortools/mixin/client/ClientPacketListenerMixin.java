package doonv.entityselectortools.mixin.client;

import doonv.entityselectortools.preview.CommandBlockManager;
import doonv.entityselectortools.preview.SelectorOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if >=1.21.11 'vehicle.Minecart' -> 'vehicle.minecart.Minecart'
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleTagQueryPacket", at = @At("RETURN"))
    private void onTagQuery(ClientboundTagQueryPacket packet, CallbackInfo ci) {
        if (packet.getTag() != null) {
            CommandBlockManager.onQueryResponse(packet.getTransactionId(), packet.getTag());
        }
    }

    @Inject(method = "handleBlockEntityData", at = @At("RETURN"))
    private void onBlockEntityUpdate(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
        if (packet.getType() == BlockEntityTypes.COMMAND_BLOCK) {
            packet.getTag().getString("Command").ifPresent(
                    command -> CommandBlockManager.updateBlock(packet.getPos(), command));
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("RETURN"))
    private void onSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = level.getEntity(packet.id());

        if (entity instanceof MinecartCommandBlock minecart) {
            var command = minecart.getCommandBlock().getCommand();
            if (!command.isEmpty()) {
                CommandBlockManager.updateEntity(entity.getId(), command);
            } else {
                SelectorOverlayRenderer.removeMinecartEntry(entity.getId());
            }
        }
    }
}
