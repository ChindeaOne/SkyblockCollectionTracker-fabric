package io.github.chindeaone.collectiontracker.tracker.skills

import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isSkillLeaderboardEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isTamingTrackingEnabled
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setSkillOverlayRendering
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.clearAllCache
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.clearCache
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.scheduleSkillFetch
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.scheduler
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object SkillTrackingHandler {
    private val logger: Logger = LogManager.getLogger(SkillTrackingHandler::class.java)

    @Volatile
    var isTracking: Boolean = false
    var isPaused: Boolean = false
    var leaderboardTrackingInitialized: Boolean = false

    var startTime: Long = 0
    private var lastTime: Long = 0
    private var lastTrackedTime: Long = 0
    private val TRACKING_INTERVAL = TimeUnit.SECONDS.toMillis(10) // 10 seconds

    private const val RESETS = 10
    private var restartCount = 0
    private var firstRestartTime: Long = 0

    var isSkillMaxed: Boolean = false

    fun startTracking() {
        val now = System.currentTimeMillis()

        if (now - lastTrackedTime < TRACKING_INTERVAL) {
            sendMessage("§cPlease wait a few seconds before tracking another skill!", true)
            return
        } else {
            sendMessage("§aTracking $skillName skill.", true)
        }

        if (scheduler == null || scheduler!!.isShutdown) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }

        initTracking(now)
        setSkillOverlayRendering(true)

        isSkillMaxed = SkillUtils.isSkillMaxed(skillName) == true
        val skillLevel = SkillUtils.getSkillLevel(skillName)
        val skillXp = SkillUtils.getSkillValue(skillName)

        SkillTrackingRates.initTracking(skillLevel ?: 0, skillXp?.toLong() ?: 0L)

        SkillTrackingRates.updateSkillLeaderboardStats()
        if (isTamingTrackingEnabled()) {
            SkillTrackingRates.updateTamingLeaderboardStats()
        }

        if (!isSkillMaxed || isTamingTrackingEnabled()) {
            // Track only via API
            scheduleSkillFetch(isSkillMaxed, skillXp?.toLong() ?: 0L, skillName)
        }
        logger.info("[SCT]: Started tracking skill: {}", skillName)
    }

    fun onSkillGain(value: Long, skillName: String?) {
        if (!isTracking || (SkillTracker.skillName != skillName) || !isSkillMaxed) return
        SkillTrackingRates.calculateSkillRates(value)
    }

    private fun initTracking(now: Long) {
        lastTrackedTime = now

        isTracking = true
        isPaused = false
        leaderboardTrackingInitialized = isSkillLeaderboardEnabled()

        startTime = now
        lastTime = 0
    }

    fun pauseTracking() {
        if (checkTracking()) return
        if (isPaused) {
            sendMessage("§cSkill tracking is already paused.", true)
            logger.warn("[SCT]: Skills tracking is already paused.")
            return
        }
        isPaused = true
        lastTime = (System.currentTimeMillis() - startTime) / 1000
        sendMessage("§7Paused tracking " + skillName.lowercase() + " skill.", true)
        logger.info("[SCT]: Pausing tracking skill: {}", skillName)
    }

    fun resumeTracking() {
        if (checkTracking()) return
        if (!isPaused) {
            sendMessage("§cSkill tracking is not paused.", true)
            logger.warn("[SCT]: Skills tracking is not paused.")
            return
        }
        isPaused = false
        startTime = System.currentTimeMillis()
        sendMessage("§7Resumed tracking " + skillName.lowercase() + " skill.", true)
        logger.info("[SCT]: Resuming tracking skill: {}", skillName)
    }

    fun stopTracking() {
        if (!isTracking) return

        if (!server) {
            logger.info("[SCT]: Tracking stopped because player disconnected from the server.")
        } else if (SkillTrackingRates.afk) {
            sendMessage("§cYou have been marked as AFK. Stopping the tracker.", true)
            logger.info("[SCT]: Tracking stopped because the player went AFK or the API server is down")
        } else {
            sendMessage("§cAPI server is down. Stopping the skill tracker.", true)
            logger.info("[SCT]: Skill tracking stopped because the API server is down.")
        }

        resetTrackingData(false)
    }

    fun stopTrackingManual() {
        if (checkTracking()) return

        resetTrackingData(false)

        sendMessage("§cStopped tracking " + skillName.lowercase() + " skill!", true)
        logger.info("[SCT]: Stopped tracking skill: {}", skillName)
    }

    private fun resetTrackingData(restart: Boolean) {
        if (scheduler != null) {
            if (!scheduler!!.isShutdown) {
                scheduler!!.shutdown()
            }
            try {
                if (!scheduler!!.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler!!.shutdownNow()
                }
            } catch (_: InterruptedException) {
                scheduler!!.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }

        isTracking = false
        isPaused = false
        leaderboardTrackingInitialized = false

        startTime = 0
        lastTime = 0

        val now = System.currentTimeMillis()
        lastTrackedTime = if (!restart) {
            now
        } else now - TRACKING_INTERVAL

        isSkillMaxed = false
        setSkillOverlayRendering(false)

        clearCache()
        clearAllCache()
        SkillTrackingRates.resetSession()
    }

    private fun checkTracking(): Boolean {
        if (!isTracking) {
            sendMessage("§cNo skill is being tracked currently!", true)
            logger.warn("[SCT]: No skill is being tracked currently.")
            return true
        }
        return false
    }

    fun restartTracking() {
        if (checkTracking()) return

        if (restartCount == 0) {
            firstRestartTime = System.currentTimeMillis()
        } else {
            val elapsedTime = System.currentTimeMillis() - firstRestartTime
            if (elapsedTime >= TimeUnit.HOURS.toMillis(1)) {
                restartCount = 0
                firstRestartTime = System.currentTimeMillis()
            }
        }

        if (restartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached. Please wait before restarting again.", true)
            logger.warn("[SCT]: Hourly restart limit reached for skill tracking.")
            return
        }

        restartCount++
        resetTrackingData(true)
        startTracking()
    }

    val uptimeInSeconds: Long
        get() {
            return if (isPaused) {
                lastTime
            } else {
                lastTime + (System.currentTimeMillis() - startTime) / 1000
            }
        }

    val uptime: String
        get() {
            val uptime: Long = if (isPaused) {
                lastTime
            } else {
                lastTime + (System.currentTimeMillis() - startTime) / 1000
            }

            return StringUtils.formatTime(uptime)
        }
}
