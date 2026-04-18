package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler.isMultiPaused
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler.isMultiTracking
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentHashMap

object MultiDataFetcher {

    private val logger: Logger = LogManager.getLogger(MultiDataFetcher::class.java)

    private val collectionCache: MutableMap<CacheKey, Map<String, Long>> = ConcurrentHashMap<CacheKey, Map<String, Long>>()
    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private const val CACHE_LIFESPAN_MS: Long = 180000L // default 3 minutes

    fun fetchMultiCollectionData() {
        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the multi tracker.")
                MultiTrackingHandler.stopMultiTracking()
                return
            }
            if (!isMultiTracking || isMultiPaused) return

            var map = getCachedData()

            if (map == null) {
                val data = fetchDataFromApi()
                if (data == null) {
                    logger.error("[SCT]: Failed to fetch multi collection data from the Hypixel API.")
                    val newMap = mutableMapOf<String, Long>()

                    for (collection in CollectionTracker.collectionList) {
                        newMap[collection] = 0
                    }
                    MultiTrackingRates.setCollections(newMap)
                    return
                }

                val jsonData = JsonParser.parseString(data).asJsonObject
                val newMap = mutableMapOf<String, Long>()

                for (entry in jsonData.entrySet()) {
                    val collectionName = entry.key
                    val collectionValue = entry.value.asLong
                    newMap[collectionName] = collectionValue
                }
                map = newMap

                val uuid = PlayerData.playerUUID
                val collectionList = CollectionTracker.collectionList
                val cacheKey = CacheKey(uuid, collectionList)
                collectionCache[cacheKey] = map
                cacheTimestamps[cacheKey] = System.currentTimeMillis()
            }

            logger.info("[SCT]: Data successfully fetched or retrieved for player with UUID: {} and collections: {}", PlayerData.playerUUID, CollectionTracker.collectionList)
            MultiTrackingRates.setCollections(map)

        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: ${e.message}")
        }
    }

    private fun getCachedData(): Map<String, Long>? {
        val uuid = PlayerData.playerUUID
        val collectionList = CollectionTracker.collectionList

        val cacheKey = CacheKey(uuid, collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed: Long = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for player with UUID: {} and collections: {} (last fetched {} ms ago)", uuid, collectionList, elapsed)
            return collectionCache[cacheKey]
        }
        return null
    }

    private fun fetchDataFromApi(): String? {
        val uuid = PlayerData.playerUUID
        val collectionList = CollectionTracker.collectionList

        val cacheKey = CacheKey(uuid, collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Cache expired for player {} collections {} (last fetched {} ms ago). Fetching new data.", uuid, collectionList, elapsed)
        } else {
            logger.info("[SCT]: No cache present for player {} collections {}. Fetching data.", uuid, collectionList)
        }

        return HypixelApiFetcher.fetchMultiJsonData()
    }

    fun clearCache() {
        collectionCache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Multi collection data cache cleared.")
    }

    private data class CacheKey(val uuid: String, val collectionList: List<String>)
}