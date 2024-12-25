package dev.vanutp.tgbridge.fabric.compat

import dev.vanutp.tgbridge.fabric.FabricTelegramBridge
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents
import net.minecraft.server.network.ServerPlayerEntity

class VanishCompat : ICompat {
    override val modId = "melius-vanish"

    fun isVanished(player: ServerPlayerEntity) = VanishAPI.isVanished(player)

    override fun enable() {
        VanishEvents.VANISH_EVENT.register { player, isVanished ->
            val username = player.displayName?.string ?: return@register
            if (isVanished) {
                FabricTelegramBridge.onPlayerLeave(username)
            } else {
                FabricTelegramBridge.onPlayerJoin(username)
            }
        }
    }
}
