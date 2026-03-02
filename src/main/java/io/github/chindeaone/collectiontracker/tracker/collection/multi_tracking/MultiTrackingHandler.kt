package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearCache
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.scheduler
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object MultiTrackingHandler  {

    private val logger: Logger = LogManager.getLogger(MultiTrackingHandler::class.java)
    @JvmStatic
    val COOLDOWN_MILLIS: Long = TimeUnit.SECONDS.toMillis(10) // 10 seconds cooldown

    @Volatile
    @JvmStatic
    var isMultiTracking = false
    @JvmStatic
    var isMultiPaused = false

    @JvmStatic
    var multiStartTime: Long = 0
    @JvmStatic
    var multiLastTime: Long = 0
    @JvmStatic
    var multiLastTrackTime: Long = 0

    private const val RESETS: Int = 10
    private var multiRestartCount: Int = 0
    private var multiFirstRestartTime: Long = 0

    @JvmStatic
    fun startMultiTracking() {
        if (scheduler.isShutdown) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }
        initMultiTracking()
        OverlayManager.setMultiTrackingOverlayRendering(true)
        logger.info("[SCT]: Starting multi tracking for player ${PlayerData.playerName}")

        MultiDataFetcher.scheduleMultiCollectionDataFetch()
    }

    private fun initMultiTracking() {
        val now = System.currentTimeMillis()
        multiStartTime = now
        multiLastTrackTime = now
        multiLastTime = 0

        isMultiTracking = true
        isMultiPaused = false
    }

    @JvmStatic
    fun stopMultiTrackingManual() {
        if (!scheduler.isShutdown) {
            sendMessage("§cStopped multi-tracking!", true)

            resetMultiTrackingData(false)

            logger.info("[SCT]: Multi-tracking stopped.")
        } else {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to stop multi-tracking manually, but no multi-tracking is active.")
        }
    }

    @JvmStatic
    fun stopMultiTracking() {
        if (!isMultiTracking) return
        if (!scheduler.isShutdown) {
            if (!server) {
                logger.info("[SCT]: Multi-tracking stopped because player disconnected from the server.")
            } else if (MultiTrackingRates.afk) {
                sendMessage("§cYou have been marked as AFK. Stopping multi-tracker.", true)
                logger.info("[SCT]: Multi-tracking stopped because the player went AFK or the API server is down")
            } else {
                sendMessage("§cAPI server is down. Stopping multi-tracker.", true)
                logger.info("[SCT]: Multi-tracking stopped because the API server is down.")
            }
            MultiTrackingRates.afk = false

            resetMultiTrackingData(false)
        } else {
            logger.warn("[SCT]: Attempted to stop multi-tracking, but no multi-tracking is active.")
        }
    }

    @JvmStatic
    fun restartMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active to restart!", true)
            logger.warn("[SCT]: Attempted to restart multi-tracking, but no multi-tracking is active.")
            return
        }

        if (multiRestartCount == 0) {
            multiFirstRestartTime = System.currentTimeMillis()
        } else {
            val elapsedTime = System.currentTimeMillis() - multiFirstRestartTime
            if (elapsedTime >= TimeUnit.HOURS.toMillis(1)) {
                multiRestartCount = 0
                multiFirstRestartTime = System.currentTimeMillis()
            }
        }

        if (multiRestartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart multi-tracking.", true)
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart multi-tracking.")
            return
        }

        multiRestartCount++
        resetMultiTrackingData(true)
        startMultiTracking()
    }

    private fun resetMultiTrackingData(restart: Boolean) {
        resetVariables()
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (_: InterruptedException) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
        // Clear cached data
        clearCache()

        // Reset uptime
        val now = System.currentTimeMillis()
        if (!restart) {
            multiLastTrackTime = now
            clearFetchedData()
        } else multiLastTrackTime = now - COOLDOWN_MILLIS

        OverlayManager.setMultiTrackingOverlayRendering(false)
        MultiCollectionOverlay.trackingDirty = false
    }

    private fun clearFetchedData() {
        CollectionsManager.resetMultiCollections()
        BazaarCollectionsManager.resetBazaarData()
    }

    private fun resetVariables() {
        isMultiTracking = false
        isMultiPaused = false
        multiStartTime = 0
        multiLastTime = 0
        MultiTrackingRates.afk = false

        clearMaps()
    }

    fun clearMaps() {
        MultiTrackingRates.unchangedStreaks.clear()
        MultiTrackingRates.collectionAmounts.clear()
        MultiTrackingRates.collectionPerHour.clear()
        MultiTrackingRates.collectionMade.clear()
        MultiTrackingRates.collectionSinceLast.clear()
        MultiTrackingRates.sessionStartCollections.clear()
        MultiTrackingRates.lastCollectionTimes.clear()
        MultiTrackingRates.lastApiCollections.clear()
        MultiTrackingRates.moneyPerHourNPC.clear()
        MultiTrackingRates.moneyMadeNPC.clear()
        MultiTrackingRates.moneyMadeBazaar.clear()
        MultiTrackingRates.moneyPerHourBazaar.clear()
        MultiTrackingRates.seenGemstones.clear()
    }

    @JvmStatic
    fun pauseMultiTracking() {
        if (!scheduler.isShutdown) {
            if (isMultiPaused) {
                sendMessage("§cMulti-tracking is already paused!", true)
                logger.warn("[SCT]: Attempted to pause multi-tracking, but multi-tracking is already paused.")
                return
            }
            isMultiPaused = true
            multiLastTime += (System.currentTimeMillis() - multiStartTime) / 1000
            sendMessage("§7Multi-tracking paused.", true)
            logger.info("[SCT]: Multi-tracking paused.")
        } else {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to pause multi-tracking, but no multi-tracking is active.")
        }
    }

    @JvmStatic
    fun resumeMultiTracking() {
        if (scheduler.isShutdown && !isMultiTracking) {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to resume multi-tracking, but no multi-tracking is active.")
            return
        }

        if (isMultiTracking && isMultiPaused) {
            sendMessage("§7Resuming multi-tracking.", true)
            logger.info("[SCT]: Resuming multi-tracking.")
            multiStartTime = System.currentTimeMillis()
            isMultiPaused = false
        } else if (isMultiTracking) {
            sendMessage("§cMulti-tracking is already active!", true)
            logger.warn("[SCT]: Attempted to resume multi-tracking, but multi-tracking is already active.")
        } else {
            sendMessage("§cTracking has not been started yet!", true)
            logger.warn("[SCT]: Attempted to resume multi-tracking, but multi-tracking has not been started.")
        }
    }

    fun getMultiUptimeInSeconds(): Long {
        if (multiStartTime == 0L) {
            return 0
        }

        return if (isMultiPaused) {
            multiLastTime
        } else {
            multiLastTime + (System.currentTimeMillis() - multiStartTime) / 1000
        }
    }

    @JvmStatic
    fun getMultiUptime(): String {
        if (multiStartTime == 0L) return "00:00:00"

        val uptime: Long = if (isMultiPaused) {
            multiLastTime
        } else {
            multiLastTime + (System.currentTimeMillis() - multiStartTime) / 1000
        }

        val hours = uptime / 3600
        val minutes = (uptime % 3600) / 60
        val seconds = uptime % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}