package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import net.minecraft.world.entity.player.Player
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.ServerLifecycleHooks

class ForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        val currentServer = ServerLifecycleHooks.getCurrentServer()
        val playerManager = currentServer.playerList
        val players = playerManager.players.filterNot { MuteService.isMuted(it.uuid) }
        val message = text.toMinecraft()
        currentServer.sendSystemMessage(message)
        for (player in players) {
            player.sendSystemMessage(message, false)
        }
    }

    override fun getOnlinePlayers(): List<TgbridgePlayer> {
        return ServerLifecycleHooks.getCurrentServer().playerList.players
            .map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            getOrDefault(key)
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) = ModList.get().isLoaded(modId)

    override fun playerToTgbridge(player: Any) = (player as Player).toTgbridge()
}
