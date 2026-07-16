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
        try {
            val response = ApiManager.request("color-codes", listOf())
            
            if (response.statusCode() != 200) {
                logger.error("[SCT]: Failed to fetch colors data. Server responded with code: {}", response.statusCode())
            } else {
                val json = JsonParser.parseString(response.body()).asJsonObject
                ColorUtils.setupColors(json)
                hasColors = true
                logger.info("[SCT]: Successfully fetched colors data.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while fetching colors data: ", e)
        }
    }
}