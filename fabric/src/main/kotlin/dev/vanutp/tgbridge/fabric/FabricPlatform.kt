package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.TBChatMessageEvent
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.minecraft.server.MinecraftServer

class FabricPlatform(private val server: MinecraftServer) : Platform() {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)

    override fun registerChatMessageListener(handler: (TBChatMessageEvent) -> Unit) {
        ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, _ ->
            handler.invoke(
                TBChatMessageEvent(
                    sender.displayName.string,
                    message.unsignedContent() ?: Component.text(""),
                )
            )
        }
    }

    override fun broadcastMessage(text: Component) {
        FabricServerAudiences.of(server).all().sendMessage(text)
    }
}
