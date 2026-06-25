package dev.vanutp.tgbridge.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun successResponseHasNoError() {
        val body = """{"ok":true,"result":{"id":1,"first_name":"bot"}}"""

        val response = json.decodeFromString<TgResponse<TgUser>>(body)

        assertEquals(true, response.ok)
        assertNull(response.errorCode)
        assertNull(response.parameters)
        assertEquals(1, response.result?.id)
    }
}
