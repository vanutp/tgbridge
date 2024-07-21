package dev.vanutp.tgbridge.forge

import com.google.gson.Gson
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.client.resource.language.TranslationStorage
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.loading.FMLPaths
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Mod(NeoForgeTelegramBridge.MOD_ID)
object NeoForgeBootstrap {
    private val bridge = NeoForgeTelegramBridge()

    init {
        runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
            },
            serverTarget = {
                FORGE_BUS.addListener(::onServerStartup)
                FORGE_BUS.addListener(::onServerShutdown)
            }
        )
    }

    private fun onDumpLangCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val configDir = FMLPaths.CONFIGDIR.get().resolve(NeoForgeTelegramBridge.MOD_ID)
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
                LiteralArgumentBuilder.literal<ServerCommandSource>("tgbridge").then(
                    LiteralArgumentBuilder.literal<ServerCommandSource>("dump_lang").executes(::onDumpLangCommand)
                )
            )
        }
    }

    private fun onServerStartup(event: ServerStartingEvent) {
        bridge.init()
    }

    private fun onServerShutdown(event: ServerStoppingEvent) {
        bridge.shutdown()
    }
}
