package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import net.minecraft.server.level.ServerPlayer
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.server.ServerLifecycleHooks

class NeoForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(NeoForgeTelegramBridge.MOD_ID)

    private fun getRecipients(chat: ChatConfig): List<ServerPlayer>? {
        val server = ServerLifecycleHooks.getCurrentServer() ?: return null
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
        val server = ServerLifecycleHooks.getCurrentServer() ?: return
        val message = text.toMinecraft()
        server.sendSystemMessage(message)
        getRecipients(chat)?.forEach { player ->
            player.sendSystemMessage(message, false)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
        return ServerLifecycleHooks.getCurrentServer()?.playerList?.players
            ?.map { it.toTgbridge() }
            ?: emptyList()
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            getOrDefault(key)
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) = ModList.get().isLoaded(modId)

    override fun playerToTgbridge(player: Any) = (player as ServerPlayer).toTgbridge()
}
