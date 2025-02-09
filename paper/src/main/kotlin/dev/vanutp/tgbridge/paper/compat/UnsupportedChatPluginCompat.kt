package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

class UnsupportedChatPluginCompat(bootstrap: PaperBootstrap) : IChatCompat, AbstractCompat(bootstrap) {
    override val pluginId = null

    override fun shouldEnable(): Boolean {
        return config.messages.incompatiblePluginChatPrefix != null
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMessage(e: AsyncPlayerChatEvent) {
        if (e.isCancelled) {
            return
        }
        bootstrap.tgbridge.onChatMessage(TBPlayerEventData(getPlayerName(e.player), Component.text(e.message)))
    }
}
