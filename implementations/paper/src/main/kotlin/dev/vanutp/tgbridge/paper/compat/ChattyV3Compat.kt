package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import ru.brikster.chatty.api.ChattyApi
import ru.brikster.chatty.api.event.ChattyMessageEvent

class ChattyV3Compat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IChatCompat {
    override val paperId = "Chatty"

    override fun shouldEnable(): Boolean {
        @Suppress("UnstableApiUsage")
        return super.shouldEnable() && getPlugin()?.pluginMeta?.version?.startsWith("3.") == true
    }

    @EventHandler
    fun onChattyMessage(e: ChattyMessageEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.sender.toTgbridge(),
                Component.text(e.plainMessage),
                e.chat.id,
                e,
            )
        )
    }

    override fun getChatRecipients(chat: ChatConfig): List<Player>? =
        ChattyApi.instance()
            .chats[chat.name]
            ?.calculateRecipients(null)
            ?.toList()
}
