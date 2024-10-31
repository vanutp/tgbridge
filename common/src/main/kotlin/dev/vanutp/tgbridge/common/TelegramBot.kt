package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.dataclass.*
import dev.vanutp.tgbridge.common.dataclass.TgApi
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration


class TelegramBot(botApiUrl: String, botToken: String, private val logger: AbstractLogger) {

    private val POLL_TIMEOUT_SECONDS = 60

    private val okhttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
        .build()
    private val client = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("$botApiUrl/bot$botToken/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TgApi::class.java)
    private var pollTask: Job? = null
    private val commandHandlers: MutableList<suspend (TgMessage) -> Boolean> = mutableListOf()
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    fun registerCommandHandler(command: String, handler: suspend (TgMessage) -> Unit) {
        val cmdRegex = Regex("^/$command(@${me.username})?(\\s|\$)", RegexOption.IGNORE_CASE)
        commandHandlers.add {
            if (cmdRegex.matches(it.effectiveText ?: "")) {
                handler(it)
                return@add true
            } else {
                return@add false
            }
        }
    }

    suspend fun init() {
        call { client.deleteWebhook() }
        me = call { client.getMe() }
    }

    suspend fun startPolling(scope: CoroutineScope) {
        if (pollTask != null) {
            throw IllegalStateException("polling already started")
        }
        pollTask = scope.launch {
            var offset = -1
            while (true) {
                try {
                    val updates = call {
                        client.getUpdates(
                            offset,
                            timeout = POLL_TIMEOUT_SECONDS,
                        )
                    }
                    if (updates.isEmpty()) {
                        continue
                    }
                    offset = updates.last().updateId + 1
                    updates.forEach { update ->
                        if (update.message == null) {
                            return@forEach
                        }
                        for (handler in commandHandlers) {
                            if (handler.invoke(update.message)) {
                                return@forEach
                            }
                        }
                        messageHandlers.forEach {
                            it.invoke(update.message)
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> break
                        else -> {
                            logger.error(e.message.toString(), e)
                            delay(1000)
                        }
                    }
                }
            }
            logger.info("pollTask finished")
        }
    }

    suspend fun recoverPolling(scope: CoroutineScope) {
        val task = pollTask
        if (task != null) {
            if (!task.isCompleted) {
                task.cancelAndJoin()
            }
            pollTask = null
        }
        startPolling(scope)
    }

    suspend fun shutdown() {
        pollTask?.cancelAndJoin()
        okhttpClient.dispatcher().executorService().shutdown()
        okhttpClient.connectionPool().evictAll()
    }

    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Int? = null,
        parseMode: String =
            if (ConfigManager.config.messages.styledMinecraftMessagesInTelegram) "None"
            else "HTML",
        disableWebPagePreview: Boolean = true,
        entities: List<TgEntity>? = null,
    ): TgMessage = call {
        client.sendMessage(TgSendMessageRequest(chatId, text, replyToMessageId, parseMode, disableWebPagePreview, entities=entities))
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Int,
        text: String,
        parseMode: String =
            if (ConfigManager.config.messages.styledMinecraftMessagesInTelegram) "None"
            else "HTML",
        disableWebPagePreview: Boolean = true,
        entities: List<TgEntity>? = null,
    ): TgMessage = call {
        client.editMessageText(TgEditMessageRequest(chatId, messageId, text, parseMode, disableWebPagePreview, entities=entities))
    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) = call {
        client.deleteMessage(TgDeleteMessageRequest(chatId, messageId))
    }
}
