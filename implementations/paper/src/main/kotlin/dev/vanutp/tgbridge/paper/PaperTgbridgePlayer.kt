package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import org.bukkit.entity.Player

fun Player.toTgbridge() = PaperTgbridgePlayer(this)

fun ITgbridgePlayer.toNative(): Player? =
    this.nativePlayer?.let { it as Player }
        ?: (TelegramBridge.INSTANCE as PaperTelegramBridge).plugin.server.getPlayer(uuid)

class PaperTgbridgePlayer(override val nativePlayer: Player) : ITgbridgePlayer {
    override val uuid = nativePlayer.uniqueId
    override val username = nativePlayer.name
    override val displayName = nativePlayer.displayName().asString()
}
