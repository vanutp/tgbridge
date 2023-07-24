package dev.vanutp.tgbridge.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor


abstract class TelegramBridge {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    protected abstract val logger: AbstractLogger
    protected abstract val platform: Platform
    private val config by lazy { TBConfig.load(platform.configDir.resolve("config.json")) }
    private val bot by lazy { TelegramBot(config, logger) }

    fun init() {
        logger.info("tgbridge starting on ${platform.name}")
        if (config.botToken == TBConfig().botToken || config.chatId == TBConfig().chatId) {
            logger.error("Can't start with default config values: please fill in botToken and chatId")
            return
        }
        bot.registerMessageHandler { msg ->
            if (msg.chat.id != config.chatId) {
                return@registerMessageHandler
            }
            val senderName = msg.from?.let { _ ->
                (msg.from.firstName + " " + (msg.from.lastName ?: "")).trim()
            } ?: msg.senderChat?.title ?: ""
            val components = mutableListOf<Component>()

            components.add(Component.text("<${senderName}>", NamedTextColor.AQUA))

            val forwardFromName = msg.forwardFrom?.let { _ ->
                (msg.forwardFrom.firstName + " " + (msg.forwardFrom.lastName ?: "")).trim()
            } ?: msg.forwardFromChat?.let {
                msg.forwardFromChat.title
            }

            forwardFromName?.let {
                components.add(Component.text("[Forwarded from $it]", NamedTextColor.BLUE))
            }

            msg.animation?.let {
                components.add(Component.text("[GIF]", NamedTextColor.GREEN))
            } ?: msg.document?.let {
                components.add(Component.text("[Document]", NamedTextColor.GREEN))
            }
            msg.photo?.let {
                components.add(Component.text("[Photo]", NamedTextColor.GREEN))
            }
            msg.audio?.let {
                components.add(Component.text("[Audio]", NamedTextColor.GREEN))
            }
            msg.sticker?.let {
                components.add(Component.text("[Sticker]", NamedTextColor.GREEN))
            }
            msg.video?.let {
                components.add(Component.text("[Video]", NamedTextColor.GREEN))
            }
            msg.videoNote?.let {
                components.add(Component.text("[Video message]", NamedTextColor.GREEN))
            }
            msg.voice?.let {
                components.add(Component.text("[Voice message]", NamedTextColor.GREEN))
            }
            msg.poll?.let {
                components.add(Component.text("[Poll]", NamedTextColor.GREEN))
            }

            components.add(Component.text(msg.text ?: msg.caption ?: ""))


            platform.broadcastMessage(
                components
                    .flatMap { listOf(it, Component.text(" ")) }
                    .fold(Component.text()) { acc, component -> acc.append(component) }
                    .build()
            )
        }
        coroutineScope.launch {
            bot.startPolling()
        }
        platform.registerChatMessageListener { e ->
            coroutineScope.launch {
                val text = (e.text as TextComponent).content()
                if (!text.startsWith(config.requirePrefixInMinecraft)) {
                    return@launch
                }
                bot.sendMessage(
                    config.chatId,
                    "<b>[${e.username}]</b> ${text.escapeHTML()}",
                )
            }
        }
//        platform.registerPlayerDeathListener { e ->
//
//        }
    }

    fun shutdown() {
        coroutineScope.launch {
            bot.stopPolling()
        }
    }
}
