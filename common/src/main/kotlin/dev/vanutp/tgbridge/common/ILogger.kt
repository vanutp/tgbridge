package dev.vanutp.tgbridge.common

interface ILogger {
    fun info(message: Any)
    fun warn(message: Any)
    fun error(message: Any)
    fun error(message: Any, exc: Exception)
}
