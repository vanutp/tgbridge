package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.TBChatMessageEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute

class PaperPlatform(private val plugin: JavaPlugin) : Platform() {
    override val name = "paper"
    override val configDir = plugin.dataFolder.toPath().absolute()

    override fun registerChatMessageListener(handler: (TBChatMessageEvent) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: AsyncChatEvent) {
                handler.invoke(
                    TBChatMessageEvent(
                        (e.player.displayName() as TextComponent).content(),
                        e.message(),
                    )
                )
            }
        }, plugin)
    }

    override fun broadcastMessage(text: Component) {
        plugin.server.broadcast(text)
    }
}
