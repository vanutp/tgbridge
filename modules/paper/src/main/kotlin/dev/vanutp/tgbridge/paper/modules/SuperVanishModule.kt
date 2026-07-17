package dev.vanutp.tgbridge.paper.modules

import de.myzelyam.api.vanish.PlayerHideEvent
import de.myzelyam.api.vanish.PlayerShowEvent
import dev.vanutp.tgbridge.common.models.TgbridgeJoinEvent
import dev.vanutp.tgbridge.common.models.TgbridgeLeaveEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import org.bukkit.event.EventHandler

class SuperVanishModule(bridge: PaperTelegramBridge) : AbstractPaperModule(bridge) {
    override val paperId = "SuperVanish"

    @EventHandler
    private fun onVanish(e: PlayerHideEvent) {
        bridge.onPlayerLeave(TgbridgeLeaveEvent(e.player.toTgbridge(), e, ignoreVanish = true))
    }

    @EventHandler
    private fun onUnvanish(e: PlayerShowEvent) {
        bridge.onPlayerJoin(
            TgbridgeJoinEvent(e.player.toTgbridge(), true, e, ignoreVanish = true)
        )
    }
}
