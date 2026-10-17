package io.github.chindeaone.collectiontracker.tracker.skills

import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName
import io.github.chindeaone.collectiontracker.config.enableTamingTracking
import io.github.chindeaone.collectiontracker.config.skillLeaderboard
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setSkillOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.saveSkillMilestoneProgress
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.clearCache
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.scheduleSkillFetch
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.scheduler
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object SkillTrackingHandler {
    private val logger: Logger = LogManager.getLogger(SkillTrackingHandler::class.java)

    @Volatile
    var isTracking: Boolean = false
    var isPaused: Boolean = false
    var leaderboardTrackingInitialized: Boolean = false

    var startTime: EpochMark? = null
    private var pauseDuration: Duration = Duration.ZERO
    private var lastTrackedTime: EpochMark? = null

    private const val RESETS = 10
    private var restartCount = 0
    private var firstRestartTime: EpochMark? = null

    var isSkillMaxed: Boolean = false

    fun startTracking() {

        if (lastTrackedTime != null && lastTrackedTime!!.timePassedSince() < 10.seconds) {
            sendMessage("§cPlease wait a few seconds before tracking another skill!")
            return
        } else {
            sendMessage("§aTracking $skillName skill.")
        }

        if (scheduler == null || scheduler!!.isShutdown) scheduler = Executors.newSingleThreadScheduledExecutor()

        initTracking()
        setSkillOverlayRendering(true)

        isSkillMaxed = SkillUtils.isSkillMaxed(skillName) == true
        val skillLevel = SkillUtils.getSkillLevel(skillName)
        val skillXp = SkillUtils.getSkillValue(skillName)

        SkillTrackingRates.initTracking(skillLevel ?: 0, skillXp?.toLong() ?: 0L)

        SkillTrackingRates.updateSkillLeaderboardStats()
        if (enableTamingTracking) SkillTrackingRates.updateTamingLeaderboardStats()

        if (!isSkillMaxed || enableTamingTracking) {
            // Track only via API
            scheduleSkillFetch(isSkillMaxed, skillXp?.toLong() ?: 0L, skillName)
        }
        logger.info("[SCT]: Started tracking $skillName")
    }

    fun onSkillGain(value: Long, skillName: String?) {
        if (!isTracking || (SkillTracker.skillName != skillName) || !isSkillMaxed) return
        SkillTrackingRates.calculateSkillRates(value)
    }

    private fun initTracking() {
        lastTrackedTime = EpochMark.now()

        isTracking = true
        isPaused = false
        leaderboardTrackingInitialized = skillLeaderboard

        startTime = EpochMark.now()
        pauseDuration = Duration.ZERO
    }

    fun pauseTracking() {
        if (checkTracking()) return
        if (isPaused) {
            sendMessage("§cSkill tracking is already paused.")
            logger.warn("[SCT]: Skills tracking is already paused.")
            return
        }

        isPaused = true
        startTime?.let { pauseDuration += it.timePassedSince() }

        sendMessage("§7Paused tracking $skillName skill.")
        logger.info("[SCT]: Pausing tracking $skillName")
    }

    fun resumeTracking() {
        if (checkTracking()) return
        if (!isPaused) {
            sendMessage("§cSkill tracking is not paused.")
            logger.warn("[SCT]: Skills tracking is not paused.")
            return
        }

        isPaused = false
        startTime = EpochMark.now()

        sendMessage("§7Resumed tracking $skillName skill.")
        logger.info("[SCT]: Resuming tracking $skillName")
    }

    fun stopTracking() {
        if (!isTracking) return

        if (!server) {
            logger.info("[SCT]: Tracking stopped because player disconnected from the server.")
        } else {
            sendMessage("§cAPI server is down. Stopping the skill tracker.")
            logger.info("[SCT]: Skill tracking stopped because the API server is down.")
        }

        resetTrackingData(false)
    }

    fun stopTrackingManual() {
        if (checkTracking()) return

        resetTrackingData(false)
        sendMessage("§cStopped tracking $skillName skill!")
        logger.info("[SCT]: Stopped tracking $skillName")
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

        startTime = null
        pauseDuration = Duration.ZERO

        val now = EpochMark.now()
        lastTrackedTime = if (!restart) {
            now
        } else now - 10.seconds

        isSkillMaxed = false
        setSkillOverlayRendering(false)

        saveSkillMilestoneProgress()

        clearCache()
        SkillTrackingRates.resetSession()
    }

    private fun checkTracking(): Boolean {
        if (!isTracking) {
            sendMessage("§cNo skill is being tracked currently!")
            logger.warn("[SCT]: No skill is being tracked currently.")
            return true
        }

        return false
    }

    fun restartTracking() {
        if (checkTracking()) return

        val firstRestart = firstRestartTime
        if (restartCount == 0 || firstRestart == null || firstRestart.timePassedSince() >= 1.hours) {
            firstRestartTime = EpochMark.now()
            restartCount = 0
        }

        if (restartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached. Please wait before restarting again.")
            logger.warn("[SCT]: Hourly restart limit reached for skill tracking.")
            return
        }

        restartCount++
        resetTrackingData(true)
        startTracking()
    }

    private val totalUptime: Duration
        get() = if (isPaused || startTime == null) {
            pauseDuration
        } else {
            pauseDuration + startTime!!.timePassedSince()
        }

    val uptimeInSeconds: Long
        get() = totalUptime.inWholeSeconds

    val uptime: String
        get() = totalUptime.formatClock()

}
