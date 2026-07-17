package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.server.ServerLifecycleHooks

fun Player.toTgbridge() = ForgeTgbridgePlayer(this)

fun ITgbridgePlayer.toNative(): ServerPlayer? =
    this.nativePlayer?.let { it as ServerPlayer }
        ?: ServerLifecycleHooks.getCurrentServer()?.playerList?.getPlayer(uuid)

class ForgeTgbridgePlayer(player: Player) : ITgbridgePlayer {
    override val nativePlayer = player as? ServerPlayer
    override val uuid = player.uuid
    override val username = player.name.string
    override val displayName = player.displayName?.string
}
