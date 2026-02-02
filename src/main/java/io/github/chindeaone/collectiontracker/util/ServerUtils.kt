package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object ServerUtils {

    var serverStatus = false

    private const val NORMAL_CHECK_INTERVAL = 6000  // 5 minutes
    private const val COOLDOWN_CHECK_INTERVAL = 12000  // 10 minutes
    private const val TRACKING_CHECK_INTERVAL = 3_600_000  // 1 hour

    private var tickCounter = 0
    private var currentCheckInterval = NORMAL_CHECK_INTERVAL
    private var consecutiveFailures = 0

    @Volatile
    var permanentlyDisabled = false
    private var trackingTimeoutFuture: ScheduledFuture<*>? = null

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    fun onClientTick() {
        if (permanentlyDisabled) return

        tickCounter++
        if (tickCounter >= currentCheckInterval) {
            tickCounter = 0
            executorService.submit { checkServerStatusPeriodically() }
        }
    }

    fun clearState() {
        trackingTimeoutFuture?.cancel(false)
        trackingTimeoutFuture = null
        permanentlyDisabled = false
    }

    private fun checkServerStatusPeriodically() {
        logger.info("[SCT]: Checking server status...")
        serverStatus = ServerStatus.checkServer()

        if (serverStatus) {
            logger.info("[SCT]: Server is alive.")
            consecutiveFailures = 0
            currentCheckInterval = NORMAL_CHECK_INTERVAL

            trackingTimeoutFuture?.cancel(false)
            trackingTimeoutFuture = null
            permanentlyDisabled = false

            if (TokenManager.getToken() == null) {
                TokenManager.fetchAndStoreToken()
            }
            checkIfDataWasFetched()

        } else {
            logger.warn("[SCT]: Server is not alive.")
            consecutiveFailures++
            if (consecutiveFailures >= 3) {
                currentCheckInterval = COOLDOWN_CHECK_INTERVAL
            }

            if (!TrackingHandlerClass.isTracking) {
                if (trackingTimeoutFuture == null) {
                    trackingTimeoutFuture = executorService.schedule({
                        permanentlyDisabled = true
                        logger.error("[SCT]: Disabled server checks due to mod not being used.")
                    }, TRACKING_CHECK_INTERVAL.toLong(), TimeUnit.MILLISECONDS)
                }
            } else {
                trackingTimeoutFuture?.cancel(false)
                trackingTimeoutFuture = null
            }

            TrackingHandlerClass.stopTracking()
        }
    }

    private fun checkIfDataWasFetched() {
        if (!FetchGemstoneList.hasGemstoneList && !FetchNpcPrices.hasNpcPrice && !FetchCollectionList.hasCollectionList) {
            CompletableFuture.runAsync { Hypixel.fetchData() }
            logger.info("[SCT]: API data loaded successfully.")
        }
    }
}