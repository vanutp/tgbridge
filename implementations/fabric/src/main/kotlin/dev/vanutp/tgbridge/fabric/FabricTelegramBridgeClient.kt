package dev.vanutp.tgbridge.fabric

import com.google.gson.Gson
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.resource.language.TranslationStorage
import net.minecraft.text.Text
import net.minecraft.util.Language
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

object FabricTelegramBridgeClient : ClientModInitializer {
    private fun onDumpLangCommand(ctx: CommandContext<FabricClientCommandSource>): Int {
        val configDir = FabricLoader.getInstance().configDir.resolve(FabricTelegramBridge.MOD_ID)
        configDir.createDirectories()
        val minecraftLangFile = configDir.resolve("minecraft_lang.json")
        val translations = (Language.getInstance() as TranslationStorage).translations
        minecraftLangFile.writeText(Gson().toJson(translations))
        ctx.source.sendFeedback(Text.literal("minecraft_lang.json created"))
        return 1
    }

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("tgbridge")
                    .then(
                        ClientCommandManager.literal("dump_lang")
                            .executes(::onDumpLangCommand)
                    )
                    .then(
                        ClientCommandManager.literal("toggle")
                    )
            )
        }
    }
}
