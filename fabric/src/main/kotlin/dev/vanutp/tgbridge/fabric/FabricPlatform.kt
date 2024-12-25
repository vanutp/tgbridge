package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge.server
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.util.Language


class FabricPlatform : Platform() {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)

    override fun broadcastMessage(text: Component) {
        server.playerManager.broadcast(text.toMinecraft(), false)
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return server.playerNames
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
}
