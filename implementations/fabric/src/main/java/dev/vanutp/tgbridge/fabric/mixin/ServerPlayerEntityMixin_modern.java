package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.IHasPlayedBefore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin_modern implements IHasPlayedBefore {
    @Unique
    private boolean hasPlayedBefore = false;

    @Override
    public boolean tgbridge$getHasPlayedBefore() {
        return hasPlayedBefore;
    }

    @Inject(method = "readCustomData", at = @At("HEAD"))
    private void readCustomData(ReadView view, CallbackInfo ci) {
        hasPlayedBefore = true;
    }
}
