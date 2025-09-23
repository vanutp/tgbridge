package dev.vanutp.tgbridge.forge

import com.google.gson.Gson
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.forge.compat.IncompatibleChatModCompat
import net.minecraft.client.resource.language.TranslationStorage
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Mod(NeoForgeTelegramBridge.MOD_ID)
object NeoForgeTelegramBridge : TelegramBridge() {
    const val MOD_ID = "tgbridge"
    override val logger = NeoForgeLogger()
    override val platform = NeoForgePlatform()
    val registryManager: RegistryWrapper.WrapperLookup by lazy {
        ServerLifecycleHooks.getCurrentServer()!!.registryManager
    }

    init {
        runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
            },
            serverTarget = {
                addIntegration(IncompatibleChatModCompat(this))
                EventManager.register()
                FORGE_BUS.addListener { _: ServerStartingEvent ->
                    init()
                }
                FORGE_BUS.addListener { _: ServerStartedEvent ->
                    onServerStarted()
                }
                FORGE_BUS.addListener { _: ServerStoppingEvent ->
                    shutdown()
                }
            }
        )
    }

    private fun onDumpLangCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID)
        configDir.createDirectories()
        val minecraftLangFile = configDir.resolve("minecraft_lang.json")
        val translations = (Language.getInstance() as TranslationStorage).translations
        minecraftLangFile.writeText(Gson().toJson(translations))
        ctx.source.sendFeedback({ Text.literal("minecraft_lang.json created") }, false)
        return 1
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        FORGE_BUS.addListener { e: RegisterClientCommandsEvent ->
            e.dispatcher.register(
                LiteralArgumentBuilder.literal<ServerCommandSource>("tgbridge")
                    .then(
                        LiteralArgumentBuilder.literal<ServerCommandSource>("dump_lang")
                            .executes(::onDumpLangCommand)
                    )
                    .then(
                        LiteralArgumentBuilder.literal("toggle")
                    )
            )
        }
    }
}
