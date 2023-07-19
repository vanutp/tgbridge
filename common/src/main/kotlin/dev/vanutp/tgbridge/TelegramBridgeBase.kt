package dev.vanutp.tgbridge

abstract class TelegramBridgeBase {
    fun init() {
        logger.info("tgbridge starting on $platform")
    }

    protected abstract val logger: LoggerBase
    abstract val platform: String
}
