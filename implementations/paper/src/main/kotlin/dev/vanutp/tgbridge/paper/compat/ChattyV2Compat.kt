package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import ru.mrbrikster.chatty.api.ChattyApi
import ru.mrbrikster.chatty.api.events.ChattyMessageEvent
import kotlin.jvm.optionals.getOrNull


class ChattyV2Compat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IChatCompat {
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

    override fun getChatRecipients(chat: ChatConfig): List<Player>? =
        ChattyApi.get()
            .getChat(chat.name)
            .getOrNull()
            ?.getRecipients(null)
            ?.toList()
}
