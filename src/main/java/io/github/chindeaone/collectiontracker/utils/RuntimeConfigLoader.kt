package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object RuntimeConfigLoader {

    private val logger: Logger = LogManager.getLogger(RuntimeConfigLoader::class)

    fun loadConsumableDurations() {
        ConsumableParser.loadConsumableDurations()
        logger.info("[SCT]: Loaded all consumable durations.")
    }
}