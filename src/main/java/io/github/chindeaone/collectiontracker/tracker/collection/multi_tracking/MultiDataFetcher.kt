package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.gui.CustomCollectionScreen
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler.isMultiPaused
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler.isMultiTracking
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentHashMap

object MultiDataFetcher {

    private val logger: Logger = LogManager.getLogger(MultiDataFetcher::class.java)

    private val collectionCache: MutableMap<CacheKey, Map<String, Long>> = ConcurrentHashMap<CacheKey, Map<String, Long>>()
    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private const val CACHE_LIFESPAN_MS: Long = 240_000L // default 4 minutes

    fun fetchMultiCollectionData(isInitialFetch: Boolean = true) {
        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the multi tracker.")
                MultiTrackingHandler.stopMultiTracking()
                return
            }
            if (!isInitialFetch && (!isMultiTracking || isMultiPaused)) return

            var map = getCachedData()

            if (map == null) {
                val data = fetchDataFromApi()
                if (data == null) {
                    logger.error("[SCT]: Failed to fetch multi collection data from the Hypixel API.")

                    if (ConfigAccess.isApiTrackingEnabled()) {
                        CollectionTracker.cancelScheduledTask()
                    }

                    if (isInitialFetch) {
                        Minecraft.getInstance().execute {
                            Minecraft.getInstance()./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(
                                CustomCollectionScreen(CollectionTracker.collectionList) {
                                    CollectionsManager.multiCollectionSource.clear()
                                }
                            )
                        }
                    }
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

                val collectionList = CollectionTracker.collectionList
                val cacheKey = CacheKey(collectionList)
                collectionCache[cacheKey] = map
                cacheTimestamps[cacheKey] = System.currentTimeMillis()
            }

            logger.info("[SCT]: Data successfully fetched or retrieved for player with and collections: {}", CollectionTracker.collectionList)

            if (isInitialFetch) {
                MultiTrackingRates.setCollections(map)
            } else {
                MultiTrackingRates.updateCollections(map)
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: ${e.message}")
        }
    }

    private fun getCachedData(): Map<String, Long>? {
        val collectionList = CollectionTracker.collectionList

        val cacheKey = CacheKey(collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed: Long = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for collections: {} (last fetched {} ms ago)",  collectionList, elapsed)
            return collectionCache[cacheKey]
        }
        return null
    }

    private fun fetchDataFromApi(): String? {
        val collectionList = CollectionTracker.collectionList

        val cacheKey = CacheKey(collectionList)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Cache expired for collections {} (last fetched {} ms ago). Fetching new data.", collectionList, elapsed)
        } else {
            logger.info("[SCT]: No cache present for collections {}. Fetching data.", collectionList)
        }

        return HypixelApiFetcher.fetchMultiJsonData()
    }

    fun clearCache() {
        collectionCache.clear()
        cacheTimestamps.clear()
        DataFetcher.clearAllCache()
        logger.info("[SCT]: Multi collection data cache cleared.")
    }

    private data class CacheKey(val collectionList: List<String>)
}