@file:OptIn(RiskFeature::class)

package dev.vanutp.tgbridge.common

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.caption
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.sender_chat
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor


abstract class TelegramBridge {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    protected abstract val logger: AbstractLogger
    protected abstract val platform: Platform
    private val config by lazy { TBConfig.load(platform.configDir.resolve("config.json")) }
    private lateinit var bot: TelegramBot
    private lateinit var pollJob: Job

    fun init() {
        logger.info("tgbridge starting on ${platform.name}")
        if (config.botToken == TBConfig().botToken || config.chatId == TBConfig().chatId) {
            logger.error("Can't start with default config values: please fill in botToken and chatId")
            return
        }
        coroutineScope.launch {
            telegramBotWithBehaviourAndLongPolling(config.botToken, coroutineScope) {
                onContentMessage {
                    val senderName =
                        if (it.from != null)
                            (it.from!!.firstName + " " + it.from!!.lastName).trim()
                        else
                            it.sender_chat?.title ?: ""
                    platform.broadcastMessage(
                        Component.text()
                            .append(Component.text("[${senderName}]", NamedTextColor.AQUA))
                            .append(Component.text(" "))
                            .append(Component.text(it.text ?: it.caption ?: ""))
                            .build()
                    )
                }
            }.let {
                bot = it.first
                pollJob = it.second
            }
        }
        platform.registerChatMessageListener { e ->
            coroutineScope.launch {
                val text = (e.text as TextComponent).content()
                bot.sendMessage(
                    ChatId(config.chatId),
                    "<b>[${e.username}]</b> ${text.escapeHTML()}",
                    parseMode = HTMLParseMode,
                )
            }
        }
    }

    fun shutdown() {
        pollJob.cancel()
    }
}
