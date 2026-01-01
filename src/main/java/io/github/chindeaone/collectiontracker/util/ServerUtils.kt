package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

object ServerUtils {

    var serverStatus = false

    private const val NORMAL_CHECK_INTERVAL = 6000  // 5 minutes
    private const val COOLDOWN_CHECK_INTERVAL = 12000  // 10 minutes
    private const val TRACKING_CHECK_INTERVAL = 18000  // 15 minutes

    private var tickCounter = 0
    private var currentCheckInterval = NORMAL_CHECK_INTERVAL
    private var consecutiveFailures = 0

    @Volatile
    private var permanentlyDisabled = false
    private var trackingTimeoutFuture: ScheduledFuture<*>? = null

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    fun onClientTick(client: Minecraft) {
        if (permanentlyDisabled) return

        tickCounter++
        if (tickCounter >= currentCheckInterval) {
            tickCounter = 0
            executorService.submit { checkServerStatusPeriodically() }
        }
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
                    }, TRACKING_CHECK_INTERVAL.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                }
            } else {
                trackingTimeoutFuture?.cancel(false)
                trackingTimeoutFuture = null
            }

            TrackingHandlerClass.stopTracking()
        }
    }
}