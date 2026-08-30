package io.github.chindeaone.collectiontracker.api.hypixelapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.utils.PlayerData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object HypixelApiFetcher {

    private val logger: Logger = LogManager.getLogger(HypixelApiFetcher::class.java)

    @JvmStatic
    fun fetchJsonData(collection: String): String? {
        try {
            val response = requestHelper(collection, CollectionsManager.collectionSource)

            return when (response.statusCode()) {
                200 -> response.body()
                
                404 -> {
                    logger.warn("[SCT]: Collection API disabled in game.")
                    null
                }
                
                else -> {
                    logger.error("[SCT]: Failed to fetch collection data. HTTP {}", response.statusCode())
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while fetching data from the server", e)
            return null
        }
    }

    fun fetchMultiJsonData(): String? {
        try {
            val collection: String = CollectionTracker.collectionList.joinToString()
            val collectionSource: String = CollectionsManager.multiCollectionSource.joinToString()

            val response = requestHelper(collection, collectionSource)

            return when (response.statusCode()) {
                200 -> response.body()
                
                404 -> {
                    logger.warn("[SCT]: Collection API disabled in game.")
                    null
                }

                else -> {
                    logger.error("[SCT]: Failed to fetch multi-collection data. HTTP {}", response.statusCode())
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while fetching multi-collection data from the server", e)
        }
        return null
    }

    private fun requestHelper(collection: String, collectionSource: String) =
        ApiManager.request(
            "hypixelapi",
            mapOf(
                "Authorization" to "Bearer ${TokenManager.token}",
                "X-UUID" to PlayerData.playerUUID,
                "X-COLLECTION" to collection,
                "X-SOURCE" to collectionSource
            )
        )
}