package dev.vanutp.tgbridge

abstract class LoggerBase {
    abstract fun info(message: String)
    abstract fun warn(message: String)
    abstract fun error(message: String)
}
