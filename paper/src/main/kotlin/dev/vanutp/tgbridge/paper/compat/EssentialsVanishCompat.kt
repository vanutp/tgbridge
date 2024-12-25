package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.paper.PaperBootstrap
import net.ess3.api.events.VanishStatusChangeEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EssentialsVanishCompat(bootstrap: PaperBootstrap) : Listener, AbstractCompat(bootstrap) {
    override val pluginId = "Essentials"

    override fun enable() {
        bootstrap.server.pluginManager.registerEvents(this, bootstrap)
    }

    @EventHandler
    fun onVanishStatusChange(e: VanishStatusChangeEvent) {
        val username = e.affected.base.displayName().asString()
        if (e.value) {
            bootstrap.tgbridge.onPlayerLeave(username)
        } else {
            bootstrap.tgbridge.onPlayerJoin(username)
        }
    }
}
