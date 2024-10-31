package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin_modern {
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_JOIN_EVENT().invoker().onPlayerJoin(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayerEntity player, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_LEAVE_EVENT().invoker().onPlayerLeave(player);
    }
}
