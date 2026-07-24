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
import io.github.chindeaone.collectiontracker.api.serverapi.RepoUtils
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.skilltreeapi.FetchSkillTree
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
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object Hypixel {

    private val scoreboardTitlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?(?: [♲☀Ⓑ])?")

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

    private fun checkServer() {
        val mc = Minecraft.getInstance()
        var hypixel = false

        val clientBrand = mc.connection?.serverBrand()
        clientBrand?.let {
            if (it.contains("hypixel", ignoreCase = true)) {
                hypixel = true
            }
        }
        server = hypixel
    }

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isInHypixel) {
            checkServer()
        }

        val isNowOnSkyblock = checkScoreboard(client) ?: return
        val wasOnSkyblock = skyblock

        if (wasOnSkyblock == isNowOnSkyblock) {
            return
        }
        skyblock = isNowOnSkyblock

        if (isNowOnSkyblock && HypixelUtils.isInHypixel) {
            onSkyblockJoin(client) // initialize data fetching and update checking
        } else {
            onSkyblockLeave() // pause tracking when leaving Skyblock
        }
    }

    private fun onSkyblockJoin(client: Minecraft) {
        ServerStatus.checkServerWithCallback(client::execute) { up ->
            serverStatus = up

            if (!up) {
                ChatUtils.sendMessage("§cThe API server is currently under maintenance. Tracking will be unavailable until the server is back online. Apologies for the inconvenience.")
                logger.warn("[SCT]: The API server is currently under maintenance.")
                return@checkServerWithCallback
            }

            loadData(client)
            checkForUpdates()
        }
    }

    private fun loadData(client: Minecraft) {
        if (TokenManager.token != null) {
            fetchData()
            return
        }

        CompletableFuture.runAsync {
            ApiManager.authenticateMojang()
        }.thenRun {
            client.execute {
                if (TokenManager.token == null) {
                    ChatUtils.sendMessage("§cCouldn't request an API token. Some tracking features might be unavailable. Use §e/sct token§c to retry manually.")
                } else {
                    fetchData()
                }
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

        CompletableFuture.runAsync {
            RepoUtils.checkGithubReleases()
            RepoUtils.checkLatestVersion()
        }.thenAccept {
            Minecraft.getInstance().execute {
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
    }

    fun fetchData() {
        if (!FetchCollectionList.hasCollectionList) CompletableFuture.runAsync { FetchCollectionList.fetchCollectionList() }
        if (!FetchNpcPrices.hasNpcPrice) CompletableFuture.runAsync { FetchNpcPrices.fetchPrices() }
        if (!FetchGemstoneList.hasGemstoneList) CompletableFuture.runAsync { FetchGemstoneList.fetchGemstoneList() }
        if (!FetchColors.hasColors) CompletableFuture.runAsync { FetchColors.fetchColorsData() }
        if (!FetchWaypoints.hasWaypoints) CompletableFuture.runAsync { FetchWaypoints.fetchWaypoints() }
        if (!ColeweightFetcher.hasColeweightLb) CompletableFuture.runAsync { ColeweightFetcher.fetchColeweightLbTop1k() }
        if (!ColeweightFetcher.hasColeweightTopColors) CompletableFuture.runAsync { ColeweightFetcher.fetchColeweightTopColors() }
        if (!EliteApiFetcher.hasFarmingweightLb) CompletableFuture.runAsync { EliteApiFetcher.fetchFarmingweightLbTop1k() }
        if (!EliteApiFetcher.hasFarmingweightTopColors) CompletableFuture.runAsync { EliteApiFetcher.fetchFarmingweightTopColors() }
        if (!FetchSkillTree.hasSkillTree) CompletableFuture.runAsync { FetchSkillTree.fetchSkillTree(
            mining = true,
            foraging = true
        ) }
    }

    private fun checkScoreboard(client: Minecraft): Boolean? {
        val displayName = ScoreboardUtils.getScoreboardTitle(client) ?: return null
        val scoreboardTitle = displayName.removeColor()
        return scoreboardTitlePattern.matches(scoreboardTitle)
    }

    private fun onSkyblockLeave() {
        if (TrackingHandler.isTracking) TrackingHandler.pauseTracking()
        if (MultiTrackingHandler.isMultiTracking) MultiTrackingHandler.pauseMultiTracking()
        if (SkillTrackingHandler.isTracking) SkillTrackingHandler.pauseTracking()
        if (ColeweightTrackingHandler.isTracking) ColeweightTrackingHandler.pauseTracking()
    }
}