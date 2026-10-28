package io.github.chindeaone.collectiontracker.config

import com.google.gson.GsonBuilder
import io.github.chindeaone.collectiontracker.config.version.VersionManager
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.migration.ConfigMigrator
import io.github.chindeaone.collectiontracker.utils.RuntimeConfigLoader
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.LegacyStringChromaColourTypeAdapter
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import java.time.LocalDateTime
import kotlin.concurrent.fixedRateTimer
import kotlin.jvm.java

class ConfigManager {

    companion object {
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(ChromaColour::class.java, LegacyStringChromaColourTypeAdapter(true).nullSafe())
            .enableComplexMapKeySerialization()
            .create()
    }

    private val logger: Logger = LogManager.getLogger(ConfigManager::class)

    private var configDirectory = File("config/sct")
    private var configBackupDirectory = File("config/sct/backups")
    private var configFile: File
    private var configBackupFile: File

    var config: ModConfig? = null
    private var savedConfig: String? = null

    var processor: MoulConfigProcessor<ModConfig>

    init {
        configDirectory.mkdirs()
        configBackupDirectory.mkdirs()
        configFile = File(configDirectory, "config.json")
        configBackupFile = File(configBackupDirectory, "config_backup.json")

        if (configFile.isFile) {
            logger.info("[SCT]: Trying to load the config")
            tryReadConfig()
        }

        if (config == null) {
            logger.info("[SCT]: Creating a clean config.")
            config = ModConfig()
        }

        val config = config ?: error("[SCT]: Failed to initialize config")

        processor = MoulConfigProcessor(config)
        BuiltinMoulConfigGuis.addProcessors(processor)
        VersionManager.injectConfigProcessor(processor)
        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(config)
        findPositionLinks(config, Collections.newSetFromMap(IdentityHashMap()))
    }

    private fun findPositionLinks(obj: Any?, visited: MutableSet<Any>) {
        if (obj == null) return
        val cls = obj.javaClass
        if (!cls.name.startsWith("io.github.chindeaone.collectiontracker")) return
        if (!visited.add(obj)) return

        var currClass: Class<*>? = cls
        while (currClass != null && currClass.name.startsWith("io.github.chindeaone.collectiontracker")) {
            for (field in currClass.declaredFields) {
                if (field.isSynthetic) continue
                try {
                    field.isAccessible = true
                    val type = field.type

                    if (Position::class.java.isAssignableFrom(type)) {
                        val position = field.get(obj) as? Position ?: continue
                        val configLink = field.getAnnotation(ConfigLink::class.java)
                        if (configLink != null) {
                            position.setLink(configLink)
                        }
                    } else {
                        if (type.isPrimitive || type.isEnum || type.name.startsWith("java.") || type.name.startsWith("com.google.gson.")) continue
                        val value = field.get(obj) ?: continue
                        findPositionLinks(value, visited)
                    }
                } catch (e: Exception) {
                    logger.error("[SCT] Failed to link field '${field.name}'", e)
                }
            }
            currClass = currClass.superclass
        }
    }

    private fun tryReadConfig() {
        try {
            val jsonObject = FileReader(configFile, StandardCharsets.UTF_8).use {
                JsonParser.parseReader(it).asJsonObject
            }

            val saved = ConfigMigrator.migrate(jsonObject)
            if (saved) {
                logger.info("[SCT]: Config migrated to the latest version, saving the updated config.")
            }

            config = gson.fromJson(jsonObject, ModConfig::class.java)
            // Remove null entries
            config?.let { loadedConfig ->
                removeNulls(loadedConfig)
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Could not load config", e)
        }
    }

    private fun removeNulls(root: Any?) {
        if (root == null) return
        val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())

        fun remove(obj: Any?) {
            if (obj == null) return

            val cls = obj.javaClass
            if (cls.isPrimitive || cls.packageName.startsWith("java.") || cls.isEnum) return
            if (!visited.add(obj)) return

            for (field in cls.declaredFields) {
                try {
                    field.isAccessible = true
                    val value = field.get(obj) ?: continue

                    when (value) {
                        is MutableList<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val list = value as MutableList<Any?>
                            val it = list.iterator()
                            while (it.hasNext()) {
                                if (it.next() == null) it.remove()
                            }
                            for (el in list) remove(el)
                        }

                        is Collection<*> -> {
                            for (el in value) remove(el)
                        }

                        is Map<*, *> -> {
                            for (entry in value.values) remove(entry)
                        }

                        else -> remove(value)
                    }
                } catch (_: Exception) {}
            }
        }
        remove(root)
    }

    fun loadFromConfig() {
        RuntimeConfigLoader.loadConsumableDurations()
    }

    @Synchronized
    fun save() {
        val config = config ?: error("[SCT]: Cannot save null config.")
        val content = gson.toJson(config)

        ConsumableParser.saveConsumableDurations()

        saveFileWithRetry(
            tempFile = File(configDirectory, "config.json.write"),
            backupFile = createBackupFile(),
            content = content
        )
    }

    private fun saveFileWithRetry(tempFile: File, backupFile: File, content: String, attempt: Int = 0) {
        try {
            tempFile.parentFile?.mkdirs()
            tempFile.writeText(content, StandardCharsets.UTF_8)

            if (configFile.exists()) {
                backupFile.parentFile?.mkdirs()
                configFile.copyTo(backupFile, overwrite = true)
            }

            try {
                Files.move(
                    tempFile.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (e: AccessDeniedException) {
                if (attempt >= 5) throw e
                Thread.sleep(100L)
                saveFileWithRetry(tempFile, backupFile, content, attempt + 1)
            }

            savedConfig = content
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to save config, restoring from backup.", e)
            tempFile.delete()
            if (backupFile.exists()) backupFile.copyTo(configFile, overwrite = true)
            throw e
        }
    }

    private fun createBackupFile(): File {
        val now = LocalDateTime.now()
        val yearDir = File(configBackupDirectory, now.year.toString())
        val monthDir = File(yearDir, now.monthValue.toString().padStart(2, '0'))
        monthDir.mkdirs()

        val version = SkyblockCollectionTracker.VERSION
        val fileName = "${now.toLocalDate()}-${version}-backup.json"

        return File(monthDir, fileName)
    }

    fun startAutoSave() {
        fixedRateTimer("sct-auto-save", daemon = true, initialDelay = 60_000L, period = 60_000L) {
            save()
        }
    }
}