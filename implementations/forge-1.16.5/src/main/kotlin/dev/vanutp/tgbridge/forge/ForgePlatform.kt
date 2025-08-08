package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.MessageType
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.util.Language
import net.minecraft.util.Util.NIL_UUID
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.server.ServerLifecycleHooks

class ForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        val currentServer = ServerLifecycleHooks.getCurrentServer()
        val playerManager = currentServer.playerManager
        val players = playerManager.playerList.filterNot { MuteService.isMuted(it.uuid) }
        val message = text.toMinecraft()
        val sender = NIL_UUID
        val packet = GameMessageS2CPacket(
            message,
            MessageType.CHAT,
            sender
        )
        currentServer.sendSystemMessage(message, sender)
        for (player in players) {
            player.networkHandler.sendPacket(packet)
        }
    }

    override fun getOnlinePlayers(): List<TgbridgePlayer> {
        return ServerLifecycleHooks.getCurrentServer().playerManager.playerList
            .map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            get(key)
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) = ModList.get().isLoaded(modId)

    override fun playerToTgbridge(player: Any) = (player as PlayerEntity).toTgbridge()
}
