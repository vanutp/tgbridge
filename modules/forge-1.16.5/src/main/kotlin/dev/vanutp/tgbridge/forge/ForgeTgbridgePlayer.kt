package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraftforge.fml.server.ServerLifecycleHooks

fun PlayerEntity.toTgbridge() = ForgeTgbridgePlayer(this)

fun ITgbridgePlayer.toNative(): ServerPlayerEntity? =
    this.nativePlayer?.let { it as ServerPlayerEntity }
        ?: ServerLifecycleHooks.getCurrentServer().playerManager.getPlayer(uuid)

class ForgeTgbridgePlayer(player: PlayerEntity) : ITgbridgePlayer {
    override val nativePlayer = player as? ServerPlayerEntity
    override val uuid = player.uuid
    override val username = player.name.string
    override val displayName = player.displayName.string
}
