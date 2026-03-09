package io.github.chindeaone.collectiontracker.tracker.coleweight

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.set

object ColeweightDataFetcher {
    private val logger = LogManager.getLogger(ColeweightDataFetcher::class.java)

    private val cache: MutableMap<CacheKey, String> = ConcurrentHashMap<CacheKey, String>()
    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private const val CACHE_LIFESPAN_MS: Long = 180000L // default 3 minutes
    var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun scheduleFetch() {
        scheduler.scheduleAtFixedRate({ fetchData() }, 1, 200, TimeUnit.SECONDS)
        logger.info("[SCT]: Coleweight fetching scheduled every 200 seconds.")
    }

    private fun fetchData() {
        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping Coleweight tracker.")
                ColeweightTrackingHandler.stopTracking()
                return
            }

            if (!ColeweightTrackingHandler.isTracking || ColeweightTrackingHandler.isPaused) return

            val data = getData()
            if (data.isEmpty()) {
                logger.error("[SCT]: Failed to fetch Coleweight data")
                return
            }
            val coleweight = JsonParser.parseString(data).asJsonObject.get("coleweight").asFloat

            logger.info("[SCT]: Coleweight data fetched successfully: $coleweight")
            ColeweightTrackingRates.calculateRates(coleweight)

        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Coleweight data: ${e.message}", e)
        }
    }

    @JvmStatic
    fun getData(): String {
        val uuid = PlayerData.playerUUID

        val cacheKey = CacheKey(uuid)
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed: Long = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for player with UUID: {} (cache age: {} ms)", uuid, elapsed)
            return cache[cacheKey] ?: ""
        }

        val jsonData = ColeweightFetcher.fetchColeweightData()

        if (!jsonData.isNullOrEmpty()) {
            cache[cacheKey] = jsonData
            cacheTimestamps[cacheKey] = System.currentTimeMillis()
            return jsonData
        }

        return ""
    }

    fun clearCache() {
        cache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Coleweight data cache cleared.")
    }

    private data class CacheKey(val uuid: String)
}