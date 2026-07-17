package dev.vanutp.tgbridge.forge

import com.google.gson.Gson
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.forge.modules.IncompatibleChatModModule
import net.minecraft.client.resource.language.TranslationStorage
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Language
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.server.FMLServerStartedEvent
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText


@Mod(ForgeTelegramBridge.MOD_ID)
class ForgeTelegramBridge : TelegramBridge() {
    companion object {
        const val MOD_ID = "tgbridge"
    }

    override val logger = ForgeLogger()
    override val platform = ForgePlatform()

    init {
        val modBus = FMLJavaModLoadingContext.get().modEventBus
        when (FMLEnvironment.dist) {
            Dist.CLIENT -> {
                modBus.addListener(::onClientSetup)
            }

            Dist.DEDICATED_SERVER -> {
                addModule(IncompatibleChatModModule(this))
                EventManager(this).register()
                init()
                EVENT_BUS.addListener { _: FMLServerStartedEvent ->
                    onServerStarted()
                }
                EVENT_BUS.addListener { _: FMLServerStoppingEvent ->
                    shutdown()
                }
            }

            else -> throw IllegalArgumentException("Unknown dist ${FMLEnvironment.dist}")
        }
    }

    private fun onDumpLangCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID)
        configDir.createDirectories()
        val minecraftLangFile = configDir.resolve("minecraft_lang.json")
        val translations = (Language.getInstance() as TranslationStorage).languageData
        minecraftLangFile.writeText(Gson().toJson(translations))
        ctx.source.sendFeedback(LiteralText("minecraft_lang.json created"), false)
        return 1
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        EVENT_BUS.addListener { e: RegisterCommandsEvent ->
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
