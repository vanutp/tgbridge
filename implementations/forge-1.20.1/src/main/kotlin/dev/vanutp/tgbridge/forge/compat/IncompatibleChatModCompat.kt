package dev.vanutp.tgbridge.forge.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.forge.ForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toAdventure
import dev.vanutp.tgbridge.forge.toTgbridge
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class IncompatibleChatModCompat(override val bridge: ForgeTelegramBridge) : AbstractCompat(bridge) {
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
        FORGE_BUS.register(this)
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            if (e.originalEvent is ServerChatEvent && (e.metadata["IncompatibleChatModCompat"] as? Boolean) != true) {
                e.isCancelled = true
            }
        }
    }
}
