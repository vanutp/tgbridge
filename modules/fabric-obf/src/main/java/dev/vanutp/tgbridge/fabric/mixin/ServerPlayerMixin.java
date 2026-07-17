package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {
    @Inject(method = "die", at = @At("TAIL"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_DEATH_EVENT().invoker().onPlayerDeath((ServerPlayer) (Object) this, damageSource);
    }
}
