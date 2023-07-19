package dev.vanutp.tgbridge

abstract class TelegramBridgeBase {
    companion object {
        const val MOD_ID = "tgbridge"
    }

    fun init() {
        println("meow meow from base")
    }
}
