package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.colors.FetchColors
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.FetchVersions
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors

object ServerUtils {

    @JvmStatic
    var serverStatus = false

    private const val NORMAL_CHECK_INTERVAL = 6000  // 5 minutes
    private const val COOLDOWN_CHECK_INTERVAL = 12000  // 10 minutes

    private var currentCheckInterval = NORMAL_CHECK_INTERVAL
    private var consecutiveFailures = 0

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % currentCheckInterval == 0L) {
            executorService.submit { checkServerStatusPeriodically(client) }
        }
    }

    private fun checkServerStatusPeriodically(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        logger.info("[SCT]: Checking server status...")
        ServerStatus.checkServerWithCallback(client::execute) { up ->
            serverStatus = up

            if (serverStatus) {
                logger.info("[SCT]: Server is alive.")
                consecutiveFailures = 0
                currentCheckInterval = NORMAL_CHECK_INTERVAL

                if (TokenManager.token == null) {
                    TokenManager.fetchAndStoreToken()
                }
                checkIfDataWasFetched()
            } else {
                logger.warn("[SCT]: Server is not alive.")
                consecutiveFailures++
                if (consecutiveFailures >= 3) {
                    currentCheckInterval = COOLDOWN_CHECK_INTERVAL
                }

                // Stop all api-related tracking
                SkillTrackingHandler.stopTracking()
                ColeweightTrackingHandler.stopTracking()
            }
        }
    }

    private fun checkIfDataWasFetched() {
        if (hasData()) return

        Hypixel.fetchData()
        logger.info("[SCT]: Attempting to load missing data")
    }

    @Synchronized
    private fun hasData(): Boolean {
        return FetchColors.hasColors &&
                FetchNpcPrices.hasNpcPrice &&
                FetchCollectionList.hasCollectionList &&
                FetchGemstoneList.hasGemstoneList &&
                ColeweightFetcher.hasColeweightTopColors &&
                ColeweightFetcher.hasColeweightLb &&
                EliteApiFetcher.hasFarmingweightTopColors &&
                EliteApiFetcher.hasFarmingweightLb &&
                FetchVersions.hasVersions
    }
}