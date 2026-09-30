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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object ServerUtils {

    var serverStatus = false

    private const val CHECK_INTERVAL = 600_000L
    private const val REDUCED_CHECK_INTERVAL = CHECK_INTERVAL / 2

    private val logger: Logger = LogManager.getLogger(ServerUtils::class.java)

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "sct-server-status").apply { isDaemon = true }
    }

    fun startCheckingServer () {
        scheduleNextCheck(CHECK_INTERVAL)
    }

    private fun scheduleNextCheck(delay: Long) {
        scheduler.schedule({ checkServerStatusPeriodically() }, delay, TimeUnit.MILLISECONDS)
    }

    private fun checkServerStatusPeriodically() {
        if (!HypixelUtils.isInSkyblock) {
            val nextDelay = if (serverStatus) CHECK_INTERVAL else REDUCED_CHECK_INTERVAL
            scheduleNextCheck(nextDelay)
            return
        }

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
            .exceptionally { error ->
                logger.error("[SCT]: Error occurred while checking server status.", error)
                serverStatus = false
                null
            }
            .whenComplete { _, _ ->
                val nextDelay = if (serverStatus) CHECK_INTERVAL else REDUCED_CHECK_INTERVAL
                scheduleNextCheck(nextDelay)
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