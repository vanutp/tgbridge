package dev.vanutp.tgbridge.fabric.compat

import dev.vanutp.tgbridge.common.compat.IVanishCompat
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.models.*
import dev.vanutp.tgbridge.fabric.FabricEventWrapper
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge
import dev.vanutp.tgbridge.fabric.toTgbridge
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents

class VanishCompat(override val bridge: FabricTelegramBridge) : AbstractCompat(bridge), IVanishCompat {
    override val fabricId = "melius-vanish"

    override fun enable() {
        VanishEvents.VANISH_EVENT.register { player, isVanished ->
            val originalEvent = FabricEventWrapper(
                VanishEvents.VanishEvent::class,
                listOf(player, isVanished)
            )
            if (isVanished) {
                FabricTelegramBridge.onPlayerLeave(
                    TgbridgeLeaveEvent(player.toTgbridge(), originalEvent)
                )
            } else {
                FabricTelegramBridge.onPlayerJoin(
                    TgbridgeJoinEvent(player.toTgbridge(), true, originalEvent)
                )
            }
        }
    }

    override fun isVanished(player: TgbridgePlayer) =
        VanishAPI.isVanished(bridge.server, player.uuid)
}
