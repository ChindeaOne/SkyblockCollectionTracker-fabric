package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.colors.FetchColors
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.FetchVersions
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.concurrent.fixedRateTimer

object ServerUtils {

    @JvmStatic
    var serverStatus = false

    private const val CHECK_INTERVAL = 600_000L

    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    fun startCheckingServer () {
        fixedRateTimer(name = "sct-server-status", initialDelay = CHECK_INTERVAL, period = CHECK_INTERVAL) {
            checkServerStatusPeriodically()
        }
    }

    private fun checkServerStatusPeriodically() {
        if (!HypixelUtils.isInSkyblock) return
        logger.info("[SCT]: Checking server status...")

        ApiManager.checkServer()
            .thenAccept { up ->
                serverStatus = up

                if (up) {
                    logger.info("[SCT]: Server is alive.")

                    if (TokenManager.token == null) {
                        TokenManager.fetchAndStoreToken()
                    }

                    checkIfDataWasFetched()
                } else {
                    logger.warn("[SCT]: Server is not alive.")

                    // Stop all api-related tracking
                    SkillTrackingHandler.stopTracking()
                    ColeweightTrackingHandler.stopTracking()
                }
            }
    }

    private fun checkIfDataWasFetched() {
        if (!HypixelUtils.isInSkyblock) return
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