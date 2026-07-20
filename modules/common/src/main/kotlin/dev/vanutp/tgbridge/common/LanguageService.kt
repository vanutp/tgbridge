package dev.vanutp.tgbridge.common

import com.google.gson.GsonBuilder
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.models.ResourceReloadEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import okhttp3.OkHttpClient
import java.lang.reflect.Type
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*
import com.google.gson.JsonArray as GsonArray
import com.google.gson.JsonDeserializationContext as GsonDeserializationContext
import com.google.gson.JsonDeserializer as GsonDeserializer
import com.google.gson.JsonElement as GsonElement
import com.google.gson.JsonObject as GsonObject
import com.google.gson.JsonParseException as GsonParseException
import com.google.gson.JsonPrimitive as GsonPrimitive
import com.google.gson.JsonSerializationContext as GsonSerializationContext
import com.google.gson.JsonSerializer as GsonSerializer


data class LanguageFile(
    val name: String,
    val contents: String,
)

interface IResourceContainer {
    val containerPath: Path
    fun getLanguageFiles(language: String): List<LanguageFile>
}

class ZipResourceContainer(private val zipRoot: Path?, override val containerPath: Path) : IResourceContainer {
    private fun <T> withZipRoot(block: (Path) -> T): T {
        return if (zipRoot != null) {
            block(zipRoot)
        } else {
            FileSystems
                .newFileSystem(
                    URI("jar:${containerPath.toUri()}"),
                    emptyMap<String, String>()
                )
                .use {
                    block(it.rootDirectories.first())
                }
        }
    }

    override fun getLanguageFiles(language: String): List<LanguageFile> {
        val locale = language.lowercase()
        return withZipRoot { zipRoot ->
            val assets = zipRoot.resolve("assets")
            if (!assets.isDirectory()) return@withZipRoot listOf()
            assets.listDirectoryEntries()
                .asSequence()
                .filter { it.isDirectory() }
                .flatMap { it.listDirectoryEntries() }
                .filter { it.isDirectory() && it.name.removeSuffix("/") == "lang" }
                .flatMap { it.listDirectoryEntries() }
                .filter { it.isRegularFile() && it.name.lowercase() == "$locale.json" }
                .map { LanguageFile(it.name, it.readText()) }
                .toList()
        }
    }
}

private fun parseTranslationValue(v: GsonElement, allowArrays: Boolean = true): Component =
    when (v) {
        // TODO: migrate to when guards
        is GsonArray -> if (allowArrays) {
            val parsed = v.asList().map { parseTranslationValue(it, false) }
            if (parsed.isEmpty()) {
                Component.empty()
            } else {
                parsed.first().append(parsed.drop(1))
            }
        } else {
            throw GsonParseException("Expected string or object, found $v")
        }

        is GsonPrimitive -> if (v.isString) {
            Component.text(v.asString)
        } else {
            throw GsonParseException("Expected string or object, found $v")
        }

        is GsonObject -> GsonComponentSerializer.gson().deserializeFromTree(
            v
        )

        else -> throw UnsupportedOperationException("Unreachable code in parseTranslationValue on $v")
    }

private data class LanguageSourceDataFile(
    val size: Long,
    val lastModified: Long,
)

private data class LanguageSourceData(
    val version: Int,
    val minecraftVersion: String,
    val language: String,
    // file name -> (size, last modified)
    val files: Map<String, LanguageSourceDataFile>,
)

private data class LanguageData(
    val source: LanguageSourceData?,
    val strings: Map<String, Component>,
) {
    companion object {
        private const val SOURCE_DATA_KEY = "_tgbridge_source_data"
    }

    class Serializer : GsonDeserializer<LanguageData>, GsonSerializer<LanguageData> {
        override fun deserialize(
            json: GsonElement,
            typeOfT: Type,
            context: GsonDeserializationContext
        ): LanguageData {
            val root = json as? GsonObject
                ?: throw GsonParseException("Failed to parse minecraft_language.json: not an object")
            val sourceData = root.get(SOURCE_DATA_KEY)?.let {
                context.deserialize<LanguageSourceData>(it, LanguageSourceData::class.java)
            }
            val strings = root.asMap().mapNotNull {
                if (it.key != SOURCE_DATA_KEY) {
                    it.key to parseTranslationValue(it.value, false)
                } else {
                    null
                }
            }.toMap()
            return LanguageData(sourceData, strings)
        }

        override fun serialize(
            src: LanguageData,
            typeOfSrc: Type,
            context: GsonSerializationContext
        ): GsonElement {
            return src.strings
                .asSequence()
                .map {
                    val key = it.key
                    val value = it.value
                    if (value is TextComponent && value.children().isEmpty()) {
                        key to GsonPrimitive(value.content())
                    } else {
                        key to GsonComponentSerializer.gson().serializeToTree(value)
                    }
                }
                .plusElement(SOURCE_DATA_KEY to context.serialize(src.source))
                .toMap()
                .let { context.serialize(it) }
        }
    }
}

@Serializable
private data class AssetEntry(
    val hash: String,
)

@Serializable
private data class MinecraftAssetIndex(
    val objects: Map<String, AssetEntry>
)

@Serializable
private data class ObjectEntry(
    val id: String,
    val url: String,
)

@Serializable
private data class MinecraftVersionManifest(
    val assetIndex: ObjectEntry
)


@Serializable
private data class MinecraftManifest(
    val versions: List<ObjectEntry>
)

object LanguageService {
    private const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val RESOURCES_URL = "https://resources.download.minecraft.net"
    private const val SOURCE_DATA_VERSION = 2

    private val bridge = TelegramBridge.INSTANCE
    private val lenientJson = Json {
        ignoreUnknownKeys = true
    }
    private val gson = GsonBuilder()
        .registerTypeAdapter(LanguageData::class.java, LanguageData.Serializer())
        .create()

    private val minecraftLangPath = bridge.platform.configDir.resolve("minecraft_lang.json")
    private var language = LanguageData(null, emptyMap())
    private val hardcodedDefaultMinecraftLang = mapOf(
        "gui.xaero-deathpoint-old" to "Old Death",
        "gui.xaero-deathpoint" to "Death",
    )

    fun init() {
        if (minecraftLangPath.exists()) {
            language = gson.fromJson(minecraftLangPath.readText(), LanguageData::class.java)
        }

        TgbridgeEvents.POST_RELOAD.addListener {
            updateLangIfNeeded()
        }
    }

    private suspend fun downloadVanillaLang(): String {
        val http = OkHttpClient.Builder().withProxyConfig().build()
        try {
            bridge.logger.info("Downloading vanilla language file...")
            val version = bridge.platform.getMinecraftVersion()
            val lang = config.messages.autoMessagesLanguage
            val manifest = lenientJson.decodeFromString<MinecraftManifest>(http.get(MANIFEST_URL))
            val versionInfo = manifest.versions.find { it.id == version }
                ?: throw IllegalStateException("Minecraft version $version not found in the Mojang manifest")
            val versionData = lenientJson.decodeFromString<MinecraftVersionManifest>(http.get(versionInfo.url))
            val assetIndexData = lenientJson.decodeFromString<MinecraftAssetIndex>(http.get(versionData.assetIndex.url))
            val langFileUrl = assetIndexData.objects.entries
                .find { it.key == "minecraft/lang/$lang.json" }
                ?.value
                ?.let { "${RESOURCES_URL}/${it.hash.substring(0, 2)}/${it.hash}" }
                ?: throw IllegalStateException("Language $lang not found in the asset index")
            return http.get(langFileUrl)
        } finally {
            http.dispatcher.executorService.shutdown()
            http.connectionPool.evictAll()
        }
    }

    private suspend fun getVanillaLang(): Map<String, String> {
        val version = bridge.platform.getMinecraftVersion().replace('.', '_')
        val lang = config.messages.autoMessagesLanguage
        val cacheDir = bridge.platform.configDir.resolve(".cache")
        if (!cacheDir.isDirectory()) {
            cacheDir.createDirectories()
        }
        val cacheFile = cacheDir.resolve("vanilla_${version}_$lang.json")
        if (!cacheFile.isRegularFile()) {
            cacheDir.listDirectoryEntries().forEach { it.deleteExisting() }
            cacheFile.writeText(downloadVanillaLang())
        }
        return Json.decodeFromString(cacheFile.readText())
    }

    suspend fun updateLangIfNeeded() {
        val version = bridge.platform.getMinecraftVersion()
        val lang = config.messages.autoMessagesLanguage ?: return

        val e = ResourceReloadEvent(bridge.platform.getModResources())
        TgbridgeEvents.RESOURCE_RELOAD.invoke(e)
        val files = e.containers
            .asSequence()
            .map { it.containerPath }
            .distinct()
            .filter { it.absolute().startsWith(bridge.platform.gameDir) }
            .associate {
                val path = try {
                    it.absolute().relativeTo(bridge.platform.gameDir)
                } catch (_: IllegalArgumentException) {
                    it
                }
                path.toString() to LanguageSourceDataFile(
                    it.fileSize(),
                    it.getLastModifiedTime().toInstant().epochSecond,
                )
            }
        val newSourceData = LanguageSourceData(
            version = SOURCE_DATA_VERSION,
            minecraftVersion = version,
            language = lang,
            files = files
        )
        if (newSourceData == language.source) {
            return
        }

        val newLang = mutableMapOf<String, Component>()
        if (lang != "en_us") {
            bridge.logger.info("Generating language file for version $version and language $lang")
            newLang.putAll(getVanillaLang().mapValues { Component.text(it.value) })
            bridge.logger.info("Processing mods & resource packs...")
            e.containers
                .flatMap { container ->
                    container.getLanguageFiles(lang).map {
                        container.containerPath to it
                    }
                }
                .forEach { (containerPath, file) ->
                    val err = { entry: Map.Entry<String, GsonElement> ->
                        bridge.logger.warn("Could not parse the key '${entry.key}' in $containerPath:${file.name}")
                        null
                    }
                    val data = try {
                        gson.fromJson(file.contents, com.google.gson.JsonObject::class.java)
                    } catch (e: Throwable) {
                        bridge.logger.warn("Failed to parse $containerPath:${file.name}: ${e.stackTraceToString()}")
                        return@forEach
                    }
                    val converted = data.asMap().mapNotNull {
                        try {
                            it.key to parseTranslationValue(it.value)
                        } catch (_: Throwable) {
                            err(it)
                            return@forEach
                        }
                    }
                    newLang.putAll(converted)
                }
            bridge.logger.info("Language file generated!")
        }

        language = LanguageData(newSourceData, newLang)
        minecraftLangPath.writeText(gson.toJson(language))
    }

    fun getString(key: String): Component? {
        return language.strings[key]
            ?: bridge.platform.getLanguageKey(key)?.let(Component::text)
            ?: hardcodedDefaultMinecraftLang[key]?.let(Component::text)
    }
}
