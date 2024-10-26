package dev.vanutp.tgbridge.fabric

import com.google.gson.JsonElement
import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.message.SignedMessage
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Language


class FabricPlatform(private val server: MinecraftServer) : Platform() {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)
    private val IS_192 = arrayOf("1.19", "1.19.1", "1.19.2").contains(server.version)
    private val IS_19 = arrayOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4").contains(server.version)
    private val IS_19_204 = arrayOf(
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "1.20",
        "1.20.1",
        "1.20.2",
        "1.20.3",
        "1.20.4"
    ).contains(server.version)

    private fun adventureToMinecraft(adventure: Component): Text {
        val serializedTree = GsonComponentSerializer.gson().serializeToTree(adventure)

        return if (IS_19_204) {
            // net.minecraft.text.Text$Serializer
            val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
            // called fromJson on older versions
            val fromJsonTree = textCls.getMethod("method_10872", JsonElement::class.java)
            fromJsonTree.invoke(null, serializedTree) as Text
        } else {
            // 1.20.5+
            Text.Serialization.fromJsonTree(
                serializedTree,
                DynamicRegistryManager.of(Registries.REGISTRIES)
            )!!
        }
    }

    private fun minecraftToAdventure(minecraft: Text): Component {
        val jsonString = if (IS_19_204) {
            // net.minecraft.text.Text$Serializer
            val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
            // called toJson on older versions
            val toJsonString = textCls.getMethod("method_10867", Text::class.java)
            toJsonString.invoke(null, minecraft) as String
        } else {
            // 1.20.5+
            Text.Serialization.toJsonString(
                minecraft,
                DynamicRegistryManager.of(Registries.REGISTRIES)
            )
        }

        return GsonComponentSerializer.gson().deserialize(jsonString)
    }

    override fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit) {
        ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, _ ->
            val messageContent = if (IS_192) {
                val cls = message.javaClass
                val getContent = cls.getMethod("method_44125")
                getContent.invoke(message) as Text
            } else {
                message.content
            }
            handler.invoke(
                TBPlayerEventData(
                    sender.displayName?.string ?: return@register,
                    minecraftToAdventure(messageContent),
                )
            )
        }
    }

    private fun registerFilteredPlayerEvent(
        handler: (TBPlayerEventData) -> Unit,
        filter: (TranslatableTextContent) -> Boolean
    ) {
        ServerMessageEvents.GAME_MESSAGE.register { _, text, _ ->
            val content = text.content
            if (content is TranslatableTextContent && filter(content)) {
                handler(
                    TBPlayerEventData((content.args[0] as Text).string, minecraftToAdventure(text))
                )
            }
        }
    }

    override fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        registerFilteredPlayerEvent(handler) { it.key.startsWith("death.") }
    }

    override fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        registerFilteredPlayerEvent(handler) { it.key == "multiplayer.player.joined" }
    }

    override fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        registerFilteredPlayerEvent(handler) { it.key == "multiplayer.player.left" }
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
                            val textComponent = Text.literal(text)
                            if (IS_19) {
                                val cls = ctx.source.javaClass
                                val sendFeedback = cls.getMethod(
                                    "method_9226",
                                    Text::class.java,
                                    Boolean::class.javaPrimitiveType
                                )
                                sendFeedback.invoke(ctx.source, textComponent, false)
                            } else {
                                ctx.source.sendFeedback({ textComponent }, false)
                            }
                        }
                    ))
                    return@executes if (res) 1 else -1
                }
            }
            lastArg.then(newArg)
            lastArg = newArg
        }
        server.commandManager.dispatcher.register(builder)
    }

    override fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        registerFilteredPlayerEvent(handler) { it.key.startsWith("chat.type.advancement.") }
    }

    override fun broadcastMessage(text: Component) {
        server.playerManager.broadcast(adventureToMinecraft(text), false)
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return server.playerNames
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (hasTranslation(key)) {
            if (IS_192) {
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
