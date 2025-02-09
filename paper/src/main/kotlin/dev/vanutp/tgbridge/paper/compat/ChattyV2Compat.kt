package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent

class ChattyV2Compat(bootstrap: PaperBootstrap) : IChatCompat, AbstractCompat(bootstrap) {
    override val pluginId = "Chatty"

    override fun shouldEnable(): Boolean {
        return super.shouldEnable() && getPlugin()?.pluginMeta?.version?.startsWith("2.") == true
    }

    @EventHandler
    fun onChattyMessage(e: ChattyMessageEvent) {
        if (e.chat.name != config.messages.globalChatName) {
            return
        }
        bootstrap.tgbridge.onChatMessage(TBPlayerEventData(getPlayerName(e.player), Component.text(e.message)))
    }
}
