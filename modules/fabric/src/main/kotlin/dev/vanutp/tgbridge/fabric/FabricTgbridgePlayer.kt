package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

fun Player.toTgbridge() = FabricTgbridgePlayer(this)

fun ITgbridgePlayer.toNative(): ServerPlayer? =
    this.nativePlayer?.let { it as ServerPlayer }
        ?: FabricTelegramBridge.server.playerList?.getPlayer(uuid)

class FabricTgbridgePlayer(player: Player) : ITgbridgePlayer {
    override val nativePlayer = player as? ServerPlayer
    override val uuid = player.uuid
    override val username = player.name.string
    override val displayName = player.displayName?.string
}
