package dev.vanutp.tgbridge.forge.mixin;

import dev.vanutp.tgbridge.forge.IHasPlayedBefore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin implements IHasPlayedBefore {
    @Unique
    private boolean tgbridge$hasPlayedBefore = false;

    @Override
    public boolean tgbridge$getHasPlayedBefore() {
        return tgbridge$hasPlayedBefore;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        tgbridge$hasPlayedBefore = true;
    }
}
