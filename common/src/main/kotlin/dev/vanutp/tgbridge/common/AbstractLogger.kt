package dev.vanutp.tgbridge.common

abstract class AbstractLogger {
    abstract fun info(message: Any)
    abstract fun warn(message: Any)
    abstract fun error(message: Any)
}
