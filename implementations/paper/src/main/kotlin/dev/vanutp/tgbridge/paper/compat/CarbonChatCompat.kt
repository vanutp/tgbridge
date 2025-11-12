package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import net.draycia.carbon.api.CarbonChatProvider
import net.draycia.carbon.api.event.events.CarbonChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CarbonChatCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IChatCompat {
    override val paperId = "CarbonChat"

    fun onCarbonChatMessage(e: CarbonChatEvent) {
        val channel = e.chatChannel().key().asString().removePrefix("carbon:")
        if (e.cancelled()) {
            return
        }
        val sender = e.sender()
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                TgbridgePlayer(sender.uuid(), sender.username(), sender.displayName().asString()),
                e.message(),
                channel,
                e,
            )
        )
    }

    override fun getChatRecipients(chat: ChatConfig): List<Player>? {
        val cc = CarbonChatProvider.carbonChat()
        val channelKey = if (chat.name.contains(":")) {
            Key.key(chat.name)
        } else {
            Key.key("carbon:${chat.name}")
        }
        val channel = cc.channelRegistry()
            .channel(channelKey)
            ?: return null
        return cc.server().players()
            .asSequence()
            .filter { channel.permissions().hearingPermitted(it).permitted() }
            .filterNot { it.leftChannels().contains(channel.key()) }
            .map { Bukkit.getPlayer(it.uuid())!! }
            .toList()
    }

    override fun enable() {
        super.enable()
        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent::class.java, this::onCarbonChatMessage)
    }
}
