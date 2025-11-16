package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

class IncompatibleChatPluginCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IChatCompat {
    override fun shouldEnable(): Boolean {
        return config.integrations.incompatiblePluginChatPrefix != null
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMessage(e: AsyncPlayerChatEvent) {
        if (e.isCancelled) {
            return
        }
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                Component.text(e.message),
                null,
                e,
            )
        )
    }

    override fun getChatRecipients(chat: ChatConfig) =
        bridge.plugin.server.onlinePlayers
            .takeIf { chat.isDefault }
            ?.map { it.toTgbridge() }
}
