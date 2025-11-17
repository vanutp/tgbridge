package dev.vanutp.tgbridge.fabric.compat

import dev.vanutp.tgbridge.common.modules.IVanishModule
import dev.vanutp.tgbridge.common.modules.AbstractModule
import dev.vanutp.tgbridge.common.models.*
import dev.vanutp.tgbridge.fabric.FabricEventWrapper
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge
import dev.vanutp.tgbridge.fabric.toTgbridge
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents

class VanishModule(override val bridge: FabricTelegramBridge) : AbstractModule(bridge), IVanishModule {
    override val fabricId = "melius-vanish"

    override fun enable() {
        VanishEvents.VANISH_EVENT.register { player, isVanished ->
            val originalEvent = FabricEventWrapper(
                VanishEvents.VanishEvent::class,
                listOf(player, isVanished)
            )
            if (isVanished) {
                FabricTelegramBridge.onPlayerLeave(
                    TgbridgeLeaveEvent(player.toTgbridge(), originalEvent, ignoreVanish = true)
                )
            } else {
                FabricTelegramBridge.onPlayerJoin(
                    TgbridgeJoinEvent(player.toTgbridge(), true, originalEvent, ignoreVanish = true)
                )
            }
        }
    }

    override fun isVanished(player: ITgbridgePlayer) =
        VanishAPI.isVanished(bridge.server, player.uuid)
}
