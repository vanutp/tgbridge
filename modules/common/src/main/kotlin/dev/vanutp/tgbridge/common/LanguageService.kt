package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.models.ResourceReloadEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*


interface IResourceContainer {
    val containerPath: Path
    fun getLanguageFiles(language: String): List<String>
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

    override fun getLanguageFiles(language: String): List<String> {
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
                .map { it.readText() }
                .toList()
        }
    }
}

@Serializable
data class LanguageSourceDataFile(
    val size: Long,
    val lastModified: Long,
)

@Serializable
data class LanguageSourceData(
    val version: Int,
    val minecraftVersion: String,
    val language: String,
    // file name -> (size, last modified)
    val files: Map<String, LanguageSourceDataFile>,
)

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
    private const val SOURCE_DATA_KEY = "_tgbridge_source_data"
    private const val SOURCE_DATA_VERSION = 1

    private val bridge = TelegramBridge.INSTANCE
    private val lenientJson = Json {
        ignoreUnknownKeys = true
    }

    private val minecraftLangPath = bridge.platform.configDir.resolve("minecraft_lang.json")
    private var minecraftLang = mapOf<String, String>()
    private var sourceData: LanguageSourceData? = null
    private val hardcodedDefaultMinecraftLang = mapOf(
        "gui.xaero-deathpoint-old" to "Old Death",
        "gui.xaero-deathpoint" to "Death",
    )

    fun init() {
        if (minecraftLangPath.exists()) {
            val data = Json.parseToJsonElement(minecraftLangPath.readText())
            sourceData = data.jsonObject[SOURCE_DATA_KEY]?.let {
                Json.decodeFromJsonElement(it)
            }
            if (sourceData == null || sourceData!!.version == SOURCE_DATA_VERSION) {
                minecraftLang = data.jsonObject
                    .filterNot { it.key == SOURCE_DATA_KEY }
                    .mapValues { it.value.jsonPrimitive.content }
            } else {
                sourceData = null
                minecraftLang = emptyMap()
            }
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
        if (newSourceData == sourceData) {
            return
        }

        val newLang = mutableMapOf<String, String>()
        if (lang != "en_us") {
            bridge.logger.info("Generating language file for version $version and language $lang")
            newLang.putAll(getVanillaLang())
            bridge.logger.info("Processing mods & resource packs...")
            e.containers
                .flatMap { it.getLanguageFiles(lang) }
                .forEach {
                    val data = Json.decodeFromString<Map<String, String>>(it)
                    newLang.putAll(data)
                }
            bridge.logger.info("Language file generated!")
        }

        sourceData = newSourceData
        minecraftLang = newLang
        val sourceDataSerialized = Json.encodeToJsonElement(sourceData!!)
        val serialized = JsonObject(
            mapOf(SOURCE_DATA_KEY to sourceDataSerialized)
                + Json.encodeToJsonElement(minecraftLang).jsonObject
        )
        minecraftLangPath.writeText(Json.encodeToString(serialized))
    }

    fun getString(key: String): String? {
        return minecraftLang[key]
            ?: bridge.platform.getLanguageKey(key)
            ?: hardcodedDefaultMinecraftLang[key]
    }
}
