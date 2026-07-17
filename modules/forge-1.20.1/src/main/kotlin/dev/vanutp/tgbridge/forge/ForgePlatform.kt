package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.ZipResourceContainer
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.SharedConstants
import net.minecraft.locale.Language
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.ServerLifecycleHooks

class ForgePlatform : IPlatform {
    override val name = "forge"
    override val gameDir = FMLPaths.GAMEDIR.get()
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)
    private val _modResources by lazy {
        ModList.get().mods.mapNotNull {
            if (it.modId == "minecraft" || it.modId == "forge") {
                return@mapNotNull null
            }
            val root = it.owningFile.file.secureJar.rootPath
            val containerPath = it.owningFile.file.secureJar.primaryPath
            if (containerPath.toString() == "") {
                // I have no idea how to get the containing jar path
                return@mapNotNull null
            }
            ZipResourceContainer(root, containerPath)
        }
    }

    override fun broadcastMessage(recipients: List<ITgbridgePlayer>, text: Component) {
        val server = ServerLifecycleHooks.getCurrentServer()
        val message = text.toMinecraft()
        server.sendSystemMessage(message)
        recipients.forEach { player ->
            player.toNative()?.sendSystemMessage(message, false)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
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
    override fun getModResources() = _modResources
    override fun getMinecraftVersion() = SharedConstants.getCurrentVersion().id
    override fun playerToTgbridge(player: Any) = (player as ServerPlayer).toTgbridge()
}
