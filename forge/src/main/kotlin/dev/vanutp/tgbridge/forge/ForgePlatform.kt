package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.TBChatMessageEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.Text
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ForgePlatform : Platform() {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(ForgeTelegramBridge.MOD_ID)

    private fun adventureToMinecraft(adventure: Component): Text {
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serializeToTree(adventure))!!
    }

    private fun minecraftToAdventure(minecraft: Text): Component {
        return GsonComponentSerializer.gson().deserializeFromTree(Text.Serializer.toJsonTree(minecraft))
    }

    override fun registerChatMessageListener(handler: (TBChatMessageEvent) -> Unit) {
        FORGE_BUS.addListener { e: ServerChatEvent ->
            handler.invoke(
                TBChatMessageEvent(
                    e.player.displayName.string,
                    minecraftToAdventure(e.message)
                )
            )
        }
    }

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer().playerManager.broadcast(adventureToMinecraft(text), false)
    }
}
