package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.util.Language
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.server.ServerLifecycleHooks

class NeoForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(NeoForgeTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        val currentServer = ServerLifecycleHooks.getCurrentServer() ?: return
        val playerManager = currentServer.playerManager
        val players = playerManager.playerList.filterNot { MuteService.isMuted(it.uuid) }
        val message = text.toMinecraft()
        currentServer.sendMessage(message)
        for (player in players) {
            player.sendMessageToClient(message, false)
        }
    }

    override fun getOnlinePlayers(): List<TgbridgePlayer> {
        return ServerLifecycleHooks.getCurrentServer()?.playerManager?.playerList
            ?.map { it.toTgbridge() }
            ?: emptyList()
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            get(key)
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) = ModList.get().isLoaded(modId)
}
