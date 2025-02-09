package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import net.ess3.api.events.VanishStatusChangeEvent
import org.bukkit.event.EventHandler

class EssentialsVanishCompat(bootstrap: PaperBootstrap) : IVanishCompat, AbstractCompat(bootstrap) {
    override val pluginId = "Essentials"

    @EventHandler
    fun onVanishStatusChange(e: VanishStatusChangeEvent) {
        val username = getPlayerName(e.affected.base)
        if (e.value) {
            bootstrap.tgbridge.onPlayerLeave(username)
        } else {
            bootstrap.tgbridge.onPlayerJoin(username)
        }
    }
}
