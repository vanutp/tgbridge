package dev.vanutp.tgbridge.paper.compat

import com.dthielke.herochat.ChannelChatEvent
import com.dthielke.herochat.Chatter
import com.dthielke.herochat.Herochat
import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class HeroChatCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IChatCompat {
    override val paperId = "Herochat"

    @EventHandler(priority = EventPriority.MONITOR)
    fun onHeroChatMessage(e: ChannelChatEvent) {
        if (e.result != Chatter.Result.ALLOWED) {
            return
        }
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.sender.player.toTgbridge(),
                Component.text(e.message),
                e.channel.name,
                e,
            )
        )
    }

    override fun getChatRecipients(chat: ChatConfig) =
        Herochat.getChannelManager()
            .channels
            .find { it.name.equals(chat.name, true) }
            ?.members
            ?.map { it.player.toTgbridge() }
}
