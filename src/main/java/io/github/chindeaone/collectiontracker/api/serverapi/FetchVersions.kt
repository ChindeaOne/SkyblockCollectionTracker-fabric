package io.github.chindeaone.collectiontracker.api.serverapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.VersionUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchVersions {

    private val logger: Logger = LogManager.getLogger(FetchVersions::class.java)

    @Volatile
    var hasVersions = false

    fun fetchVersions() {
        try {
            val response = ApiManager.request("versions", listOf())

            if (response.statusCode() == 200) {
                val jsonObject = JsonParser.parseString(response.body()).asJsonObject
                VersionUtils.parseVersions(jsonObject)

                hasVersions = true
                logger.info("[SCT]: Successfully fetched versions data.")
            } else {
                logger.error("[SCT]: Failed to fetch versions data. Server responded with code: {}", response.statusCode())
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Exception occurred while fetching versions data.", e)
        }
    }
}