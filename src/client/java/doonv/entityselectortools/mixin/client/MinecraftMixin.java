package doonv.entityselectortools.mixin.client;

import doonv.entityselectortools.creation.CreationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    public int missTime;

    //~ if >=26.1 'pickBlock' -> 'pickBlockOrEntity'
    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void middleClickExtendVolumeSelect(CallbackInfo ci) {
        if (this.player == null || !CreationManager.inWandCreationMode(this.player)) return;

        BlockPos pos = CreationManager.getTargetPos(this.player);

        CreationManager.expandTo(pos);

        ci.cancel();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void leftClickSetPos1(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null || !CreationManager.inWandCreationMode(this.player))
            return;

        CreationManager.pos1 = CreationManager.getTargetPos(this.player);

        this.missTime = 0;

        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void rightClickSetPos2(CallbackInfo ci) {
        if (this.player == null || !CreationManager.inWandCreationMode(this.player))
            return;

        CreationManager.pos2 = CreationManager.getTargetPos(this.player);

        ci.cancel();
    }
}
