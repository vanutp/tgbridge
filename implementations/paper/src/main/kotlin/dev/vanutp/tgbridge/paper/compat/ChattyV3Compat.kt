package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.EventResult
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import ru.brikster.chatty.api.event.ChattyMessageEvent

class ChattyV3Compat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge) {
    override val paperId = "Chatty"

    override fun shouldEnable(): Boolean {
        @Suppress("UnstableApiUsage")
        return super.shouldEnable() && getPlugin()?.pluginMeta?.version?.startsWith("3.") == true
    }

    @EventHandler
    fun onChattyMessage(e: ChattyMessageEvent) {
        if (e.chat.id != config.integrations.globalChatName) {
            return
        }
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.sender.toTgbridge(),
                Component.text(e.plainMessage),
                e,
            )
        )
    }

    override fun enable() {
        super.enable()
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            if (e.originalEvent is ChattyMessageEvent) {
                EventResult.CONTINUE
            } else {
                EventResult.STOP
            }
        }
    }
}
