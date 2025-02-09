package dev.vanutp.tgbridge.paper.compat

import de.myzelyam.api.vanish.PlayerHideEvent
import de.myzelyam.api.vanish.PlayerShowEvent
import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import org.bukkit.event.EventHandler

class SuperVanishCompat(bootstrap: PaperBootstrap) : IVanishCompat, AbstractCompat(bootstrap) {
    override val pluginId = "SuperVanish"

    @EventHandler
    private fun onVanish(e: PlayerHideEvent) {
        bootstrap.tgbridge.onPlayerLeave(getPlayerName(e.player))
    }

    @EventHandler
    private fun onUnvanish(e: PlayerShowEvent) {
        bootstrap.tgbridge.onPlayerJoin(getPlayerName(e.player))
    }
}
