package dev.vanutp.tgbridge.fabric

import com.google.gson.JsonElement
import dev.vanutp.tgbridge.common.integration.PlaceholderAPI
import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.integration.StyledChat
import dev.vanutp.tgbridge.common.integration.Vanish
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.fabric.integration.FabricPlaceholderAPI
import dev.vanutp.tgbridge.fabric.integration.FabricStyledChat
import dev.vanutp.tgbridge.fabric.integration.FabricVanish
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.message.SignedMessage
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Language
import kotlin.jvm.optionals.getOrNull


class FabricPlatform(val server: MinecraftServer) : Platform() {
    override val name = "fabric"
    override val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)
    override val placeholderAPIInstance: PlaceholderAPI? = if (FabricLoader.getInstance().isModLoaded("placeholder-api")) FabricPlaceholderAPI else null
    override val styledChatInstance: StyledChat? = if (FabricLoader.getInstance().isModLoaded("styledchat")) FabricStyledChat else null
    override val vanishInstance: Vanish? = if (FabricLoader.getInstance().isModLoaded("melius-vanish")) FabricVanish else null
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

    companion object {
        var instance: FabricPlatform? = null
        fun setInstance(server: MinecraftServer): FabricPlatform {
            instance = FabricPlatform(server)
            return instance!!
        }
    }
    fun adventureToMinecraft(adventure: Component): Text {
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

    fun minecraftToAdventure(minecraft: Text): Component {
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
        /*if (styledChatInstance == null)*/ ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, _ ->
            if (sender is ServerPlayerEntity && (vanishInstance == null || !vanishInstance.isVanished(sender))) {
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
//        else styledChatInstance.registerMessageEvent(handler)
    }

    override fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        CustomEvents.PLAYER_DEATH_EVENT.register { player, damageSource ->
            if (vanishInstance == null || !vanishInstance.isVanished(player)) {
                val deathMessage = damageSource.getDeathMessage(player)
                handler(
                    TBPlayerEventData(
                        player.displayName?.string ?: return@register,
                        minecraftToAdventure(deathMessage),
                    )
                )
            }
        }
    }

    override fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        CustomEvents.PLAYER_JOIN_EVENT.register { player ->
            if (vanishInstance == null || !vanishInstance.isVanished(player))handler.invoke(
                TBPlayerEventData(
                    player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        vanishInstance?.registerOnJoinMessage(handler)
    }

    override fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        CustomEvents.PLAYER_JOIN_EVENT.register { player ->
            if (vanishInstance == null || !vanishInstance.isVanished(player))handler.invoke(
                TBPlayerEventData(
                    player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        vanishInstance?.registerOnLeaveMessage(handler)
    }

    override fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        CustomEvents.ADVANCEMENT_EARN_EVENT.register { player, advancementType, advancementNameComponent ->
            if (vanishInstance == null || !vanishInstance.isVanished(player)) {
                if (player.displayName == null) {
                    return@register
                }
                val advancementTypeKey = "chat.type.advancement.$advancementType"
                val advancementText =
                    Text.translatable(advancementTypeKey, player.displayName, advancementNameComponent)
                handler(
                    TBPlayerEventData(
                        player.displayName!!.string,
                        minecraftToAdventure(advancementText)
                    )
                )
            }
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
