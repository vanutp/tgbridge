package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.compat.IVanishCompat
import net.kyori.adventure.text.Component
import java.util.UUID

interface ITgbridgePlayer {
    val uuid: UUID
    val username: String
    val displayName: String?
    val nativePlayer: Any?

    fun getName() = if (config.messages.useRealUsername) {
        username
    } else {
        displayName ?: username
    }

    fun isVanished() =
        TelegramBridge.INSTANCE.loadedIntegrations
            .find { it is IVanishCompat }
            ?.let {
                (it as IVanishCompat).isVanished(this)
            }
            ?: false
}
