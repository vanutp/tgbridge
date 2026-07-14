package dev.vanutp.tgbridge.common

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private class NoopLogger : ILogger {
    override fun info(message: Any) {}
    override fun warn(message: Any) {}
    override fun error(message: Any) {}
    override fun error(message: Any, exc: Exception) {}
}

private fun rateLimitException(retryAfter: Int) = TelegramException(
    errorCode = 429,
    parameters = TgResponseParameters(retryAfter = retryAfter),
)

class TelegramRateLimitTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesRetryAfterFrom429Body() {
        // Exact payload returned by the Telegram Bot API on a rate limit.
        val body =
            """{"ok":false,"error_code":429,"description":"Too Many Requests: retry after 9","parameters":{"retry_after":9}}"""

        val response = json.decodeFromString<TgResponse<JsonElement>>(body)

        assertEquals(false, response.ok)
        assertEquals(429, response.errorCode)
        assertEquals(9, response.parameters?.retryAfter)
        assertNull(response.result)
    }

    @Test
    fun telegramExceptionExposesRetryAfter() {
        val body =
            """{"ok":false,"error_code":429,"description":"Too Many Requests: retry after 9","parameters":{"retry_after":9}}"""
        val response = json.decodeFromString<TgResponse<JsonElement>>(body)

        val exception = TelegramException(
            errorCode = response.errorCode,
            description = response.description,
            parameters = response.parameters,
            responseBody = body,
        )

        assertEquals(429, exception.errorCode)
        assertEquals(9, exception.retryAfter)
    }

    @Test
    fun transient429IsRetriedThenSucceeds() = runBlocking {
        var calls = 0
        val result = withRetry(logger = NoopLogger(), retryExceptions = emptySet()) {
            calls++
            // retry_after = 0 keeps the test fast (only the ~1s margin is waited)
            if (calls < 3) throw rateLimitException(retryAfter = 0)
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, calls)
    }

    @Test
    fun persistent429IsCappedThenGivesUp() = runBlocking {
        var calls = 0
        val thrown = assertFailsWith<TelegramException> {
            // A high connection-retry budget must NOT keep a persistent rate limit
            // alive: the rate-limit cap is what must stop it.
            withRetry(logger = NoopLogger(), maxAttempts = 100, retryExceptions = emptySet()) {
                calls++
                throw rateLimitException(retryAfter = 0)
            }
        }
        assertEquals(429, thrown.errorCode)
        // 1 initial attempt + MAX_RATE_LIMIT_RETRIES retries, then it gives up
        assertEquals(1 + MAX_RATE_LIMIT_RETRIES, calls)
    }

    @Test
    fun successResponseHasNoError() {
        val body = """{"ok":true,"result":{"id":1,"first_name":"bot"}}"""

        val response = json.decodeFromString<TgResponse<TgUser>>(body)

        assertEquals(true, response.ok)
        assertNull(response.errorCode)
        assertNull(response.parameters)
        assertEquals(1, response.result?.id)
    }
}
