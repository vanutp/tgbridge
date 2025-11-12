package dev.vanutp.tgbridge.forge.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.compat.IChatCompat
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.forge.NeoForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toAdventure
import dev.vanutp.tgbridge.forge.toTgbridge
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks

class IncompatibleChatModCompat(override val bridge: NeoForgeTelegramBridge) : AbstractCompat(bridge), IChatCompat {
    override fun shouldEnable(): Boolean {
        return config.integrations.incompatiblePluginChatPrefix != null
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMessage(e: ServerChatEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                e.message.toAdventure(),
                null,
                e,
                mapOf("IncompatibleChatModCompat" to true)
            )
        )
    }

    override fun enable() {
        NeoForge.EVENT_BUS.register(this)
    }

    override fun getChatRecipients(chat: ChatConfig): List<ServerPlayer>?
        = ServerLifecycleHooks.getCurrentServer()?.playerList?.players?.takeIf { chat.isDefault }
}
