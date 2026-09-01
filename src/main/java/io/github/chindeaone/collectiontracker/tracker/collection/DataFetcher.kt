package io.github.chindeaone.collectiontracker.tracker.collection

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.gui.CustomCollectionScreen
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isTracking
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

object DataFetcher {

    private val collectionCache: MutableMap<String, Long> = mutableMapOf()
    private val cacheTimestamps: MutableMap<String, Long> = mutableMapOf()
    private val leaderboardCacheTimestamps: MutableMap<String, Long> = mutableMapOf()
    private const val CACHE_LIFESPAN_MS = 240_000L // 4 minutes
    private const val LEADERBOARD_CACHE_LIFESPAN_MS = 3_600_000L // 1 hour
    @OptIn(ExperimentalAtomicApi::class)
    private val leaderboardFetchInProgress: AtomicBoolean = AtomicBoolean(false)

    private val logger: Logger = LogManager.getLogger(DataFetcher::class.java)

    fun fetchData(isInitialFetch: Boolean) {
        logger.info("[SCT]: Fetching collection data")

        try {
            if (!ServerUtils.serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the tracker.")
                TrackingHandler.stopTracking()
                return
            }
            if (!isInitialFetch && !isTracking) return

            var collectionData = getCachedData(collection)
            if (collectionData == null) {
                fetchDataFromApi(collection).thenAccept { jsonData ->
                    if (jsonData == null) {
                        logger.error("[SCT]: Failed to fetch data from the Hypixel API")

                        if (ConfigAccess.isApiTrackingEnabled()) {
                            CollectionTracker.cancelScheduledTask()
                        }

                        if (isInitialFetch) {
                            Minecraft.getInstance().execute {
                                Minecraft.getInstance()
                                    ./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(
                                        CustomCollectionScreen(listOf(collection)) {
                                            CollectionsManager.resetCollections()
                                        }
                                    )
                            }
                        }
                        return@thenAccept
                    }
                    collectionData =
                        JsonParser.parseString(jsonData).getAsJsonObject().entrySet().iterator().next().value.asLong

                    collectionCache[collection] = collectionData
                    cacheTimestamps[collection] = System.currentTimeMillis()

                    if (isInitialFetch) {
                        TrackingRates.setCollection(collectionData)
                    } else {
                        TrackingRates.updateCollection(collectionData)
                    }
                    logger.info("[SCT]: Data successfully fetched for collection: {}", collection)
                }.exceptionally { throwable ->
                    logger.error("[SCT]: Error fetching data from the Hypixel API: {}", throwable.message, throwable)
                    null
                }
            } else {
                if (isInitialFetch) {
                    TrackingRates.setCollection(collectionData)
                } else {
                    TrackingRates.updateCollection(collectionData)
                }
                logger.info("[SCT]: Data successfully retrieved for collection: {}", collection)
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: {}", e.message, e)
        }
    }

    private fun getCachedData(collection: String): Long? {
        val lastFetched = cacheTimestamps[collection]

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Returning cached data for collection: {} (last fetched {} ms ago)", collection, elapsed)
            return collectionCache[collection]
        }
        return null
    }

    private fun fetchDataFromApi(collection: String): CompletableFuture<String?> {
        val lastFetched = cacheTimestamps[collection]

        if (lastFetched != null) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info("[SCT]: Cache expired for collection {} (last fetched {} ms ago). Fetching new data.", collection, elapsed)
        } else {
            logger.info("[SCT]: No cache present for collection {}. Fetching data.", collection)
        }

        return HypixelApiFetcher.fetchJsonData(collection)
    }

    fun clearCollectionCache() {
        collectionCache.clear()
        cacheTimestamps.clear()
        logger.info("[SCT]: Collection data caches have been cleared.")
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun clearAllCache() {
        clearCollectionCache()
        leaderboardFetchInProgress.store(false)
        leaderboardCacheTimestamps.clear()
        LeaderboardManager.clear()
        logger.info("[SCT]: All data caches, including leaderboard, have been cleared.")
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun fetchLeaderboardData(targetCollection: String) {
        if (targetCollection.isEmpty()) return
        if (!ConfigAccess.isCollectionLeaderboardEnabled()) return
        if (!leaderboardFetchInProgress.compareAndSet(expectedValue = false, newValue = true)) return

        try {
            val lastFetched = leaderboardCacheTimestamps[targetCollection]
            if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < LEADERBOARD_CACHE_LIFESPAN_MS) {
                return
            }
            logger.info("[SCT]: Fetching leaderboard data for collection: {}", targetCollection)

            EliteApiFetcher.fetchCollectionLeaderboard(targetCollection).thenAccept { jsonData ->
                if (jsonData == null) {
                    logger.error("[SCT]: Failed to fetch leaderboard data from the Elite API")
                    ChatUtils.sendMessage("§cFailed to fetch leaderboard data.", true)
                    return@thenAccept
                }

                val jsonObject = JsonParser.parseString(jsonData).getAsJsonObject()
                val entriesArray = jsonObject.getAsJsonArray("entries")
                val entries = ArrayList<LeaderboardEntry>(entriesArray.size())

                for (i in 0 until entriesArray.size()) {
                    val entryObject = entriesArray.get(i).getAsJsonObject()
                    if (entryObject.get("username").asString.equals(PlayerData.playerName, ignoreCase = true)) continue

                    entries.add(LeaderboardEntry(
                            entryObject.get("username").asString,
                            entryObject.get("rank").asInt,
                            entryObject.get("amount").asLong,
                            ConfigAccess.isIncludeWipedProfilesEnabled() && entryObject.get("wiped").asBoolean
                    ))
                }
                LeaderboardManager.set(entries)
                leaderboardCacheTimestamps[targetCollection] = System.currentTimeMillis()
                logger.info("[SCT]: Leaderboard data successfully fetched and updated for collection: {}", targetCollection)
            }.exceptionally { throwable ->
                logger.error("[SCT]: Error fetching leaderboard data: {}", throwable.message, throwable)
                return@exceptionally null
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching leaderboard data: {}", e.message, e)
        } finally {
            leaderboardFetchInProgress.store(false)
        }
    }
}