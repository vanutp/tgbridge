package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.network.MessageType
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Language
import net.minecraft.util.Util.NIL_UUID
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.server.ServerLifecycleHooks

class ForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    private fun getRecipients(chat: ChatConfig): List<ServerPlayerEntity>? {
        val server = ServerLifecycleHooks.getCurrentServer()
        val integration = TelegramBridge.INSTANCE.chatIntegration
        val players = if (integration == null) {
            server.playerManager.playerList.takeIf { chat.isDefault }
        } else {
            integration.getChatRecipients(chat)?.map { it.toNative() }
        }
        return players?.filterNot { MuteService.isMuted(it.uuid) }
    }

    override fun getChatRecipients(chat: ChatConfig) =
        getRecipients(chat)?.map { it.toTgbridge() }

    override fun broadcastMessage(chat: ChatConfig, text: Component) {
        val server = ServerLifecycleHooks.getCurrentServer()
        val message = text.toMinecraft()
        val packet = GameMessageS2CPacket(
            message,
            MessageType.CHAT,
            NIL_UUID,
        )
        server.sendSystemMessage(message, NIL_UUID)
        getRecipients(chat)?.forEach { player ->
            player.networkHandler.sendPacket(packet)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
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

    override fun playerToTgbridge(player: Any) = (player as ServerPlayerEntity).toTgbridge()
}
