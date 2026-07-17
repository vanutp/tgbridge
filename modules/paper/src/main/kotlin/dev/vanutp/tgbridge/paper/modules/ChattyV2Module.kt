package dev.vanutp.tgbridge.paper.modules

import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import ru.mrbrikster.chatty.api.ChattyApi
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent
import kotlin.jvm.optionals.getOrNull


class ChattyV2Module(bridge: PaperTelegramBridge) : AbstractPaperModule(bridge), IChatModule {
    override val paperId = "Chatty"

    override fun shouldEnable(): Boolean {
        @Suppress("UnstableApiUsage")
        return super.shouldEnable() && getPlugin()?.pluginMeta?.version?.startsWith("2.") == true
    }

    @EventHandler
    fun onChattyMessage(e: ChattyMessageEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                Component.text(e.message),
                e.chat.name,
                e,
            )
        )
    }

    override fun enable() {
        super.enable()
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            e.recipients += getChatRecipients(e.chat) ?: emptyList()
        }
    }

    fun getChatRecipients(chat: ChatConfig) =
        ChattyApi.get()
            .getChat(chat.name)
            .getOrNull()
            ?.getRecipients(null)
            ?.map { it.toTgbridge() }
}
