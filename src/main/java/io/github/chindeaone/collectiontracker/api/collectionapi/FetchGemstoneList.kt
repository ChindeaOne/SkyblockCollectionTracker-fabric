package io.github.chindeaone.collectiontracker.api.collectionapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchGemstoneList {

    private val logger: Logger = LogManager.getLogger(FetchGemstoneList::class.java)
    @Volatile
    var hasGemstoneList: Boolean = false

    fun fetchGemstoneList() {
        ApiManager.requestAsync("gemstones")
            .thenAccept { response ->
                if (response.statusCode() == 200) {
                    val json = JsonParser.parseString(response.body()).asJsonObject
                    GemstonesManager.gemstones = json.keySet().toMutableList()

                    hasGemstoneList = true
                    logger.info("[SCT]: Successfully received the gemstone list.")
                } else {
                    logger.error("[SCT]: Failed to fetch gemstone list. HTTP {}", response.statusCode())
                }
            }.exceptionally { e ->
                logger.error("[SCT]: Error while receiving the gemstone list", e)
                null
            }
    }
}