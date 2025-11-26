package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.modules.IVanishModule
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
        TelegramBridge.INSTANCE.enabledModules
            .filterIsInstance<IVanishModule>()
            .any { it.isVanished(this) }
}
