package io.github.chindeaone.collectiontracker.api.waypointsapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchWaypoints {

    private val logger: Logger = LogManager.getLogger(FetchWaypoints::class.java)
    @Volatile
    var hasWaypoints: Boolean = false

    fun fetchWaypoints() {
        try {
            val response = ApiManager.request("waypoints", listOf())

            if (response.statusCode() == 200) {
                val json = JsonParser.parseString(response.body()).asJsonObject
                WaypointsUtils.setWaypoints(json)
                hasWaypoints = true
                logger.info("[SCT]: Successfully fetched waypoints.")
            } else {
                logger.error("[SCT]: Failed to fetch waypoints. Server responded with code: {}", response.statusCode())
            }
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while fetching waypoints: ", e)
        }
    }
}
