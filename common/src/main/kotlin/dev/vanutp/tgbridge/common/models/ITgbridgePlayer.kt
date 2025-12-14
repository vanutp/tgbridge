package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.modules.IVanishModule
import java.util.UUID

interface ITgbridgePlayer {
    val uuid: UUID
    val username: String
    val displayName: String?

    /**
     * The native player object, i.e. `Player` on Paper and `ServerPlayer` on Fabric/(Neo)Forge.
     * Can be null. In that case, the player will be looked up by UUID when needed.
     */
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
