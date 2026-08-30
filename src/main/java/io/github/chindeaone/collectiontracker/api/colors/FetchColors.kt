package io.github.chindeaone.collectiontracker.api.colors

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchColors {

    private val logger: Logger = LogManager.getLogger(FetchColors::class.java)
    @Volatile
    var hasColors: Boolean = false

    fun fetchColorsData() {
        ApiManager.requestAsync("collection-colors")
            .thenAccept { response ->
                if (response.statusCode() == 200) {
                    val json = JsonParser.parseString(response.body()).asJsonObject
                    ColorUtils.setupColors(json)
                    hasColors = true
                    logger.info("[SCT]: Successfully fetched colors data.")
                } else {
                    logger.error("[SCT]: Failed to fetch colors data. Server responded with code: {}", response.statusCode())
                }
            }.exceptionally { e ->
                logger.error("[SCT]: An error occurred while fetching colors data: ", e)
                null
            }
    }
}