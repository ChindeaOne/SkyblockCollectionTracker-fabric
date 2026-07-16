package io.github.chindeaone.collectiontracker.api.tokenapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object TokenFetcher {

    private val logger: Logger = LogManager.getLogger(TokenFetcher::class.java)

    fun fetchToken(): String? {
        return try {
            val headers = listOf(
                "X-UUID" to PlayerData.playerUUID
            )
            val response = ApiManager.request("get-token", headers)

            if (response.statusCode() != 200) {
                logger.error("[SCT]: Failed to fetch token, response code: {}", response.statusCode())
                null
            } else {
                val json = JsonParser.parseString(response.body()).asJsonObject
                logger.info("[SCT]: Successfully fetched token")
                json["token"].asString
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to fetch token", e)
            return null
        }
    }
}
