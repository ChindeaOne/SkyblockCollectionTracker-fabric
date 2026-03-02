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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

object MultiDataFetcher {

    private val logger: Logger = LogManager.getLogger(MultiDataFetcher::class.java)

    private val collectionCache: MutableMap<CacheKey, String> = ConcurrentHashMap<CacheKey, String>()
    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private const val CACHE_LIFESPAN_MS: Long = 180000L // default 3 minutes
    var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val period = 200.seconds

    fun scheduleMultiCollectionDataFetch() {
        scheduler.scheduleAtFixedRate(MultiDataFetcher::fetchMultiCollectionData, 1, period.inWholeSeconds, TimeUnit.SECONDS)
    }

    private fun fetchMultiCollectionData() {
        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the multi tracker.")
                MultiTrackingHandler.stopMultiTracking()
                return
            }
            if (!isMultiTracking) return
            if (isMultiPaused) return

            val data = getData()
            if (data.isEmpty()) {
                logger.error("[SCT]: Failed to fetch or retrieve multi collection data from the cache.")
                return
            }
            val jsonData = JsonParser.parseString(data).asJsonObject
            val map = mutableMapOf<String, Long>()

            for (entry in jsonData.entrySet()) {
                val collectionName = entry.key
                val collectionValue = entry.value.asLong
                map[collectionName] = collectionValue
            }

            logger.info("[SCT]: Data successfully fetched or retrieved and displayed for player with UUID: {} and collections: {}", PlayerData.playerUUID, CollectionTracker.collectionList)
            MultiTrackingRates.calculateMultiRates(map)

        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: ${e.message}")
        }
    }

    private fun getData(): String {
        val uuid = PlayerData.playerUUID
        val collectionList = CollectionTracker.collectionList

        val cacheKey = CacheKey(uuid, collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed: Long = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for player with UUID: {} and collections: {} (last fetched {} ms ago)", uuid, collectionList, elapsed)
            return collectionCache[cacheKey]!!
        }

        if (lastFetched != null) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Cache expired for player {} collections {} (last fetched {} ms ago). Fetching new data.", uuid, collectionList, elapsed)
        } else {
            logger.info("[SCT]: No cache present for player {} collections {}. Fetching data.", uuid, collectionList)
        }

        val jsonData = HypixelApiFetcher.fetchMultiJsonData()

        if (jsonData != null) {
            collectionCache[cacheKey] = jsonData
            cacheTimestamps[cacheKey] = System.currentTimeMillis()
        }

        return jsonData
    }

    fun clearCache() {
        collectionCache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Multi collection data cache cleared.")
    }

    private data class CacheKey(val uuid: String, val collectionList: List<String>)
}