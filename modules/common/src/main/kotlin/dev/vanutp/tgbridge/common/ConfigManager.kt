package dev.vanutp.tgbridge.common

import com.charleskorn.kaml.*
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.Config
import dev.vanutp.tgbridge.common.models.JoinMessagesMode
import dev.vanutp.tgbridge.common.models.Lang
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage
import java.nio.file.Path
import kotlin.io.path.*

object ConfigManager {
    private const val LATEST_CONFIG_VERSION = 4
    private const val LATEST_LANG_VERSION = 4

    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private lateinit var configDir: Path
    lateinit var config: Config
        private set
    lateinit var lang: Lang
        private set

    fun init(configDir: Path) {
        this.configDir = configDir
        reload()
    }

    private fun migrateLang() {
        if (lang.version == 1) {
            val mm = MiniMessage.miniMessage()
            lang = lang.copy(
                version = 2,
                minecraft = lang.minecraft.copy(
                    messageMeta = lang.minecraft.messageMeta.copy(
                        reply = "<blue>" + mm.escapeTags(lang.minecraft.messageMeta.reply)
                            .replace("{sender}", "<sender>")
                            .replace("{text}", "<text>"),
                        replyToMinecraft = "<blue>" + mm.escapeTags(lang.minecraft.messageMeta.replyToMinecraft)
                            .replace("{text}", "<text>"),
                        forward = "<gray>" + mm.escapeTags(lang.minecraft.messageMeta.forward)
                            .replace("{from}", "<from>"),
                        gif = mm.escapeTags(lang.minecraft.messageMeta.gif),
                        document = mm.escapeTags(lang.minecraft.messageMeta.document),
                        photo = mm.escapeTags(lang.minecraft.messageMeta.photo),
                        audio = mm.escapeTags(lang.minecraft.messageMeta.audio),
                        sticker = mm.escapeTags(lang.minecraft.messageMeta.sticker),
                        video = mm.escapeTags(lang.minecraft.messageMeta.video),
                        videoMessage = mm.escapeTags(lang.minecraft.messageMeta.videoMessage),
                        voiceMessage = mm.escapeTags(lang.minecraft.messageMeta.voiceMessage),
                        poll = mm.escapeTags(lang.minecraft.messageMeta.poll)
                            .replace("{title}", "<title>"),
                        pin = "<dark_aqua>" + mm.escapeTags(lang.minecraft.messageMeta.pin) + "</dark_aqua> <message>",
                    )
                )
            )
        } else if (lang.version == 2) {
            lang = lang.copy(
                version = 3,
                telegram = lang.telegram.copy(
                    playerDied = lang.telegram.playerDied
                        .replace("{deathMessage}", "<death_message>")
                ),
            )
        } else if (lang.version == 3) {
            lang = lang.copy(version = 4)
        } else if (lang.version != LATEST_LANG_VERSION) {
            throw Exception("Unsupported lang version ${lang.version}")
        }
    }

    private fun migrateConfig(configPath: Path, langPath: Path) {
        if (config.version == 1) {
            config = config.copy(
                version = 2,
                messages = config.messages.copy(
                    requirePrefixInMinecraft = null,
                ),
            )
        } else if (config.version == 2) {
            val data = yaml.parseToYamlNode(configPath.readText())
            config = config.copy(
                version = 3,
                integrations = config.integrations.copy(
                    bluemapUrl = data
                        .yamlMap.get<YamlMap>("messages")
                        ?.getScalar("bluemapUrl")?.content ?: ""
                )
            )
        } else if (config.version == 3) {
            val data = yaml.parseToYamlNode(configPath.readText())
            val oldGeneral = data.yamlMap.get<YamlMap>("general")
            val oldToken = oldGeneral?.getScalar("botToken")?.content ?: Config().botToken
            val oldChatId = oldGeneral?.getScalar("chatId")?.content?.toLongOrNull() ?: Config().chats[0].chatId
            val oldTopicId = when (val node = oldGeneral?.get<YamlNode>("topicId")) {
                is YamlScalar -> node.content.toIntOrNull()
                else -> null
            }
            val oldEvents = data.yamlMap.get<YamlMap>("events")
            val oldEnableJoinMessages = oldEvents?.getScalar("enableJoinMessages")?.content?.toBoolean() ?: true
            val langData = yaml.parseToYamlNode(langPath.readText())
            val langVersion = langData.yamlMap.getScalar("version")?.content?.toIntOrNull() ?: 1
            val oldMinecraftFormat = langData.yamlMap.get<YamlMap>("minecraft")?.getScalar("format")?.content
                ?: "<aqua>\\<<sender>></aqua> <text>"
            val oldTelegramFormat = langData.yamlMap.get<YamlMap>("telegram")?.getScalar("chatMessage")?.content
                ?.let {
                    if (langVersion < 2) {
                        it.replace("{sender}", "<username>").replace("{text}", "<text>")
                    } else {
                        "$it <text>"
                    }
                } ?: "<b>[<username>]</b> <text>"
            config = config.copy(
                version = 4,
                botToken = oldToken,
                chats = listOf(
                    ChatConfig(
                        name = "global",
                        isDefault = true,
                        chatId = oldChatId,
                        topicId = oldTopicId,
                        minecraftFormat = oldMinecraftFormat,
                        telegramFormat = oldTelegramFormat,
                    )
                ),
                events = config.events.copy(
                    joinMessages = if (oldEnableJoinMessages)
                        JoinMessagesMode.ENABLED
                    else
                        JoinMessagesMode.DISABLED
                )
            )
        } else if (config.version != LATEST_CONFIG_VERSION) {
            throw Exception("Unsupported config version ${config.version}")
        }
        if (config.chats.any { it.chatId > 0 }) {
            config = config.copy(
                chats = config.chats.map {
                    it.copy(
                        chatId = if (it.chatId > 0) {
                            -1000000000000 - it.chatId
                        } else {
                            it.chatId
                        }
                    )
                },
            )
        }
    }

    fun reload() {
        if (configDir.notExists()) {
            configDir.createDirectory()
        }

        val configPath = configDir.resolve("config.yml")
        if (configPath.notExists()) {
            configPath.writeText(yaml.encodeToString(Config(version = LATEST_CONFIG_VERSION)))
        }
        val langPath = configDir.resolve("lang.yml")
        if (langPath.notExists()) {
            langPath.writeText(yaml.encodeToString(Lang(version = LATEST_LANG_VERSION)))
        }

        config = yaml.decodeFromString<Config>(configPath.readText())
        migrateConfig(configPath, langPath)
        // write new keys & update docs
        configPath.writeText(yaml.encodeToString(config))

        lang = yaml.decodeFromString<Lang>(langPath.readText())
        migrateLang()
        // write new keys & update docs
        langPath.writeText(yaml.encodeToString(lang))
    }
}
