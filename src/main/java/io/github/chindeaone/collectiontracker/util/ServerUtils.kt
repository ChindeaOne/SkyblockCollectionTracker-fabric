package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors

object ServerUtils {

    var serverStatus = false

    private const val NORMAL_CHECK_INTERVAL = 6000  // 5 minutes
    private const val COOLDOWN_CHECK_INTERVAL = 12000  // 10 minutes

    private var tickCounter = 0
    private var currentCheckInterval = NORMAL_CHECK_INTERVAL
    private var consecutiveFailures = 0

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    fun onTick(client: Minecraft) {
        tickCounter++
        if (tickCounter >= currentCheckInterval) {
            tickCounter = 0
            executorService.submit { checkServerStatusPeriodically(client) }
        }
    }

    private fun checkServerStatusPeriodically(client: Minecraft) {
        logger.info("[SCT]: Checking server status...")
        ServerStatus.checkServerAsync(client::execute) { up ->
            serverStatus = up

            if (serverStatus) {
                logger.info("[SCT]: Server is alive.")
                consecutiveFailures = 0
                currentCheckInterval = NORMAL_CHECK_INTERVAL

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

                // Stop all tracking if server is down
                TrackingHandler.stopTracking()
                SkillTrackingHandler.stopTracking()
            }
        }
    }

    private fun checkIfDataWasFetched() {
        if (!ServerStatus.hasData()) {
            Hypixel.fetchData()
            logger.info("[SCT]: API data loaded successfully.")
        }
    }
}