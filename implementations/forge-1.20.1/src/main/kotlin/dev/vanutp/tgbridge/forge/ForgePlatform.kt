package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.util.Language
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.ServerLifecycleHooks

class ForgePlatform : IPlatform {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer().playerManager.broadcast(text.toMinecraft(), false)
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
}
