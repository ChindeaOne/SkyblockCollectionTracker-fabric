/*
* This kotlin object is derived from the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.colors.FetchColors
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.FetchVersions
import io.github.chindeaone.collectiontracker.api.serverapi.FetchRepoData
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.api.waypointsapi.FetchWaypoints
import io.github.chindeaone.collectiontracker.updater.UpdaterManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.updater.ModrinthData
import io.github.chindeaone.collectiontracker.utils.ServerUtils.serverStatus
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Hypixel {

    @JvmStatic
    var server = false
    var skyblock = false
    private var updateCheckPerformed = false

    private var logger: Logger = LogManager.getLogger(Hypixel::class.java)

    fun onDisconnect() {
        logger.info("[SCT]: Player has disconnected from the server.")

        server = false
        skyblock = false
        serverStatus = false

        if (TrackingHandler.isTracking) TrackingHandler.pauseTracking()
        if (MultiTrackingHandler.isMultiTracking) MultiTrackingHandler.pauseMultiTracking()
        if (SkillTrackingHandler.isTracking) SkillTrackingHandler.pauseTracking()
        if (ColeweightTrackingHandler.isTracking) ColeweightTrackingHandler.pauseTracking()
    }

    private fun checkMinecraftServer(client: Minecraft) {
        var hypixel = false

        client.connection?.serverBrand()?.let {
            if (it.contains("hypixel", ignoreCase = true)) {
                hypixel = true
            }
        }

        server = hypixel
    }

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isInHypixel) {
            checkMinecraftServer(client)
        }

        val isNowOnSkyblock = ScoreboardUtils.checkScoreboard(client) ?: return
        val wasOnSkyblock = skyblock

        if (wasOnSkyblock == isNowOnSkyblock) {
            return
        }
        skyblock = isNowOnSkyblock

        if (isNowOnSkyblock && HypixelUtils.isInHypixel) {
            onSkyblockJoin() // initialize data fetching and update checking
        } else {
            onSkyblockLeave() // pause tracking when leaving Skyblock
        }
    }

    private fun onSkyblockJoin() {
        ApiManager.checkServer()
            .thenAccept { up ->
                serverStatus = up

                if (!up) {
                    ChatUtils.sendMessage("§cThe API server is currently under maintenance. Tracking will be unavailable until the server is back online. Apologies for the inconvenience.")
                    logger.warn("[SCT]: The API server is currently under maintenance.")
                    return@thenAccept
                }

                loadData()
                checkForUpdates()
            }
    }

    private fun loadData() {
        if (TokenManager.token != null) {
            fetchData()
            return
        }

        ApiManager.authenticateMojang()
            .thenAccept { fetchedToken ->
                if (fetchedToken == null) {
                    ChatUtils.sendMessage("§cCouldn't request an API token. Some tracking features might be unavailable. Use §e/sct token§c to retry manually.")
                } else {
                    fetchData()
                }
            }
    }

    private fun checkForUpdates() {
        if (updateCheckPerformed) return
        updateCheckPerformed = true

        logger.info("[SCT]: Update stream status: {}", ConfigAccess.getUpdateStream())

        if (ConfigAccess.getUpdateStream() == About.UpdateStream.NONE) {
            logger.info("[SCT]: Update stream is disabled.")
            return
        }

        FetchRepoData.checkGithubReleases()
            .thenRun {
                RepoUtils.checkLatestVersion()
            }
            .thenAccept {
                if (!VersionUtils.checkIfVersionIsSupported()) {
                    ChatUtils.sendMessage("§cSkyblockCollectionTracker is no longer supported on this Minecraft version. Please update to a newer version of Minecraft!")
                    return@thenAccept
                }

                if (RepoUtils.latestVersion != null) {
                    when (ConfigAccess.getUpdateType()) {
                        About.UpdateType.AUTOMATIC -> {
                            ChatUtils.sendMessage("§eA new version for SkyblockCollectionTracker found: §a${RepoUtils.latestVersion}§e. It will be downloaded after closing the game.")
                            UpdaterManager.update()
                        }
                        About.UpdateType.MANUAL -> {
                            val url = ModrinthData.getModLink()
                            ChatUtils.sendMessage("§eA new version for SkyblockCollectionTracker found: §a${RepoUtils.latestVersion}§e.", true)
                            ChatUtils.sendClickableLinkComponent("§eClick here to manually download it.", "§eOpens the Modrinth page in your browser.", url)
                        }
                    }

                    logger.info("[SCT]: New version found: ${RepoUtils.latestVersion}")
                    ConfigHelper.disableUpdateChecks()
                } else {
                    if (!ConfigAccess.hasCheckedUpdate()) {
                        ChatUtils.sendMessage("§aThe mod has been updated successfully.")
                        ChatUtils.sendCommandComponent("§eSee what changed here.", "/sct changelog")
                        ConfigHelper.enableUpdateChecks()
                        logger.info("[SCT]: The mod has been updated successfully.")
                    }
                    logger.info("[SCT]: No new version found.")
                }
            }
    }

    fun fetchData() {
        if (!FetchCollectionList.hasCollectionList) FetchCollectionList.fetchCollectionList()
        if (!FetchNpcPrices.hasNpcPrice) FetchNpcPrices.fetchPrices()
        if (!FetchGemstoneList.hasGemstoneList) FetchGemstoneList.fetchGemstoneList()
        if (!FetchColors.hasColors) FetchColors.fetchColorsData()
        if (!FetchWaypoints.hasWaypoints) FetchWaypoints.fetchWaypoints()
        if (!ColeweightFetcher.hasColeweightLb) ColeweightFetcher.fetchColeweightLbTop1k()
        if (!ColeweightFetcher.hasColeweightTopColors) ColeweightFetcher.fetchColeweightTopColors()
        if (!EliteApiFetcher.hasFarmingweightLb) EliteApiFetcher.fetchFarmingweightLbTop1k()
        if (!EliteApiFetcher.hasFarmingweightTopColors) EliteApiFetcher.fetchFarmingweightTopColors()
        if (!FetchVersions.hasVersions) FetchVersions.fetchVersions()
    }

    private fun onSkyblockLeave() {
        if (TrackingHandler.isTracking) TrackingHandler.pauseTracking()
        if (MultiTrackingHandler.isMultiTracking) MultiTrackingHandler.pauseMultiTracking()
        if (SkillTrackingHandler.isTracking) SkillTrackingHandler.pauseTracking()
        if (ColeweightTrackingHandler.isTracking) ColeweightTrackingHandler.pauseTracking()
    }
}