package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge.server
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import net.minecraft.server.level.ServerPlayer


class FabricPlatform : IPlatform {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)

    private fun getRecipients(chat: ChatConfig): List<ServerPlayer>? {
        val module = TelegramBridge.INSTANCE.chatModule
        val players = if (module == null) {
            server.playerList.players.takeIf { chat.isDefault }
        } else {
            module.getChatRecipients(chat)?.map { it.toNative() }
        }
        return players?.filterNot { MuteService.isMuted(it.uuid) }
    }

    override fun getChatRecipients(chat: ChatConfig) =
        getRecipients(chat)?.map { it.toTgbridge() }

    override fun broadcastMessage(chat: ChatConfig, text: Component) {
        val message = text.toMinecraft()
        server.sendSystemMessage(message)
        getRecipients(chat)?.forEach { player ->
            player.sendSystemMessage(message, false)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
        return server.playerList.players
            .map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            if (FabricTelegramBridge.versionInfo.IS_192) {
                // this::class.java is net.minecraft.locale.Language$1 and calling get on it
                // fails with IllegalAccessException for some reason
                val cls = Class.forName("net.minecraft.class_2477")  // net.minecraft.locale.Language
                val get = cls.getMethod("method_4679", String::class.javaObjectType)
                get.invoke(this, key) as String
            } else {
                getOrDefault(key)
            }
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) =
        FabricLoader.getInstance().isModLoaded(modId)

    override fun playerToTgbridge(player: Any) = (player as ServerPlayer).toTgbridge()
}
