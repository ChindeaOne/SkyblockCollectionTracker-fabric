package io.github.chindeaone.collectiontracker.tracker.coleweight

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object ColeweightDataFetcher {
    private val logger = LogManager.getLogger(ColeweightDataFetcher::class.java)

    private val cache: MutableMap<String, String> = ConcurrentHashMap()
    private val cacheTimestamps: MutableMap<String, EpochMark> = ConcurrentHashMap()
    private val CACHE_LIFESPAN: Duration = 4.minutes
    var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun scheduleFetch() {
        scheduler.scheduleAtFixedRate({ fetchData() }, 1, 300, TimeUnit.SECONDS)
        logger.info("[SCT]: Coleweight fetching scheduled every 5 minutes.")
    }

    private fun fetchData() {
        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping Coleweight tracker.")
                ColeweightTrackingHandler.stopTracking()
                return
            }

            if (!ColeweightTrackingHandler.isTracking || ColeweightTrackingHandler.isPaused) return

            getData().thenAccept { data ->
                if (data.isEmpty()) {
                    logger.error("[SCT]: Failed to fetch Coleweight data")
                    return@thenAccept
                }

                try {
                    val coleweight = JsonParser.parseString(data).asJsonObject.get("coleweight").asFloat

                    logger.info("[SCT]: Coleweight data fetched successfully: $coleweight")
                    ColeweightTrackingRates.calculateRates(coleweight)
                } catch (e: Exception) {
                    logger.error("[SCT]: Error parsing Coleweight data: ${e.message}", e)
                    return@thenAccept
                }
            }.exceptionally { e ->
                logger.error("[SCT]: Error fetching Coleweight data: ${e.message}", e)
                null
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Coleweight data: ${e.message}", e)
        }
    }

    fun getData(): CompletableFuture<String> {
        val uuid = PlayerData.playerUUID
        val name = PlayerData.playerName

        val lastFetched = cacheTimestamps[uuid]
        if (lastFetched != null && lastFetched.timePassedSince() < CACHE_LIFESPAN) {
            logger.info("[SCT]: Returning cached data for player with UUID: $uuid (cache age: ${lastFetched.timePassedSince().inWholeSeconds} seconds)")
            return CompletableFuture.completedFuture(cache[uuid] ?: "")
        }

        return ColeweightFetcher.fetchColeweightData(name, uuid)
            .thenApply { data ->
                if (data.isNullOrEmpty()) {
                    logger.warn("[SCT]: Received empty response when fetching Coleweight data for player: $name")
                    ""
                } else {
                    cache[uuid] = data
                    cacheTimestamps[uuid] = EpochMark.now()
                    data
                }
            }
            .exceptionally { e ->
                logger.error("[SCT]: Error fetching Coleweight data for player $name: ${e.message}")
                ""
            }
    }

    fun clearCache() {
        cache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Coleweight data cache cleared.")
    }
}