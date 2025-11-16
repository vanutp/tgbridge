package dev.vanutp.tgbridge.common.compat

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeEvents
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ReplacementsCompat(bridge: TelegramBridge) : AbstractCompat(bridge) {
    private lateinit var filePath: Path
    private var replacements: Map<String, String> = mapOf()
    private var pattern: Pattern? = null

    private fun loadConfig() {
        filePath = bridge.platform.configDir.resolve("replacements.json")
        if (filePath.notExists()) {
            val defaultData = ReplacementsCompat::class.java.getResource("/replacements.json")!!.readText()
            filePath.writeText(defaultData)
        }
        replacements = Json.decodeFromString<Map<String, String>>(filePath.readText())
        pattern = replacements.keys.joinToString("|").let { regex ->
            if (regex.isEmpty()) null else Pattern.compile("($regex)", Pattern.CASE_INSENSITIVE)
        }
    }

    override fun enable() {
        loadConfig()
        TgbridgeEvents.POST_RELOAD.addListener { loadConfig() }
        TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
            val effectivePattern = pattern ?: return@addListener
            e.message = e.message.replaceText {
                it.match(effectivePattern).replacement { match, _ ->
                    val matchStr = match.group()
                    val replacement = replacements[matchStr] ?: matchStr
                    Component.text(replacement)
                }
            }
        }
    }
}
