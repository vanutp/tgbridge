package dev.vanutp.tgbridge.common.compat

import kotlin.test.Test

class OggUtilsTest {
    @Test
    fun parseShortOgg() {
        val shortMessage = OggUtilsTest::class.java.getResource("/short.ogg")!!.readBytes()
        extractOpusPackets(shortMessage)
    }

    @Test
    fun parseLongOgg() {
        val longMessage = OggUtilsTest::class.java.getResource("/long.ogg")!!.readBytes()
        extractOpusPackets(longMessage)
    }
}
