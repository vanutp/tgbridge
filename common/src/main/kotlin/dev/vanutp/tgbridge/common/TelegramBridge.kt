package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.converters.TelegramToMinecraftConverter
import dev.vanutp.tgbridge.common.models.*
import dev.vanutp.tgbridge.common.modules.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.time.Clock
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

abstract class TelegramBridge {
    internal val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())
    abstract val logger: ILogger
    abstract val platform: IPlatform
    private var initialized = CompletableDeferred<Unit>()
    private var started: Boolean = false
    lateinit var bot: TelegramBot private set
    private var spark: SparkHelper? = null

    val merger: MessageMerger = MessageMerger()

    protected val availableModules = mutableListOf<ITgbridgeModule>()
    private val _enabledModules = mutableListOf<ITgbridgeModule>()
    val enabledModules: List<ITgbridgeModule>
        get() = _enabledModules
    val chatModule: IChatModule?
        get() = _enabledModules.find { it is IChatModule } as? IChatModule

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
        MuteService.init(logger, platform.configDir)
        config.getError()?.let {
            logger.error(it)
            return
        }
        bot = TelegramBot(config.advanced.botApiUrl, config.botToken, logger, coroutineScope)
        addModule(ReplacementsModule(this))
        addModule(VoiceMessagesModule(this))
        coroutineScope.launch {
            bot.init()
            logger.info("Logged in as @${bot.me.username}")
            registerTelegramHandlers()
            bot.startPolling()
            initialized.complete(Unit)
        }
    }

    suspend fun waitForInit(): Boolean =
        try {
            withTimeout(5.seconds) {
                initialized.await()
            }
            true
        } catch (_: TimeoutCancellationException) {
            false
        }

    fun addModule(module: ITgbridgeModule) {
        if (started) {
            throw IllegalStateException("Can't add module ${module::class.simpleName} after the server has started")
        }
        if (availableModules.contains(module)) {
            throw IllegalStateException("Module ${module::class.simpleName} is already added")
        }
        availableModules.add(module)
    }

    private fun checkConflictingModules() {
        var hasChatModule = false
        var hasVanishModule = false
        for (module in enabledModules) {
            if (module is IChatModule) {
                if (hasChatModule) {
                    logger.error("Multiple chat modules found (see above), this is not supported and won't work")
                }
                hasChatModule = true
            }
            if (module is IVanishModule) {
                if (hasVanishModule) {
                    logger.error("Multiple vanish modules found (see above), this is not supported and won't work")
                }
                hasVanishModule = true
            }
        }
    }

    fun onServerStarted() = coroutineScope.launch {
        // TODO: this will fail if there is no internet on server start
        if (!waitForInit()) {
            if (config.getError() != null) {
                logger.error("Error initializing the mod, check the server console for errors")
            }
            return@launch
        }
        availableModules
            .filter { it.shouldEnable() }
            .also { toEnable ->
                toEnable.forEach { it.enable() }
                _enabledModules.addAll(toEnable)
            }
        logger.info("Loaded modules: " + enabledModules.joinToString { it::class.simpleName ?: "unknown" })
        // Doing this here to ensure spark is loaded
        spark = SparkHelper.createOrNull()
        if (config.events.enableStartMessages) {
            coroutineScope.launch {
                sendMessage(config.getDefaultChat(), lang.telegram.serverStarted)
            }
        }
        started = true
    }

    fun shutdown() {
        runBlocking {
            MuteService.shutdown()
            if (config.events.enableStopMessages && initialized.isCompleted) {
                sendMessage(config.getDefaultChat(), lang.telegram.serverStopped)
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

    private fun getMessageChat(msg: TgMessage): ChatConfig? {
        val topicId = if (msg.isTopicMessage ?: false) {
            msg.messageThreadId!!
        } else {
            // Replies in "General" topic have threadId set to the reply message id
            1
        }
        return config.getChat(msg.chat.id, topicId)
    }

    private suspend fun onTelegramTpsCommand(msg: TgMessage) {
        val spark = spark ?: return
        val chat = getMessageChat(msg) ?: return
        val durations = spark.getPlaceholders()
        if (durations == null) {
            logger.error("Unable to get spark data")
            return
        }
        sendMessage(chat, lang.telegram.tps.formatLang(durations))
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val chat = getMessageChat(msg) ?: return
        val onlinePlayers = platform.getOnlinePlayers().filterNot { it.isVanished() }
        val text = if (onlinePlayers.isNotEmpty()) {
            lang.telegram.playerList.formatLang(
                Placeholders(
                    mapOf(
                        "count" to onlinePlayers.size.toString(),
                        "usernames" to onlinePlayers.joinToString { it.getName() },
                    ),
                )
            )
        } else {
            lang.telegram.playerListZeroOnline
        }
        sendMessage(chat, text)
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        val chat = getMessageChat(msg) ?: return
        merger.lock.withLock {
            merger.lastMessages.remove(chat.name)
        }
        val textComponent = TelegramToMinecraftConverter.convert(msg, bot.me.id)
        val e = TgbridgeTgChatMessageEvent(
            msg, chat, null,
            Placeholders(
                mapOf("sender" to msg.senderName, "chat_name" to chat.name),
                mapOf("text" to textComponent),
            )
        )
        if (!TgbridgeEvents.TG_CHAT_MESSAGE.invoke(e)) return

        e.player?.let { player ->
            val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(player, Placeholders(), e)
            TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)
            e.placeholders = placeholdersEvt.placeholders
        }

        val fmtString = if (chat.isDefault) lang.minecraft.format else lang.minecraft.formatChat
        platform.broadcastMessage(chat, fmtString.formatMiniMessage(e.placeholders))
    }

    private fun tryReinit(ctx: TBCommandContext): Boolean = runBlocking {
        initialized = CompletableDeferred()
        init()
        val success = waitForInit()
        if (success) {
            onServerStarted()
            ctx.reply("tgbridge initialized")
        } else {
            ctx.reply(config.getError() ?: "Error initializing the mod, check the server console for errors")
        }
        return@runBlocking success
    }

    fun onReloadCommand(ctx: TBCommandContext): Boolean {
        if (!initialized.isCompleted) {
            try {
                if (config.getError() != null) {
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

        enabledModules
            .filter { !it.shouldEnable() && it.canBeDisabled }
            .also { toDisable ->
                logger.info("Disabling modules: " + toDisable.joinToString { it::class.simpleName ?: "unknown" })
                toDisable.forEach { it.disable() }
                _enabledModules.removeAll(toDisable)
            }
        availableModules
            .filter { it.shouldEnable() && !_enabledModules.contains(it) }
            .also { toEnable ->
                logger.info("Enabling modules: " + toEnable.joinToString { it::class.simpleName ?: "unknown" })
                toEnable.forEach { it.enable() }
                _enabledModules.addAll(toEnable)
            }
        checkConflictingModules()

        runBlocking {
            bot.recoverPolling()
            merger.unlock()
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        coroutineScope.launch {
            TgbridgeEvents.POST_RELOAD.invoke(Unit)
        }
        return true
    }

    fun onToggleMuteCommand(ctx: TBCommandContext): Boolean {
        val player = ctx.source?.uuid ?: return false
        if (MuteService.isMuted(player)) {
            if (MuteService.unmute(player)) {
                ctx.reply(lang.minecraft.serviceMessages.mute.unmuted)
                return true
            }
        } else if (MuteService.mute(player)) {
            ctx.reply(lang.minecraft.serviceMessages.mute.muted)
            return true
        }
        return false
    }

    fun onChatMessage(e: TgbridgeMcChatMessageEvent) = wrapMinecraftHandler {
        if (!TgbridgeEvents.MC_CHAT_MESSAGE.invoke(e)) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.sender,
            Placeholders(mapOf("username" to e.sender.getName())),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getChat(e.chatName) ?: return@wrapMinecraftHandler

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
            lang.telegram.chatMessage.formatMiniMessage(placeholdersEvt.placeholders)
                .append(Component.text(" "))
        )

        val currText = tgPrefix + telegramText
        val currDate = Clock.systemUTC().instant()

        val lm = merger.lastMessages[chat.name]
        if (
            lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text!! + "\n" + currText).text.length <= 4000
            && currDate.minus((config.messages.mergeWindow).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text = lm.text!! + "\n" + currText
            lm.date = currDate
            editMessageText(chat, lm.id, lm.text!!.text, lm.text!!.entities)
        } else {
            val newMsg = sendMessage(chat, currText.text, currText.entities)
            merger.lastMessages[chat.name] = LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText
            )
        }
    }

    fun onPlayerDeath(e: TgbridgeDeathEvent) = wrapMinecraftHandler {
        if (!config.events.enableDeathMessages) return@wrapMinecraftHandler

        val convertedMessage = when (val msg = e.message) {
            is TranslatableComponent -> {
                val args = mutableListOf<Component>(Component.text(e.player.getName()))
                args.addAll(msg.args().map { it.asComponent() }.drop(1))
                msg.args(args)
            }

            null -> Component.translatable("death.attack.generic", Component.text(e.player.getName()))
            else -> msg
        }

        if (!TgbridgeEvents.DEATH.invoke(e)) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(component = mapOf("death_message" to convertedMessage)),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val telegramText = MinecraftToTelegramConverter.convert(
            lang.telegram.playerDied.formatMiniMessage(placeholdersEvt.placeholders),
        )
        val chat = config.getDefaultChat()
        sendMessage(chat, telegramText.text, telegramText.entities)
        merger.lastMessages.remove(chat.name)
    }

    fun onPlayerJoin(e: TgbridgeJoinEvent) = wrapMinecraftHandler {
        if (!config.events.enableJoinMessages) return@wrapMinecraftHandler
        if (!TgbridgeEvents.JOIN.invoke(e)) return@wrapMinecraftHandler
        if (!e.ignoreVanish && e.player.isVanished()) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(mapOf("username" to e.player.getName())),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getDefaultChat()
        val lm = merger.lastMessages[chat.name]
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == e.player.uuid
            && currDate.minus((config.events.leaveJoinMergeWindow).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            deleteMessage(chat, lm.id)
        } else {
            val message = if (e.hasPlayedBefore) {
                lang.telegram.playerJoined
            } else {
                lang.telegram.playerJoinedFirstTime
            }
            sendMessage(chat, message.formatLang(placeholdersEvt.placeholders))
        }
        merger.lastMessages.remove(chat.name)
    }

    fun onPlayerLeave(e: TgbridgeLeaveEvent) = wrapMinecraftHandler {
        if (!config.events.enableLeaveMessages) return@wrapMinecraftHandler
        if (!TgbridgeEvents.LEAVE.invoke(e)) return@wrapMinecraftHandler
        if (!e.ignoreVanish && e.player.isVanished()) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(mapOf("username" to e.player.getName())),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getDefaultChat()
        val newMsg = sendMessage(
            chat,
            lang.telegram.playerLeft.formatLang(placeholdersEvt.placeholders),
        )
        merger.lastMessages[chat.name] = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = e.player.uuid
        )
    }

    fun onPlayerAdvancement(e: TgbridgeAdvancementEvent) = wrapMinecraftHandler {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) return@wrapMinecraftHandler

        val advancementName = e.title.asString()
        val advancementDescription = if (advancementsCfg.showDescription) {
            e.description.asString()
        } else {
            ""
        }

        if (!TgbridgeEvents.ADVANCEMENT.invoke(e)) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(
                mapOf(
                    "username" to e.player.getName(),
                    "title" to advancementName.escapeHTML(),
                    "description" to advancementDescription.escapeHTML(),
                ),
            ),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

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
        val chat = config.getDefaultChat()
        sendMessage(chat, langKey.formatLang(placeholdersEvt.placeholders))
        merger.lastMessages.remove(chat.name)
    }

    fun wrapMinecraftHandler(fn: suspend () -> Unit) {
        if (!initialized.isCompleted) {
            return
        }
        coroutineScope.launch {
            merger.lock.withLock {
                fn()
            }
        }
    }

    private suspend fun sendMessage(chat: ChatConfig, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            chat.chatId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
            replyToMessageId = chat.topicId,
        )
    }

    private suspend fun editMessageText(
        chat: ChatConfig,
        messageId: Int,
        text: String,
        entities: List<TgEntity>? = null
    ): TgMessage {
        return bot.editMessageText(
            chat.chatId,
            messageId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
        )
    }

    private suspend fun deleteMessage(chat: ChatConfig, messageId: Int) {
        bot.deleteMessage(chat.chatId, messageId)
    }
}
