package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.IHasPlayedBefore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin_modern implements IHasPlayedBefore {
    @Unique
    private boolean hasPlayedBefore = false;

    @Override
    public boolean tgbridge$getHasPlayedBefore() {
        return hasPlayedBefore;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readAdditionalSaveData(ValueInput view, CallbackInfo ci) {
        hasPlayedBefore = true;
    }
}
