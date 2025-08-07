package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge.server
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.util.Language


class FabricPlatform : IPlatform {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        val playerManager = server.playerManager
        val players = playerManager.playerList.filterNot { MuteService.isMuted(it.uuid) }
        val message = text.toMinecraft()
        server.sendMessage(message)
        for (player in players) {
            player.sendMessageToClient(message, false)
        }
    }

    override fun getOnlinePlayers(): List<TgbridgePlayer> {
        return server.playerManager.playerList
            .map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            if (FabricTelegramBridge.versionInfo.IS_192) {
                // this::class.java is net.minecraft.util.Language$1 and calling get on it
                // fails with IllegalAccessException for some reason
                val cls = Class.forName("net.minecraft.class_2477")  // net.minecraft.util.Language
                val get = cls.getMethod("method_4679", String::class.javaObjectType)
                get.invoke(this, key) as String
            } else {
                get(key)
            }
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) =
        FabricLoader.getInstance().isModLoaded(modId)
}
