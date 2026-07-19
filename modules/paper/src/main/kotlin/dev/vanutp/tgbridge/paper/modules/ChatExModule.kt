package dev.vanutp.tgbridge.paper.modules

import de.jeter.chatex.api.events.PlayerUsesGlobalChatEvent
import dev.vanutp.tgbridge.common.ConfigManager
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class ChatExModule(bridge: PaperTelegramBridge) : AbstractPaperModule(bridge), IChatModule {
    override val paperId = "ChatEx"

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChatExMessage(e: PlayerUsesGlobalChatEvent) {
        if (e.isCancelled) return
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                Component.text(e.message),
                ConfigManager.config.getDefaultChat().name,
                e,
            )
        )
    }

    override fun enable() {
        super.enable()
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            if (e.chat.isDefault) {
                e.recipients += bridge.platform.getOnlinePlayers()
            }
        }
    }
}
