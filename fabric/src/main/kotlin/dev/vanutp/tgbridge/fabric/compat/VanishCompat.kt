package dev.vanutp.tgbridge.fabric.compat

import dev.vanutp.tgbridge.fabric.FabricTelegramBridge
import dev.vanutp.tgbridge.fabric.getPlayerName
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents
import net.minecraft.server.network.ServerPlayerEntity

class VanishCompat : ICompat {
    override val modId = "melius-vanish"

    fun isVanished(player: ServerPlayerEntity) = VanishAPI.isVanished(player)

    override fun enable() {
        VanishEvents.VANISH_EVENT.register { player, isVanished ->
            val username = getPlayerName(player).string
            if (isVanished) {
                FabricTelegramBridge.onPlayerLeave(username)
            } else {
                FabricTelegramBridge.onPlayerJoin(username, true)
            }
        }
    }
}
