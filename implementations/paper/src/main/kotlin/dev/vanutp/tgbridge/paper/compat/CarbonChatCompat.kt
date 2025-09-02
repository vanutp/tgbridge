package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.EventResult
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import net.draycia.carbon.api.CarbonChatProvider
import net.draycia.carbon.api.event.events.CarbonChatEvent

class CarbonChatCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge) {
    override val paperId = "CarbonChat"

    fun onCarbonChatMessage(e: CarbonChatEvent) {
        val channel = e.chatChannel().key().asString()
        val channelValid =
            channel.equals(config.integrations.globalChatName, ignoreCase = true)
                || channel.equals("carbon:" + config.integrations.globalChatName, ignoreCase = true)
        if (!channelValid || e.cancelled()) {
            return
        }
        val sender = e.sender()
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                TgbridgePlayer(sender.uuid(), sender.username(), sender.displayName().asString()),
                e.message(),
                e,
            )
        )
    }

    override fun enable() {
        super.enable()
        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent::class.java, this::onCarbonChatMessage)
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            if (e.originalEvent is CarbonChatEvent) {
                EventResult.CONTINUE
            } else {
                EventResult.STOP
            }
        }
    }
}
