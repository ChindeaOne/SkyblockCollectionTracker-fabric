package io.github.chindeaone.collectiontracker.tracker.skills

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher.fetchCollectionLeaderboard
import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher.fetchSkillsData
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isIncludeWipedProfilesEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isSkillLeaderboardEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isTamingTrackingEnabled
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardEntry
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager
import io.github.chindeaone.collectiontracker.utils.PlayerData.playerName
import io.github.chindeaone.collectiontracker.utils.PlayerData.playerUUID
import io.github.chindeaone.collectiontracker.utils.ServerUtils.serverStatus
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.function.Function

object SkillFetcher {
    private val logger: Logger = LogManager.getLogger(SkillFetcher::class.java)

    private val cacheTimestamps: MutableMap<CacheKey, Long> = ConcurrentHashMap<CacheKey, Long>()
    private val leaderboardCacheTimestamps: MutableMap<String, Long> = ConcurrentHashMap<String, Long>()
    private val skillLeaderboardFetchInProgress: MutableMap<String, AtomicBoolean> = ConcurrentHashMap<String, AtomicBoolean>()
    private const val CACHE_LIFESPAN_MS = 180000L // default 3 minutes
    private const val LEADERBOARD_CACHE_LIFESPAN_MS = 3600000L // 1 hour

    var scheduler: ScheduledExecutorService? = null

    fun scheduleSkillFetch(isSkillMaxed: Boolean, value: Long, skillName: String) {

        // initial delay of 5 mins because data is already fetched when tracking starts
        scheduler!!.scheduleAtFixedRate(
            { fetchSkillData(skillName, isSkillMaxed) },
            5,
            5,
            TimeUnit.MINUTES
        )
        // because of that, manual call is needed
        if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(value) // only if skill isn't maxed, as maxed skills use chat messages to track

        SkillTrackingRates.calculateTamingRates(SkillUtils.getTamingValue().toLong())
        logger.info("[SCT]: Skill data fetching scheduled to run every 5 minutes")
    }

    private fun fetchSkillData(skillName: String, isSkillMaxed: Boolean) {
        try {
            if (!serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the skill tracker.")
                SkillTrackingHandler.stopTracking()
                return
            }

            if (!SkillTrackingHandler.isTracking) return
            if (SkillTrackingHandler.isPaused) return


            getData(playerUUID, skillName) // fetch data for the tracked skill

            // Skill leaderboard fetching
            fetchSkillLeaderboardData(skillName)
            if (isTamingTrackingEnabled()) {
                fetchSkillLeaderboardData("Taming")
            }

            val skillXp = SkillUtils.getSkillValue(skillName) // get the XP of the tracked skill again here

            if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(skillXp?.toLong() ?: 0L) // only if skill isn't maxed, as maxed skills use chat messages to track

            SkillTrackingRates.calculateTamingRates(SkillUtils.getTamingValue().toLong())
        } catch (e: Exception) {
            logger.error("[SCT]: Error while fetching data from the Hypixel API", e)
        }
    }

    fun fetchSkillLeaderboardData(skillName: String) {
        if (skillName.isEmpty()) return
        if (!isSkillLeaderboardEnabled()) return

        val inProgress =
            skillLeaderboardFetchInProgress.computeIfAbsent(skillName.lowercase(Locale.getDefault())) { `_`: String? ->
                AtomicBoolean(false)
            }
        if (!inProgress.compareAndSet(false, true)) return

        try {
            val lastFetched = leaderboardCacheTimestamps[skillName.lowercase(Locale.getDefault())]
            if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < LEADERBOARD_CACHE_LIFESPAN_MS) {
                return
            }
            logger.info("[SCT]: Fetching leaderboard data for skill: {}", skillName)

            fetchCollectionLeaderboard(skillName.lowercase(Locale.getDefault())).thenAccept(Consumer { jsonData: String? ->
                if (jsonData == null) {
                    logger.error("[SCT]: Failed to fetch leaderboard data for skill {} from the Elite API", skillName)
                    return@Consumer
                }
                val jsonObject = JsonParser.parseString(jsonData).getAsJsonObject()
                val entriesArray = jsonObject.getAsJsonArray("entries")
                val entries: MutableList<LeaderboardEntry> = ArrayList<LeaderboardEntry>(entriesArray.size())

                for (i in 0..<entriesArray.size()) {
                    val entryObject = entriesArray.get(i).getAsJsonObject()
                    val username = entryObject.get("username").asString
                    if (username.equals(playerName, ignoreCase = true)) continue

                    entries.add(
                        LeaderboardEntry(
                            username,
                            entryObject.get("rank").asInt,
                            entryObject.get("amount").asLong,
                            isIncludeWipedProfilesEnabled() && entryObject.get("wiped").asBoolean
                        )
                    )
                }

                LeaderboardManager.setSkillLeaderboard(skillName, entries)
                leaderboardCacheTimestamps[skillName.lowercase()] = System.currentTimeMillis()
                logger.info("[SCT]: Leaderboard data successfully fetched and updated for skill: {}", skillName)
            }).exceptionally(Function { ex: Throwable? ->
                logger.error(
                    "[SCT]: Exception occurred while fetching leaderboard data for skill {}: {}",
                    skillName,
                    ex!!.message,
                    ex
                )
                null
            })
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching skill leaderboard data for {}: {}", skillName, e.message, e)
        } finally {
            inProgress.set(false)
        }
    }

    private fun getData(playerUUID: String, skill: String) {
        val cacheKey = CacheKey(playerUUID, skill)
        val now = System.currentTimeMillis()
        val lastFetched = cacheTimestamps[cacheKey]

        if (lastFetched != null && (now - lastFetched) < CACHE_LIFESPAN_MS) {
            val elapsed = System.currentTimeMillis() - lastFetched
            logger.info(
                "[SCT]: Using cached data for player {} skill {} (last fetched {} ms ago).",
                playerUUID,
                skill,
                elapsed
            )
        }

        if (lastFetched != null) {
            val elapsed = now - lastFetched
            logger.info(
                "[SCT]: Cache expired for player: {} and skill: {} (last fetched {} ms ago). Fetching new data.",
                playerUUID,
                skill,
                elapsed
            )
        } else {
            logger.info("[SCT]: No cache present for player: {} and skill: {}. Fetching data.", playerUUID, skill)
        }

        fetchSkillsData()
        cacheTimestamps[cacheKey] = now
    }

    fun clearCache() {
        cacheTimestamps.clear()
        leaderboardCacheTimestamps.clear()
        skillLeaderboardFetchInProgress.clear()
        logger.info("[SCT]: All skill data caches have been cleared.")
    }

    private data class CacheKey(val uuid: String, val skill: String)
}