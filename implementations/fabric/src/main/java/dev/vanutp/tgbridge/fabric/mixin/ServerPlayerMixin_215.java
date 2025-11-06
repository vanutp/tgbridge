package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.IHasPlayedBefore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin_215 implements IHasPlayedBefore {
    @Unique
    private boolean hasPlayedBefore = false;

    @Override
    public boolean tgbridge$getHasPlayedBefore() {
        return hasPlayedBefore;
    }

    @Dynamic
    @Inject(method = "method_5749", at = @At("HEAD"))
    private void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        hasPlayedBefore = true;
    }
}
