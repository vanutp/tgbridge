package dev.vanutp.tgbridge.forge

import com.google.gson.Gson
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.forge.modules.IncompatibleChatModModule
import net.minecraft.client.resources.language.ClientLanguage
import net.minecraft.commands.CommandSourceStack
import net.minecraft.locale.Language
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import net.minecraft.network.chat.Component as Text

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
                EVENT_BUS.addListener { _: ServerStartedEvent ->
                    onServerStarted()
                }
                EVENT_BUS.addListener { _: ServerStoppingEvent ->
                    shutdown()
                }
            }

            else -> throw IllegalArgumentException("Unknown dist ${FMLEnvironment.dist}")
        }
    }

    private fun onDumpLangCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID)
        configDir.createDirectories()
        val minecraftLangFile = configDir.resolve("minecraft_lang.json")
        val translations = (Language.getInstance() as ClientLanguage).storage
        minecraftLangFile.writeText(Gson().toJson(translations))
        ctx.source.sendSuccess(Text.literal("minecraft_lang.json created"), false)
        return 1
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        EVENT_BUS.addListener { e: RegisterClientCommandsEvent ->
            e.dispatcher.register(
                LiteralArgumentBuilder.literal<CommandSourceStack>("tgbridge")
                    .then(
                        LiteralArgumentBuilder.literal<CommandSourceStack>("dump_lang")
                            .executes(::onDumpLangCommand)
                    )
                    .then(
                        LiteralArgumentBuilder.literal("toggle")
                    )
            )
        }
    }

}
