package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.Platform
import net.kyori.adventure.text.Component
import net.minecraft.network.MessageType
import net.minecraft.util.Language
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.server.ServerLifecycleHooks

class ForgePlatform : Platform() {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer().playerManager.broadcastChatMessage(
            text.toMinecraft(),
            MessageType.CHAT,
            net.minecraft.util.Util.NIL_UUID
        )
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return ServerLifecycleHooks.getCurrentServer().playerNames
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            get(key)
        } else {
            null
        }
    }
}
