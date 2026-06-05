package io.github.chindeaone.collectiontracker.config

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.chindeaone.collectiontracker.config.error.ConfigError
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
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.LegacyStringChromaColourTypeAdapter
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import java.nio.file.AtomicMoveNotSupportedException
import kotlin.jvm.java

class ConfigManager {

    companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(ChromaColour::class.java, LegacyStringChromaColourTypeAdapter(true).nullSafe())
            .registerTypeAdapter(UUID::class.java, object : TypeAdapter<UUID>() {
                override fun write(out: JsonWriter, value: UUID) {
                    out.value(value.toString())
                }

                override fun read(reader: JsonReader): UUID {
                    return UUID.fromString(reader.nextString())
                }
            }.nullSafe())
            .enableComplexMapKeySerialization()
            .create()
    }

    private val logger: Logger = LogManager.getLogger(ConfigManager::class)

    private var configDirectory = File("config/sct")
    private var configFile: File
    var config: ModConfig? = null
    private var lastSaveTime = 0L

    var processor: MoulConfigProcessor<ModConfig>

    init {
        configDirectory.mkdirs()
        configFile = File(configDirectory, "config.json")

        if (configFile.isFile) {
            logger.info("Trying to load the config")
            tryReadConfig()
        }

        if (config == null) {
            logger.info("Creating a clean config.")
            config = ModConfig()
        }

        val config = config!!
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
            InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8).use { isr ->
                JsonReader(isr).use { reader ->
                    config = gson.fromJson(reader, ModConfig::class.java)
                }
            }
            // Remove null entries
            config?.let { removeNulls(it) }
        } catch (e: Exception) {
            throw ConfigError("Could not load config", e)
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

                        else -> {
                            remove(value)
                        }
                    }
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }
        remove(root)
    }

    @Synchronized
    fun save() {
        TemporaryBuffsParser.saveDurations()
        lastSaveTime = System.currentTimeMillis()
        val config = config ?: error("Cannot save null config.")

        configDirectory.mkdirs()
        val unit = configDirectory.resolve("config.json.write")

        try {
            OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8).use { writer ->
                writer.write(gson.toJson(config))
            }

            try {
                Files.move(
                    unit.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (e: AtomicMoveNotSupportedException) {
                logger.warn("Atomic move not supported, falling back to non-atomic move", e)
                Files.move(
                    unit.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }

        } catch (e: IOException) {
            unit.delete() // cleanup best effort
            logger.error("Could not save config", e)
        }
    }
}