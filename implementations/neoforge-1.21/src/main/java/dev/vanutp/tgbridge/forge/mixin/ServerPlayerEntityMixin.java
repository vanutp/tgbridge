package dev.vanutp.tgbridge.forge.mixin;

import dev.vanutp.tgbridge.forge.IHasPlayedBefore;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin implements IHasPlayedBefore {
    @Unique
    private boolean tgbridge$hasPlayedBefore = false;

    @Override
    public boolean tgbridge$getHasPlayedBefore() {
        return tgbridge$hasPlayedBefore;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        tgbridge$hasPlayedBefore = true;
    }
}
