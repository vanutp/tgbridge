package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import dev.vanutp.tgbridge.fabric.IHasPlayedBefore;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class PlayerListMixin_modern {
    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_JOIN_EVENT().invoker().onPlayerJoin(player, ((IHasPlayedBefore)player).tgbridge$getHasPlayedBefore());
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void remove(ServerPlayer player, CallbackInfo ci) {
        CustomEvents.Companion.getPLAYER_LEAVE_EVENT().invoker().onPlayerLeave(player);
    }
}
