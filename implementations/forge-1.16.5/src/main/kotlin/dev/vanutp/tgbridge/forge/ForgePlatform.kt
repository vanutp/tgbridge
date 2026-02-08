package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.ZipResourceContainer
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.SharedConstants
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
    override val gameDir = FMLPaths.GAMEDIR.get()
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)
    private val _modResources: List<ZipResourceContainer> by lazy {
        ModList.get().mods.mapNotNull {
            if (it.modId == "minecraft" || it.modId == "forge") {
                return@mapNotNull null
            }
            val containerPath = it.owningFile.file.filePath
            if (containerPath.toString() == "") {
                // I have no idea how to get the containing jar path
                return@mapNotNull null
            }
            ZipResourceContainer(null, containerPath)
        }
    }

    override fun broadcastMessage(recipients: List<ITgbridgePlayer>, text: Component) {
        val server = ServerLifecycleHooks.getCurrentServer()
        val message = text.toMinecraft()
        val packet = GameMessageS2CPacket(
            message,
            MessageType.CHAT,
            NIL_UUID,
        )
        server.sendSystemMessage(message, NIL_UUID)
        recipients.forEach { player ->
            player.toNative()?.networkHandler?.sendPacket(packet)
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
    override fun getModResources() = _modResources
    override fun getMinecraftVersion() = SharedConstants.getGameVersion().id
    override fun playerToTgbridge(player: Any) = (player as ServerPlayerEntity).toTgbridge()
}
