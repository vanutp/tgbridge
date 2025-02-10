package dev.vanutp.tgbridge.paper.compat

import com.earth2me.essentials.Essentials
import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import net.ess3.api.events.VanishStatusChangeEvent
import net.essentialsx.api.v2.events.AsyncUserDataLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent

class EssentialsVanishCompat(bootstrap: PaperBootstrap) : IVanishCompat, AbstractCompat(bootstrap) {
    override val pluginId = "Essentials"

    @EventHandler
    fun onPlayerJoin(e: AsyncUserDataLoadEvent) {
        if (!e.user.isHidden) {
            bootstrap.tgbridge.onPlayerJoin(
                getPlayerName(e.user.base),
                e.user.base.hasPlayedBefore(),
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val ess = bootstrap.server.pluginManager.getPlugin(pluginId) as Essentials
        val user = ess.getUser(e.player)
        if (!user.isHidden && !user.isLeavingHidden) {
            bootstrap.tgbridge.onPlayerLeave(getPlayerName(e.player))
        }
    }

    @EventHandler
    fun onVanishStatusChange(e: VanishStatusChangeEvent) {
        val username = getPlayerName(e.affected.base)
        if (e.value) {
            bootstrap.tgbridge.onPlayerLeave(username)
        } else {
            bootstrap.tgbridge.onPlayerJoin(username, true)
        }
    }
}
