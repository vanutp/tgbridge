package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.paper.PaperBootstrap

abstract class AbstractCompat(protected val bootstrap: PaperBootstrap) {
    abstract val pluginId: String
    open fun enable() {}
}
