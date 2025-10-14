package dev.vanutp.tgbridge.paper.compat

import com.dthielke.herochat.ChannelChatEvent
import com.dthielke.herochat.Chatter
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class HeroChatCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge) {
    override val paperId = "Herochat"

    @EventHandler(priority = EventPriority.MONITOR)
    fun onHeroChatMessage(e: ChannelChatEvent) {
        if (
            !e.channel.name.equals(config.integrations.globalChatName, ignoreCase = true)
            || e.result != Chatter.Result.ALLOWED
        ) {
            return
        }
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.sender.player.toTgbridge(),
                Component.text(e.message),
                e,
            )
        )
    }

    override fun enable() {
        super.enable()
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            if (e.originalEvent !is ChannelChatEvent) {
                e.isCancelled = true
            }
        }
    }
}
