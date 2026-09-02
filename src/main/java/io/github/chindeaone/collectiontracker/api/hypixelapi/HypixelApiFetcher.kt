package io.github.chindeaone.collectiontracker.api.hypixelapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.utils.PlayerData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object HypixelApiFetcher {

    private val logger: Logger = LogManager.getLogger(HypixelApiFetcher::class.java)

    fun fetchJsonData(collection: String): CompletableFuture<String?> {
        return ApiManager.requestAsync("hypixelapi", headers(collection, CollectionsManager.collectionSource ?: ""))
            .thenApply { response ->
                when (response.statusCode()) {
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
            }
            .exceptionally { e ->
                logger.error("[SCT]: An error occurred while fetching data from the server", e)
                null
            }
    }

    fun fetchMultiJsonData(): CompletableFuture<String?> {
        return ApiManager.requestAsync("hypixelapi", headers(CollectionTracker.collectionList.joinToString(), CollectionsManager.multiCollectionSource.joinToString()))
            .thenApply { response ->
                when (response.statusCode()) {
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
            }
            .exceptionally { e ->
                logger.error("[SCT]: An error occurred while fetching multi-collection data from the server", e)
                null
            }
    }

    private fun headers(collection: String, collectionSource: String) = mapOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to PlayerData.playerUUID,
        "X-COLLECTION" to collection,
        "X-SOURCE" to collectionSource
    )
}