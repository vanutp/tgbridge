package dev.vanutp.tgbridge.forge.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.EventResult
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.forge.NeoForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toAdventure
import dev.vanutp.tgbridge.forge.toTgbridge
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.ServerChatEvent

class IncompatibleChatModCompat(override val bridge: NeoForgeTelegramBridge) : AbstractCompat(bridge) {
    override fun shouldEnable(): Boolean {
        return config.integrations.incompatiblePluginChatPrefix != null
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMessage(e: ServerChatEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                e.message.toAdventure(),
                e,
                mapOf("IncompatibleChatModCompat" to true)
            )
        )
    }

    override fun enable() {
        NeoForge.EVENT_BUS.register(this)
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            if (e.originalEvent is ServerChatEvent && (e.metadata["IncompatibleChatModCompat"] as? Boolean) != true) {
                EventResult.STOP
            } else {
                EventResult.CONTINUE
            }
        }
    }
}
