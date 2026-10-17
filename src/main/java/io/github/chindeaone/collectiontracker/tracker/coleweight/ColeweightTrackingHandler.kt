package io.github.chindeaone.collectiontracker.tracker.coleweight

import io.github.chindeaone.collectiontracker.gui.OverlayManager.setColeweightOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.ColeweightOverlay
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightDataFetcher.scheduler
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object ColeweightTrackingHandler {

    private val logger: Logger = LogManager.getLogger(ColeweightTrackingHandler::class.java)

    var isTracking = false
    var isPaused = false

    private const val RESETS: Int = 10
    private var restartCount: Int = 0
    private var firstRestartTime: EpochMark? = null

    private var startTime: EpochMark? = null
    private var pauseDuration: Duration = Duration.ZERO
    private var lastTrackedTime: EpochMark? = null

    private val COOLDOWN = 10.seconds

    fun startTracking() {
        if (lastTrackedTime != null && lastTrackedTime!!.timePassedSince() < COOLDOWN) {
            sendMessage("§cPlease wait a few seconds before tracking Coleweight again!")
            return
        }

        if (scheduler.isShutdown) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }

        initTracking()
        sendMessage("§aStarted tracking Coleweight.")

        setColeweightOverlayRendering(true)
        ColeweightDataFetcher.scheduleFetch()
    }

    private fun initTracking() {
        startTime = EpochMark.now()
        lastTrackedTime = EpochMark.now()
        pauseDuration = Duration.ZERO
        isTracking = true
        isPaused = false
    }

    fun stopTrackingManual() {
        if (scheduler.isShutdown) {
            sendMessage("§cNo Coleweight tracking active!")
            logger.warn("[SCT]: Attempted to stop Coleweight tracking manually, but no active tracking session was found.")
            return
        }

        sendMessage("§cStopping Coleweight tracking!")
        logger.info("[SCT]: Coleweight tracking stopped.")

        resetTrackingData()
    }

    fun stopTracking() {
        if (!isTracking) {
            sendMessage("§cNo Coleweight tracking active!")
            logger.warn("[SCT]: Attempted to stop Coleweight tracking, but no active tracking session was found.")
            return
        }

        if (!server) {
            logger.info("[SCT]: Coleweight tracking stopped because player disconnected from the server.")
        } else {
            sendMessage("§cAPI server is down. Stopping Coleweight tracker.")
            logger.info("[SCT]: Coleweight tracking stopped because the API server is down.")
        }

        resetTrackingData(false)
    }

    fun restartTracking() {
        if (!isTracking) {
            sendMessage("§cNo active Coleweight tracking session to restart!")
            logger.warn("[SCT]: Attempted to restart Coleweight tracking, but no active tracking session was found.")
            return
        }

        val firstRestart = firstRestartTime
        if (restartCount == 0 || firstRestart == null || firstRestart.timePassedSince() >= 1.hours) {
            firstRestartTime = EpochMark.now()
            restartCount = 0
        }

        if (restartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart Coleweight tracking.")
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart Coleweight tracking.")
            return
        }

        restartCount ++
        resetTrackingData(true)
        startTracking()
    }

    private fun resetTrackingData(restart: Boolean = false) {
        resetVariables()

        scheduler.shutdownNow()
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (_: InterruptedException) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }

        ColeweightDataFetcher.clearCache()

        val now = EpochMark.now()
        lastTrackedTime = if (!restart) {
            now
        } else now - 10.seconds

        setColeweightOverlayRendering(false)
        ColeweightOverlay.trackingDirty = false
    }

    private fun resetVariables() {
        startTime = null
        pauseDuration = Duration.ZERO
        lastTrackedTime = null
        isTracking = false
        isPaused = false
        ColeweightTrackingRates.reset()
    }

    fun pauseTracking() {
        if (!isTracking) {
            sendMessage("§cNo Coleweight tracking active!")
            logger.warn("[SCT]: Attempted to pause Coleweight tracking, but no active tracking session was found.")
            return
        }

        if (isPaused) {
            sendMessage("§cColeweight tracking is already paused!")
            logger.warn("[SCT]: Attempted to pause Coleweight tracking, but tracking is already paused.")
            return
        }

        isPaused = true
        startTime?.let { pauseDuration += it.timePassedSince() }

        sendMessage("§7Coleweight tracking paused.")
        logger.info("[SCT]: Coleweight tracking paused.")
    }

    fun resumeTracking() {
        if (!isTracking) {
            sendMessage("§cNo Coleweight tracking active!")
            logger.warn("[SCT]: Attempted to resume Coleweight tracking, but no active tracking session was found.")
            return
        }

        if (!isPaused) {
            sendMessage("§cColeweight tracking is not paused!")
            logger.warn("[SCT]: Attempted to resume Coleweight tracking, but tracking is not paused.")
            return
        }

        startTime = EpochMark.now()
        isPaused = false

        sendMessage("§7Resuming Coleweight tracking.")
        logger.info("[SCT]: Resuming Coleweight tracking.")
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