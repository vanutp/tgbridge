package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.paper.PaperBootstrap
import dev.vanutp.tgbridge.paper.getPlayerName
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import ru.brikster.chatty.api.event.ChattyMessageEvent

class ChattyV3Compat(bootstrap: PaperBootstrap) : IChatCompat, AbstractCompat(bootstrap) {
    override val pluginId = "Chatty"

    override fun shouldEnable(): Boolean {
        return super.shouldEnable() && getPlugin()?.pluginMeta?.version?.startsWith("3.") == true
    }

    @EventHandler
    fun onChattyMessage(e: ChattyMessageEvent) {
        if (e.chat.id != config.messages.globalChatName) {
            return
        }
        bootstrap.tgbridge.onChatMessage(TBPlayerEventData(getPlayerName(e.sender), Component.text(e.plainMessage)))
    }
}
