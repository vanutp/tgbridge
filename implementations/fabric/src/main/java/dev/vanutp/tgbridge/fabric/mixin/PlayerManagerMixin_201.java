package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import dev.vanutp.tgbridge.fabric.IHasPlayedBefore;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin_201 {
    @Dynamic
    @Inject(method = "method_14570", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_JOIN_EVENT().invoker().onPlayerJoin(player, ((IHasPlayedBefore) player).tgbridge$getHasPlayedBefore());
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayerEntity player, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_LEAVE_EVENT().invoker().onPlayerLeave(player);
    }
}
