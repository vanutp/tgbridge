package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.compat.*
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.converters.TelegramToMinecraftConverter
import dev.vanutp.tgbridge.common.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.time.Clock
import java.time.temporal.ChronoUnit
import kotlin.math.max


abstract class TelegramBridge {
    private val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())
    protected abstract val logger: ILogger
    abstract val platform: IPlatform
    private var initialized: Boolean = false
    private var started: Boolean = false
    private lateinit var bot: TelegramBot
    private var spark: SparkHelper? = null

    private var lastMessage: LastMessage? = null
    private val lastMessageLock = Mutex()

    private val availableIntegrations: MutableList<AbstractCompat> = mutableListOf()
    lateinit var loadedIntegrations: List<AbstractCompat> private set

    companion object {
        private var _INSTANCE: TelegramBridge? = null
        val INSTANCE: TelegramBridge
            get() = _INSTANCE ?: throw UninitializedPropertyAccessException("TelegramBridge is not initialized yet")
    }

    init {
        if (_INSTANCE != null) {
            throw IllegalStateException("TelegramBridge is already initialized")
        }
        _INSTANCE = this
    }

    fun init() {
        logger.info("tgbridge starting on ${platform.name}")
        ConfigManager.init(platform.configDir, platform::getLanguageKey)
        if (config.hasDefaultValues()) {
            logger.warn("Can't start with default config values: please fill in botToken and chatId, then run /tgbridge reload")
            return
        }
        bot = TelegramBot(config.advanced.botApiUrl, config.general.botToken, logger)

        runBlocking {
            bot.init()
        }
        registerTelegramHandlers()
        bot.startPolling(coroutineScope)
        initialized = true
    }

    fun addIntegration(integration: AbstractCompat) {
        if (started) {
            logger.error("Can't add integration ${integration::class.simpleName} after the server has started")
            return
        }
        availableIntegrations.add(integration)
    }

    fun onServerStarted() {
        loadedIntegrations = availableIntegrations.filter { it.shouldEnable() }
        for (integration in loadedIntegrations) {
            logger.info("Using ${integration::class.simpleName}")
            integration.enable()
        }
        // Doing this here to ensure spark is loaded
        spark = SparkHelper.createOrNull()
        if (config.events.enableStartMessages) {
            coroutineScope.launch {
                sendMessage(lang.telegram.serverStarted)
            }
        }
        started = true
    }

    fun shutdown() {
        if (!initialized) {
            return
        }
        runBlocking {
            if (config.events.enableStopMessages) {
                sendMessage(lang.telegram.serverStopped)
            }
            bot.shutdown()
        }
        coroutineScope.cancel()
    }

    private fun registerTelegramHandlers() {
        bot.registerCommandHandler("tps", this::onTelegramTpsCommand)
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private fun checkMessageChat(msg: TgMessage): Boolean {
        val isTopicMessage = msg.isTopicMessage ?: false
        val messageThreadId = if (isTopicMessage) {
            msg.messageThreadId!!
        } else {
            // Replies in "General" topic have threadId set to the reply message id
            1
        }
        val chatValid = msg.chat.id == config.general.chatId
        val topicValid = config.general.topicId == null || messageThreadId == config.general.topicId
        return chatValid && topicValid
    }

    private suspend fun onTelegramTpsCommand(msg: TgMessage) {
        val spark = spark
        if (!checkMessageChat(msg) || spark == null) {
            return
        }
        val durations = spark.getPlaceholders()
        if (durations == null) {
            logger.error("Unable to get spark data")
            return
        }
        sendMessage(lang.telegram.tps.formatLang(*durations))
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        if (!checkMessageChat(msg)) {
            return
        }
        val onlinePlayers = platform.getOnlinePlayers().filterNot { it.isVanished() }
        if (onlinePlayers.isNotEmpty()) {
            sendMessage(
                lang.telegram.playerList.formatLang(
                    "count" to onlinePlayers.size.toString(),
                    "usernames" to onlinePlayers.joinToString { it.getName() },
                )
            )
        } else {
            sendMessage(lang.telegram.playerListZeroOnline)
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        if (!checkMessageChat(msg)) {
            return
        }
        lastMessageLock.withLock {
            lastMessage = null
        }
        if (TgbridgeEvents.TG_CHAT_MESSAGE.invoke(msg) == EventResult.STOP) return
        platform.broadcastMessage(TelegramToMinecraftConverter.convert(msg, bot.me.id))
    }

    private fun tryReinit(ctx: TBCommandContext): Boolean {
        init()
        if (initialized) {
            onServerStarted()
            ctx.reply("tgbridge initialized")
        } else if (config.hasDefaultValues()) {
            ctx.reply("Config still has default values: please fill in botToken and chatId")
        } else {
            ctx.reply("Error initializing the mod, check the server console for errors")
        }
        return initialized
    }

    fun onReloadCommand(ctx: TBCommandContext): Boolean {
        if (!initialized) {
            try {
                if (config.hasDefaultValues()) {
                    return tryReinit(ctx)
                }
            } catch (_: UninitializedPropertyAccessException) {
            }
            ctx.reply("Mod was not properly initialized, please restart the server")
            return false
        }
        try {
            ConfigManager.reload()
        } catch (e: Exception) {
            ctx.reply("Error reloading config: " + (e.message ?: e.javaClass.name))
            return false
        }
        runBlocking {
            bot.recoverPolling(coroutineScope)
            if (lastMessageLock.isLocked) {
                lastMessageLock.unlock()
            }
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        coroutineScope.launch {
            TgbridgeEvents.CONFIG_RELOAD.invoke(Unit)
        }
        return true
    }

    fun onChatMessage(e: TgbridgeMcChatMessageEvent) = wrapMinecraftHandler {
        if (TgbridgeEvents.MC_CHAT_MESSAGE.invoke(e) == EventResult.STOP) return@wrapMinecraftHandler
        var telegramText = MinecraftToTelegramConverter.convert(e.message)
        val bluemapLink = telegramText.text.asBluemapLinkOrNone()
        val prefix = config.integrations.incompatiblePluginChatPrefix
            ?: config.messages.requirePrefixInMinecraft
            ?: ""
        if (bluemapLink == null && !telegramText.text.startsWith(prefix)) {
            return@wrapMinecraftHandler
        }

        if (bluemapLink != null) {
            telegramText = bluemapLink
        } else if (!config.messages.keepPrefix) {
            telegramText = TelegramFormattedText(
                telegramText.text.removePrefix(prefix),
                telegramText.entities.map { it.copy(offset = max(it.offset - prefix.length, 0)) },
            )
        }

        val tgPrefix = MinecraftToTelegramConverter.convert(
            lang.telegram.chatMessage.formatMiniMessage(listOf("username" to e.sender.getName()))
                .append(Component.text(" "))
        )

        val currText = tgPrefix + telegramText
        val currDate = Clock.systemUTC().instant()

        val lm = lastMessage
        if (
            lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text!! + "\n" + currText).text.length <= 4000
            && currDate.minus((config.messages.mergeWindow).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text = lm.text!! + "\n" + currText
            lm.date = currDate
            editMessageText(lm.id, lm.text!!.text, lm.text!!.entities)
        } else {
            val newMsg = sendMessage(currText.text, currText.entities)
            lastMessage = LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText
            )
        }
    }

    fun onPlayerDeath(e: TgbridgeDeathEvent) = wrapMinecraftHandler {
        if (!config.events.enableDeathMessages) {
            return@wrapMinecraftHandler
        }
        if (TgbridgeEvents.DEATH.invoke(e) == EventResult.STOP) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler
        val convertedMessage = when (val msg = e.message) {
            is TranslatableComponent -> {
                val args = mutableListOf<Component>(Component.text(e.player.getName()))
                args.addAll(msg.arguments().map { it.asComponent() }.drop(1))
                msg.arguments(args)
            }

            null -> Component.translatable("death.attack.generic", Component.text(e.player.getName()))
            else -> msg
        }
        val telegramText = MinecraftToTelegramConverter.convert(
            lang.telegram.playerDied.formatMiniMessage(
                listOf(),
                listOf("death_message" to convertedMessage),
            )
        )
        sendMessage(telegramText.text, telegramText.entities)
        lastMessage = null
    }

    fun onPlayerJoin(e: TgbridgeJoinEvent) = wrapMinecraftHandler {
        if (!config.events.enableJoinMessages) {
            return@wrapMinecraftHandler
        }

        if (TgbridgeEvents.JOIN.invoke(e) == EventResult.STOP) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler
        val lm = lastMessage
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == e.player.uuid
            && currDate.minus((config.events.leaveJoinMergeWindow).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            deleteMessage(lm.id)
        } else {
            val message = if (e.hasPlayedBefore) {
                lang.telegram.playerJoined
            } else {
                lang.telegram.playerJoinedFirstTime
            }
            sendMessage(message.formatLang("username" to e.player.getName()))
        }
        lastMessage = null
    }

    fun onPlayerLeave(e: TgbridgeLeaveEvent) = wrapMinecraftHandler {
        if (!config.events.enableLeaveMessages) {
            return@wrapMinecraftHandler
        }
        if (TgbridgeEvents.LEAVE.invoke(e) == EventResult.STOP) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler
        val newMsg = sendMessage(lang.telegram.playerLeft.formatLang("username" to e.player.getName()))
        lastMessage = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = e.player.uuid
        )
    }

    fun onPlayerAdvancement(e: TgbridgeAdvancementEvent) = wrapMinecraftHandler {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) {
            return@wrapMinecraftHandler
        }
        if (TgbridgeEvents.ADVANCEMENT.invoke(e) == EventResult.STOP) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler
        val advancementName = e.title.asString()
        val advancementDescription = if (advancementsCfg.showDescription) {
            e.description.asString()
        } else {
            ""
        }
        val langKey = when (e.type) {
            "task" -> {
                if (!advancementsCfg.enableTask) return@wrapMinecraftHandler
                lang.telegram.advancements.regular
            }

            "goal" -> {
                if (!advancementsCfg.enableGoal) return@wrapMinecraftHandler
                lang.telegram.advancements.goal
            }

            "challenge" -> {
                if (!advancementsCfg.enableChallenge) return@wrapMinecraftHandler
                lang.telegram.advancements.challenge
            }

            else -> throw TBAssertionFailed("Unknown advancement type ${e.type}.")
        }
        sendMessage(
            langKey.formatLang(
                "username" to e.player.getName(),
                "title" to advancementName.escapeHTML(),
                "description" to advancementDescription.escapeHTML(),
            )
        )
        lastMessage = null
    }

    private fun wrapMinecraftHandler(fn: suspend () -> Unit) {
        if (!initialized) {
            return
        }
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    private suspend fun sendMessage(text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            config.general.chatId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
            replyToMessageId = config.general.topicId,
        )
    }

    private suspend fun editMessageText(messageId: Int, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.editMessageText(
            config.general.chatId,
            messageId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
        )
    }

    private suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(config.general.chatId, messageId)
    }
}
