package dev.vanutp.tgbridge.forge

import com.google.gson.Gson
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import net.minecraft.client.resource.language.TranslationStorage
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.loading.FMLPaths
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Mod(ForgeTelegramBridge.MOD_ID)
object ForgeTelegramBridge : TelegramBridge() {
    const val MOD_ID = "tgbridge"
    override val logger = ForgeLogger()
    override val platform = ForgePlatform()

    init {
        runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
            },
            serverTarget = {
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
        ctx.source.sendFeedback(Text.literal("minecraft_lang.json created"), false)
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

}
