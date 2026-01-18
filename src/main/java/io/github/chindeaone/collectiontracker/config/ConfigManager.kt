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

class ConfigManager {

    companion object {
        val gson = GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues().registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(UUID::class.java, object : TypeAdapter<UUID>() {
                override fun write(out: JsonWriter, value: UUID) {
                    out.value(value.toString())
                }

                override fun read(reader: JsonReader): UUID {
                    return UUID.fromString(reader.nextString())
                }
            }.nullSafe()).enableComplexMapKeySerialization().create()
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

        Runtime.getRuntime().addShutdownHook(Thread {
            save()
        })
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

    fun save() {
        lastSaveTime = System.currentTimeMillis()
        val config = config ?: error("Can not save null config.")

        try {
            configDirectory.mkdirs()
            val unit = configDirectory.resolve("config.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                writer.write(gson.toJson(config))
            }
            Files.move(
                unit.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
            )
        } catch (e: IOException) {
            throw ConfigError("Could not save config", e)
        }
    }
}