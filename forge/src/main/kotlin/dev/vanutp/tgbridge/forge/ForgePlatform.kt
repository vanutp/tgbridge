package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.TBPlayerEventData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
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

    override fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: ServerChatEvent ->
            handler.invoke(
                TBPlayerEventData(
                    e.player.displayName.string,
                    minecraftToAdventure(e.message)
                )
            )
        }
    }

    fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: LivingDeathEvent ->
            if (e.entity !is PlayerEntity) {
                return@addListener
            }
            val deathMessage = e.source.getDeathMessage(e.entity)
        }
    }

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer().playerManager.broadcast(adventureToMinecraft(text), false)
    }
}
