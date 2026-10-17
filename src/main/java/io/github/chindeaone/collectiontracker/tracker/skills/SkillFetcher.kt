package io.github.chindeaone.collectiontracker.tracker.skills

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher.fetchCollectionLeaderboard
import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher.fetchSkillsData
import io.github.chindeaone.collectiontracker.config.includeWipedProfiles
import io.github.chindeaone.collectiontracker.config.skillLeaderboard
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardEntry
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils.serverStatus
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object SkillFetcher {
    private val logger: Logger = LogManager.getLogger(SkillFetcher::class.java)

    private val cacheTimestamps: MutableMap<String, EpochMark> = ConcurrentHashMap()
    private val leaderboardCacheTimestamps: MutableMap<String, EpochMark> = ConcurrentHashMap()
    private val skillLeaderboardFetchInProgress: MutableMap<String, AtomicBoolean> = ConcurrentHashMap()
    private val CACHE_LIFESPAN = 4.minutes
    private val LEADERBOARD_CACHE_LIFESPAN = 1.hours

    var scheduler: ScheduledExecutorService? = null

    fun scheduleSkillFetch(isSkillMaxed: Boolean, value: Long, skillName: String) {
        // initial delay of 5 mins because data is already fetched when tracking starts
        scheduler?.scheduleAtFixedRate({ fetchSkillData(skillName, isSkillMaxed) }, 5, 5, TimeUnit.MINUTES)
        // because of that, manual call is needed
        if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(value) // only if skill isn't maxed, as maxed skills use chat messages to track

        SkillTrackingRates.calculateTamingRates(SkillUtils.tamingValue.toLong())
        logger.info("[SCT]: Skill data fetching scheduled to run every 5 minutes")
    }

    private fun fetchSkillData(skillName: String, isSkillMaxed: Boolean) {
        try {
            if (!serverStatus) {
                logger.warn("[SCT]: API server not online. Stopping the skill tracker.")
                SkillTrackingHandler.stopTracking()
                return
            }

            if (!SkillTrackingHandler.isTracking || SkillTrackingHandler.isPaused) return

            getData(skillName) // fetch data for the tracked skill

            val skillXp = SkillUtils.getSkillValue(skillName) // get the XP of the tracked skill again here

            if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(skillXp?.toLong() ?: 0L) // only if skill isn't maxed, as maxed skills use chat messages to track

            SkillTrackingRates.calculateTamingRates(SkillUtils.tamingValue.toLong())
        } catch (e: Exception) {
            logger.error("[SCT]: Error while fetching data from the Hypixel API", e)
        }
    }

    fun fetchSkillLeaderboardData(skillName: String): CompletableFuture<Void> {
        if (skillName.isEmpty()) return CompletableFuture.completedFuture(null)
        if (!skillLeaderboard) return CompletableFuture.completedFuture(null)

        val inProgress = skillLeaderboardFetchInProgress.computeIfAbsent(skillName.lowercase()) { `_`: String? -> AtomicBoolean(false) }
        if (!inProgress.compareAndSet(false, true)) return CompletableFuture.completedFuture(null)

        return try {
            val lastFetched = leaderboardCacheTimestamps[skillName.lowercase()]
            if (lastFetched != null && lastFetched.timePassedSince() < LEADERBOARD_CACHE_LIFESPAN) {
                inProgress.set(false)
                return CompletableFuture.completedFuture(null)
            } else {
                logger.info("[SCT]: Fetching leaderboard data for $skillName.")

                fetchCollectionLeaderboard(skillName.lowercase()).thenAccept { jsonData: String? ->
                    if (jsonData == null) {
                        logger.error("[SCT]: Failed to fetch leaderboard data for skill $skillName from the Elite API")
                        ChatUtils.sendMessage("§c[SCT] Failed to fetch leaderboard data for $skillName.")
                        return@thenAccept
                    }
                    val jsonObject = JsonParser.parseString(jsonData).getAsJsonObject()
                    val entriesArray = jsonObject.getAsJsonArray("entries")
                    val entries: MutableList<LeaderboardEntry> = ArrayList<LeaderboardEntry>(entriesArray.size())

                    for (i in 0..<entriesArray.size()) {
                        val entryObject = entriesArray.get(i).getAsJsonObject()
                        val username = entryObject.get("username").asString
                        if (username.equals(PlayerData.playerName, ignoreCase = true)) continue

                        entries.add(
                            LeaderboardEntry(
                                username,
                                entryObject.get("rank").asInt,
                                entryObject.get("amount").asLong,
                                includeWipedProfiles && entryObject.get("wiped").asBoolean
                            )
                        )
                    }

                    LeaderboardManager.setSkillLeaderboard(skillName, entries)
                    leaderboardCacheTimestamps[skillName.lowercase()] = EpochMark.now()
                    logger.info("[SCT]: Leaderboard data successfully fetched and updated for $skillName")
                }.exceptionally { ex: Throwable ->
                    logger.error("[SCT]: Exception occurred while fetching leaderboard data for $skillName: ${ex.message}",ex)
                    null
                }.whenComplete { _, _ ->
                    inProgress.set(false)
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching skill leaderboard data for $skillName: ${e.message}", e)
            inProgress.set(false)
            CompletableFuture.completedFuture(null)
        }
    }

    private fun getData(skill: String) {
        val lastFetched = cacheTimestamps[skill]

        if (lastFetched != null && lastFetched.timePassedSince() < CACHE_LIFESPAN) {
            logger.info("[SCT]: Using cached data for $skill (last fetched ${lastFetched.timePassedSince().inWholeSeconds} seconds ago).")
        }

        if (lastFetched != null) {
            logger.info("[SCT]: Cache expired for $skill (last fetched ${lastFetched.timePassedSince().inWholeSeconds} seconds ago). Fetching new data.")
        } else {
            logger.info("[SCT]: No cache present for $skill. Fetching data.")
        }

        fetchSkillsData()
        cacheTimestamps[skill] = EpochMark.now()
    }

    fun clearCache() {
        cacheTimestamps.clear()
        skillLeaderboardFetchInProgress.clear()
        logger.info("[SCT]: All skill data caches have been cleared.")
    }
}