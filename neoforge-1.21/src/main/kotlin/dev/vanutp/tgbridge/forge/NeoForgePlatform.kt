package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.AdvancementEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import kotlin.jvm.optionals.getOrNull

class NeoForgePlatform : Platform() {
    override val name = "forge"
    override val configDir = FMLPaths.CONFIGDIR.get().resolve(NeoForgeTelegramBridge.MOD_ID)
    override val placeholderAPIInstance = null
    override val styledChatInstance = null
    override val vanishInstance = null

    private fun adventureToMinecraft(adventure: Component): Text {
        return Text.Serialization.fromJsonTree(
            GsonComponentSerializer.gson().serializeToTree(adventure),
            DynamicRegistryManager.of(Registries.REGISTRIES)
        )!!
    }

    private fun minecraftToAdventure(minecraft: Text): Component {
        return GsonComponentSerializer.gson().deserialize(
            Text.Serialization.toJsonString(
                minecraft,
                DynamicRegistryManager.of(Registries.REGISTRIES)
            )
        )
    }

    override fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: ServerChatEvent ->
            handler(
                TBPlayerEventData(
                    e.player.displayName?.string ?: return@addListener,
                    minecraftToAdventure(e.message)
                )
            )
        }
    }

    override fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val advancement = e.advancement.value
            val display = advancement.display.getOrNull()
            if (display == null || !display.shouldAnnounceToChat() || e.entity.displayName == null) {
                return@addListener
            }
            val advancementTypeKey = "chat.type.advancement." + (display.frame?.name?.lowercase() ?: return@addListener)
            val advancementText =
                Text.translatable(advancementTypeKey, e.entity.displayName, advancement.name.get())
            handler(
                TBPlayerEventData(
                    e.entity.displayName!!.string,
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
                    e.entity.displayName?.string ?: return@addListener,
                    minecraftToAdventure(deathMessage),
                )
            )
        }
    }

    override fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            handler(
                TBPlayerEventData(
                    e.entity.displayName?.string ?: return@addListener,
                    Component.text(""),
                )
            )
        }
    }

    override fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            handler(
                TBPlayerEventData(
                    e.entity.displayName?.string ?: return@addListener,
                    Component.text(""),
                )
            )
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
        ServerLifecycleHooks.getCurrentServer()!!.commandManager.dispatcher.register(builder)
    }

    override fun broadcastMessage(text: Component) {
        ServerLifecycleHooks.getCurrentServer()!!.playerManager.broadcast(adventureToMinecraft(text), false)
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return ServerLifecycleHooks.getCurrentServer()!!.playerNames
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            get(key)
        } else {
            null
        }
    }
}
