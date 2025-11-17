package dev.vanutp.tgbridge.paper.modules

import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import ru.brikster.chatty.api.ChattyApi
import ru.brikster.chatty.api.event.ChattyMessageEvent

class ChattyV3Module(bridge: PaperTelegramBridge) : AbstractPaperModule(bridge), IChatModule {
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

    override fun getChatRecipients(chat: ChatConfig) =
        ChattyApi.instance()
            .chats[chat.name]
            ?.calculateRecipients(null)
            ?.map { it.toTgbridge() }
}
