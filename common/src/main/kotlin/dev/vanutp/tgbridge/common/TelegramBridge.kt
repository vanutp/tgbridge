package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.models.LastMessage
import dev.vanutp.tgbridge.common.models.LastMessageType
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import java.time.Clock
import java.time.temporal.ChronoUnit


abstract class TelegramBridge {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    protected abstract val logger: AbstractLogger
    protected abstract val platform: Platform
    private var initialized: Boolean = false
    private lateinit var bot: TelegramBot

    private var lastMessage: LastMessage? = null
    private val lastMessageLock = Mutex()

    fun init() {
        logger.info("tgbridge starting on ${platform.name}")
        try {
            ConfigManager.init(platform.configDir, platform::getLanguageKey)
        } catch (_: DefaultConfigUnchangedException) {
            logger.error("Can't start with default config values: please fill in botToken and chatId")
            return
        }
        bot = TelegramBot(config.botToken, logger)

        runBlocking {
            bot.init()
            sendMessage(lang.telegram.serverStarted)
        }
        registerTelegramHandlers()
        registerMinecraftHandlers()
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
        }
        initialized = true
    }

    fun shutdown() {
        if (!initialized) {
            return
        }
        runBlocking {
            sendMessage(lang.telegram.serverStopped)
            bot.shutdown()
        }
        coroutineScope.cancel()
    }

    private fun registerTelegramHandlers() {
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val onlinePlayerNames = platform.getOnlinePlayerNames()
        if (onlinePlayerNames.isNotEmpty()) {
            sendMessage(
                lang.telegram.playerList.formatLang(
                    "count" to onlinePlayerNames.size.toString(),
                    "usernames" to onlinePlayerNames.joinToString(),
                )
            )
        } else {
            sendMessage(lang.telegram.playerListZeroOnline)
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.id != config.chatId || config.topicId != null && msg.messageThreadId != config.topicId) {
            return
        }
        lastMessageLock.withLock {
            lastMessage = null
        }
        platform.broadcastMessage(msg.toMinecraft(bot.me.id))
    }

    private fun registerMinecraftHandlers() {
        platform.registerCommand(arrayOf("tgbridge", "reload"), this::onReloadCommand)
        platform.registerChatMessageListener(this::onChatMessage)
        platform.registerPlayerDeathListener(this::onPlayerDeath)
        platform.registerPlayerJoinListener(this::onPlayerJoin)
        platform.registerPlayerLeaveListener(this::onPlayerLeave)
        platform.registerPlayerAdvancementListener(this::onPlayerAdvancement)
    }

    private fun onReloadCommand(ctx: TBCommandContext): Boolean {
        try {
            ConfigManager.reload()
        } catch (e: Exception) {
            ctx.reply("Error reloading config: " + (e.message ?: e.javaClass.name))
            return false
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        return true
    }

    private fun onChatMessage(e: TBPlayerEventData) = withScopeAndLock {
        val rawMinecraftText = (e.text as TextComponent).content()
        val escapedText = rawMinecraftText.escapeHTML()
        val bluemapLink = rawMinecraftText.asBluemapLinkOrNone()
        if (bluemapLink == null && !rawMinecraftText.startsWith(config.requirePrefixInMinecraft ?: "")) {
            return@withScopeAndLock
        }

        val currText = lang.telegram.chatMessage.formatLang(
            "username" to e.username,
            "text" to (bluemapLink ?: escapedText),
        )
        val currDate = Clock.systemUTC().instant()

        val lm = lastMessage
        if (lm != null && lm.type == LastMessageType.TEXT && (lm.text + "\n" + currText).length <= 4000 && currDate.minus(
                (config.messageMergeWindow ?: 0).toLong(),
                ChronoUnit.SECONDS
            ) < lm.date
        ) {
            lm.text += "\n" + currText
            lm.date = currDate
            editMessageText(lm.id, lm.text!!)
        } else {
            val newMsg = sendMessage(currText)
            lastMessage = LastMessage(LastMessageType.TEXT, newMsg.messageId, currDate, text = currText)
        }
    }

    private fun onPlayerDeath(e: TBPlayerEventData) = withScopeAndLock {
        val component = e.text as TranslatableComponent
        sendMessage(lang.telegram.playerDied.formatLang("deathMessage" to component.translate().escapeHTML()))
        lastMessage = null
    }

    private fun onPlayerJoin(e: TBPlayerEventData) = withScopeAndLock {
        val lm = lastMessage
        val currDate = Clock.systemUTC().instant()
        if (lm != null && lm.type == LastMessageType.LEAVE && lm.leftPlayer!! == e.username && currDate.minus(
                (config.leaveJoinMergeWindow ?: 0).toLong(), ChronoUnit.SECONDS
            ) < lm.date
        ) {
            deleteMessage(lm.id)
        } else {
            sendMessage(lang.telegram.playerJoined.formatLang("username" to e.username))
        }
        lastMessage = null
    }

    private fun onPlayerLeave(e: TBPlayerEventData) = withScopeAndLock {
        val newMsg = sendMessage(lang.telegram.playerLeft.formatLang("username" to e.username))
        lastMessage = LastMessage(
            LastMessageType.LEAVE, newMsg.messageId, Clock.systemUTC().instant(), leftPlayer = e.username
        )
    }

    private fun onPlayerAdvancement(e: TBPlayerEventData) = withScopeAndLock {
        val component = e.text as TranslatableComponent
        val advancementTypeKey = component.key()
        val squareBracketsComponent = component.args()[1] as TranslatableComponent
        val advancementNameComponent = squareBracketsComponent.args()[0] as TranslatableComponent
        val advancementName = advancementNameComponent.translate()
        val advancementDescription = advancementNameComponent.style().hoverEvent()?.let {
            val advancementTooltipComponent = it.value() as TranslatableComponent
            if (advancementTooltipComponent.children().size < 2) {
                return@let null
            }
            (advancementTooltipComponent.children()[1] as TranslatableComponent).translate()
        } ?: ""
        val langKey = when (advancementTypeKey) {
            "chat.type.advancement.task" -> lang.telegram.advancements.regular
            "chat.type.advancement.goal" -> lang.telegram.advancements.goal
            "chat.type.advancement.challenge" -> lang.telegram.advancements.challenge
            else -> throw TBAssertionFailed("Unknown advancement type $advancementTypeKey.")
        }
        sendMessage(
            langKey.formatLang(
                "username" to e.username,
                "title" to advancementName.escapeHTML(),
                "description" to advancementDescription.escapeHTML(),
            )
        )
        lastMessage = null
    }

    private fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    private suspend fun sendMessage(text: String): TgMessage {
        return bot.sendMessage(config.chatId, text, replyToMessageId = config.topicId)
    }

    private suspend fun editMessageText(messageId: Int, text: String): TgMessage {
        return bot.editMessageText(config.chatId, messageId, text)
    }

    private suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(config.chatId, messageId)
    }
}
