package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.entity.player.PlayerEvent
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
            handler(
                TBPlayerEventData(
                    e.player.displayName.string,
                    minecraftToAdventure(e.message)
                )
            )
        }
    }

    override fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val display = e.advancement.display
            if (display == null || !display.shouldAnnounceToChat()) {
                return@addListener
            }
            val advancementTypeKey = "chat.type.advancement." + (display.frame?.id ?: return@addListener)
            val advancementText =
                Text.translatable(advancementTypeKey, e.entity.displayName, e.advancement.toHoverableText())
            handler(
                TBPlayerEventData(
                    e.entity.displayName.string,
                    minecraftToAdventure(advancementText)
                )
            )
        }
    }

    override fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: LivingDeathEvent ->
            if (e.entity !is PlayerEntity) {
                return@addListener
            }
            val deathMessage = e.source.getDeathMessage(e.entity)
            handler(
                TBPlayerEventData(
                    e.entity.displayName.string,
                    minecraftToAdventure(deathMessage),
                )
            )
        }
    }

    override fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            handler(TBPlayerEventData(e.entity.displayName.string, Component.text("")))
        }
    }

    override fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            handler(TBPlayerEventData(e.entity.displayName.string, Component.text("")))
        }
    }

    override fun registerCommand(command: Array<String>, handler: (TBCommandContext) -> Boolean) {
        // TODO: get rid of code duplication between versions and loaders
        val builder = CommandManager.literal(command[0])
        var lastArg = builder
        command.drop(1).forEachIndexed { i, x ->
            val newArg = CommandManager.literal(x)
            if (i == command.size - 2) {
                newArg.executes { ctx ->
                    val res = handler(TBCommandContext(
                        reply = { text ->
                            ctx.source.sendFeedback({ Text.literal(text) }, false)
                        }
                    ))
                    return@executes if (res) 1 else -1
                }
            }
            lastArg.then(newArg)
            lastArg = newArg
        }
        ServerLifecycleHooks.getCurrentServer().commandManager.dispatcher.register(builder)
    }

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer().playerManager.broadcast(adventureToMinecraft(text), false)
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
