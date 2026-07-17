package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.ZipResourceContainer
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge.server
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import net.minecraft.server.level.ServerPlayer
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull


class FabricPlatform : IPlatform {
    override val name = "fabric"
    override val gameDir = FabricLoader.getInstance().gameDir
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)
    private val _modResources by lazy {
        val ignoreMods = listOf("minecraft", "fabricloader", "fabric-api", "fabric-language-kotlin")
        FabricLoader.getInstance().allMods.mapNotNull { mod ->
            val rootModId = mod.containingMod.getOrNull()?.metadata?.id ?: mod.metadata.id
            if (rootModId in ignoreMods) {
                return@mapNotNull null
            }
            val root = mod.rootPaths.singleOrNull() ?: return@mapNotNull null
            if (root.fileSystem.provider().scheme != "jar") {
                return@mapNotNull null
            }
            val containerPath = Path(root.fileSystem.toString())
            ZipResourceContainer(root, containerPath)
        }
    }

    override fun broadcastMessage(recipients: List<ITgbridgePlayer>, text: Component) {
        val message = text.toMinecraft()
        server.sendSystemMessage(message)
        recipients.forEach { player ->
            player.toNative()?.sendSystemMessage(message, false)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
        return server.playerList.players.map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            if (VersionInfo.IS_192) {
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

    override fun isModLoaded(modId: String) = FabricLoader.getInstance().isModLoaded(modId)
    override fun getModResources() = _modResources
    override fun getMinecraftVersion() = VersionInfo.version
    override fun playerToTgbridge(player: Any) = (player as ServerPlayer).toTgbridge()
}
