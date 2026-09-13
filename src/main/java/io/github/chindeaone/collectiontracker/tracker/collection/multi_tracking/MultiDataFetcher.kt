package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.rendering.screen.CustomCollectionScreen
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler.isMultiTracking
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object MultiDataFetcher {

    private val logger: Logger = LogManager.getLogger(MultiDataFetcher::class.java)

    private val collectionCache: MutableMap<CacheKey, Map<String, Long>> = ConcurrentHashMap<CacheKey, Map<String, Long>>()
    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private const val CACHE_LIFESPAN_MS: Long = 240_000L // default 4 minutes

    fun fetchMultiCollectionData(isInitialFetch: Boolean = true) {
        if (!ServerUtils.serverStatus) {
            logger.warn("[SCT]: API server not online. Stopping the multi tracker.")
            MultiTrackingHandler.stopMultiTracking()
            return
        }
        if (!isInitialFetch && !isMultiTracking) return

        val collectionList = CollectionTracker.collectionList
        var map = getCachedData(collectionList)
        if (map == null) {
            fetchDataFromApi(collectionList).thenAccept { data ->
                if (data == null) {
                    logger.error("[SCT]: Failed to fetch multi collection data from the Hypixel API.")

                    if (ConfigAccess.isApiTrackingEnabled()) {
                        CollectionTracker.cancelScheduledTask()
                    }

                    if (isInitialFetch) {
                        MinecraftUtils.runOnClientThread {
                            MinecraftUtils.setScreen(CustomCollectionScreen(CollectionTracker.collectionList) {
                                CollectionsManager.resetMultiCollections()
                            })
                        }
                    }
                    return@thenAccept
                }

                val jsonData = JsonParser.parseString(data).asJsonObject
                val newMap = mutableMapOf<String, Long>()

                for (entry in jsonData.entrySet()) {
                    val collectionName = entry.key
                    val collectionValue = entry.value.asLong
                    newMap[collectionName] = collectionValue
                }
                map = newMap

                val cacheKey = CacheKey(collectionList)
                collectionCache[cacheKey] = map
                cacheTimestamps[cacheKey] = System.currentTimeMillis()

                if (isInitialFetch) {
                    MultiTrackingRates.setCollections(map)
                } else {
                    MultiTrackingRates.updateCollections(map)
                }

                logger.info("[SCT]: Data successfully fetched for collections: $collectionList")
            }.exceptionally { e ->
                logger.error("[SCT]: An error occurred while fetching multi collection data from the Hypixel API: ${e.message}")
                null
            }
        } else {
            if (isInitialFetch) {
                MultiTrackingRates.setCollections(map)
            } else {
                MultiTrackingRates.updateCollections(map)
            }
            logger.info("[SCT]: Data successfully retrieved for collections: $collectionList")
        }
    }

    private fun getCachedData(collectionList: List<String>): Map<String, Long>? {
        val cacheKey = CacheKey(collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed: Long = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for collections: $collectionList (last fetched $elapsed ms ago)")
            return collectionCache[cacheKey]
        }
        return null
    }

    private fun fetchDataFromApi(collectionList: List<String>): CompletableFuture<String?> {
        val cacheKey = CacheKey(collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Cache expired for collections $collectionList (last fetched $elapsed ms ago). Fetching new data.")
        } else {
            logger.info("[SCT]: No cache present for collections $collectionList. Fetching data.")
        }

        return HypixelApiFetcher.fetchMultiJsonData()
    }

    fun clearCollectionCache() {
        collectionCache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Multi collection data cache cleared.")
    }

    fun clearAllCache() {
        clearCollectionCache()
        DataFetcher.clearAllCache()
        logger.info("[SCT]: All caches cleared.")
    }

    private data class CacheKey(val collectionList: List<String>)
}