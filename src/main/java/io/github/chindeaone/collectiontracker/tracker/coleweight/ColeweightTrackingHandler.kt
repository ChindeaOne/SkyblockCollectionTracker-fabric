package io.github.chindeaone.collectiontracker.tracker.coleweight

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.gui.overlays.ColeweightOverlay
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightDataFetcher.scheduler
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.afk
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ColeweightTrackingHandler {

    private val logger: Logger = LogManager.getLogger(ColeweightTrackingHandler::class.java)

    @JvmField
    var isTracking = false

    @JvmField
    var isPaused = false

    private const val RESETS: Int = 10
    private var restartCount: Int = 0
    private var firstRestartTime: Long = 0

    private var startTime: Long = 0
    private var lastTime: Long = 0
    private var lastTrackedTime: Long = 0
    private val COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(10)

    @JvmStatic
    fun startTracking() {
        val now = System.currentTimeMillis()

        if (now - lastTrackedTime < COOLDOWN_MILLIS) {
            sendMessage("§cPlease wait a few seconds before tracking Coleweight again!", true)
            return
        }

        if (scheduler.isShutdown) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }

        initTracking(now)
        sendMessage("§aStarted tracking Coleweight.", true)

        OverlayManager.setColeweightOverlayRendering(true)
        ColeweightDataFetcher.scheduleFetch()
    }

    private fun initTracking(now: Long) {
        startTime = now
        lastTrackedTime = now
        lastTime = 0
        isTracking = true
        isPaused = false
    }

    @JvmStatic
    fun stopTrackingManual() {
        if (!scheduler.isShutdown) {
            sendMessage("§cStopping Coleweight tracking!", true)

            resetTrackingData()

            logger.info("[SCT]: Coleweight tracking stopped.")
        } else {
            sendMessage("§cNo Coleweight tracking active!", true)
            logger.warn("[SCT]: Attempted to stop Coleweight tracking manually, but no active tracking session was found.")
        }
    }

    @JvmStatic
    fun stopTracking() {
        if (!isTracking) return
        if (!scheduler.isShutdown) {
            if (!server) {
                logger.info("[SCT]: Coleweight tracking stopped because player disconnected from the server.")
            } else if (afk) {
                sendMessage("§cYou have been marked as AFK. Stopping Coleweight tracker.", true)
                logger.info("[SCT]: Coleweight tracking stopped because the player was marked as AFK or the API server is down.")
            } else {
                sendMessage("§cAPI server is down. Stopping Coleweight tracker.", true)
                logger.info("[SCT]: Coleweight tracking stopped because the API server is down.")
            }
            afk = false

            resetTrackingData(false)
        } else {
            logger.warn("[SCT]: Attempted to stop Coleweight tracking, but no active tracking session was found.")
        }
    }

    @JvmStatic
    fun restartTracking() {
        if (!isTracking) {
            sendMessage("§cNo active Coleweight tracking session to restart!", true)
            logger.warn("[SCT]: Attempted to restart Coleweight tracking, but no active tracking session was found.")
            return
        }

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
            sendMessage("§cHourly restart limit reached! Cannot restart Coleweight tracking.", true)
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

        val now = System.currentTimeMillis()
        lastTrackedTime = if (!restart) {
            now
        } else now - COOLDOWN_MILLIS

        OverlayManager.setColeweightOverlayRendering(false)
        ColeweightOverlay.trackingDirty = false
    }

    private fun resetVariables() {
        startTime = 0
        lastTrackedTime = 0
        isTracking = false
        isPaused = false
        ColeweightTrackingRates.reset()
    }

    @JvmStatic
    fun pauseTracking() {
        if (!scheduler.isShutdown) {
            if (isPaused) {
                sendMessage("§cColeweight tracking is already paused!", true)
                logger.warn("[SCT]: Attempted to pause Coleweight tracking, but tracking is already paused.")
                return
            }
            isPaused = true
            lastTime += (System.currentTimeMillis() - startTime) / 1000
            sendMessage("§7Coleweight tracking paused.", true)
            logger.info("[SCT]: Coleweight tracking paused.")
        } else {
            sendMessage("§cNo Coleweight tracking active!", true)
            logger.warn("[SCT]: Attempted to pause Coleweight tracking, but no active tracking session was found.")
        }
    }

    @JvmStatic
    fun resumeTracking() {
        if (scheduler.isShutdown && !isTracking) {
            sendMessage("§cNo Coleweight tracking active!", true)
            logger.warn("[SCT]: Attempted to resume Coleweight tracking, but no active tracking session was found.")
            return
        }

        if (isTracking && isPaused) {
            sendMessage("§7Resuming Coleweight tracking.", true)
            logger.info("[SCT]: Resuming Coleweight tracking.")
            startTime = System.currentTimeMillis()
            isPaused = false
        } else if (isTracking) {
            sendMessage("§cColeweight tracking is already active!", true)
            logger.warn("[SCT]: Attempted to resume Coleweight tracking, but tracking is already active and not paused.")
        } else {
            sendMessage("§cTracking has not been started yet!", true)
            logger.warn("[SCT]: Attempted to resume Coleweight tracking, but tracking has not been started yet.")
        }
    }

    fun getUptimeInSeconds(): Long {
        if (startTime == 0L) {
            return 0
        }

        return if (isPaused) {
            lastTime
        } else {
            lastTime + (System.currentTimeMillis() - startTime) / 1000
        }
    }

    @JvmStatic
    fun getUptime(): String {
        if (startTime == 0L) return "00:00:00"

        val uptime: Long = if (isPaused) {
            lastTime
        } else {
            lastTime + (System.currentTimeMillis() - startTime) / 1000
        }

        val hours = uptime / 3600
        val minutes = (uptime % 3600) / 60
        val seconds = uptime % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}