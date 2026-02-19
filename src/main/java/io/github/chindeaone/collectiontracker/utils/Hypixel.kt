/*
* This kotlin object is derived from the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.colors.FetchColors
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.RepoUtils
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.autoupdate.UpdaterManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.ServerUtils.serverStatus
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object Hypixel {

    private val scoreboardTitlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?(?: [♲☀Ⓑ])?")

    var server = false
    var skyblock = false
    private var playerLoaded = false

    private var logger: Logger = LogManager.getLogger(Hypixel::class.java)

    fun onDisconnect() {
        logger.info("[SCT]: Player has disconnected from the server.")
        server = false
        skyblock = false
        playerLoaded = false
        serverStatus = false
        TrackingHandler.stopTracking()
        SkillTrackingHandler.stopTracking()
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

    fun onTick(client: Minecraft) {
        if (!HypixelUtils.isInHypixel) {
            checkServer()
            if (HypixelUtils.isInHypixel && !playerLoaded) {
                loadPlayerData(client)
                if (playerLoaded) {
                    ServerStatus.checkServerAsync(client::execute) { up ->
                        serverStatus = up

                        if (!serverStatus) {
                            ChatUtils.sendMessage("§cThe API server is currently under maintenance. Tracking will be unavailable until the server is back online. Apologies for the inconvenience.")
                            logger.warn("[SCT]: The API server is currently under maintenance.")
                        } else {
                            if (TokenManager.getToken() == null) {
                                TokenManager.fetchAndStoreToken()
                            }
                            fetchData()
                            logger.info("[SCT]: Update stream status: {}", ConfigAccess.getUpdateType())

                            if (ConfigAccess.getUpdateType() != About.UpdateType.NONE) {
                                CompletableFuture.runAsync {
                                    RepoUtils.checkGithubReleases()
                                    RepoUtils.checkLatestVersion()
                                }.thenAcceptAsync  {
                                    if (RepoUtils.latestVersion != null) {
                                        Minecraft.getInstance().execute {
                                            ChatUtils.sendMessage(
                                                "§eA new version for SkyblockCollectionTracker found: §a${RepoUtils.latestVersion}§e. It will be downloaded after closing the game."
                                            )
                                        }
                                        logger.info("[SCT]: New version found: ${RepoUtils.latestVersion}")
                                        UpdaterManager.update()
                                        ConfigHelper.disableUpdateChecks()
                                    } else {
                                        if (!ConfigAccess.hasCheckedUpdate()) {
                                            Minecraft.getInstance().execute {
                                                ChatUtils.sendMessage("§aThe mod has been updated successfully.")
                                                ChatUtils.sendCommandComponent("§eSee what changed here.", "/sct changelog")
                                            }
                                            ConfigHelper.enableUpdateChecks()
                                            logger.info("[SCT]: The mod has been updated successfully.")
                                        }
                                        logger.info("[SCT]: No new version found.")
                                    }
                                }
                            } else{
                                logger.info("[SCT]: Update stream is disabled.")
                            }
                        }
                    }
                }
            }
        }

        val inSkyblock = checkScoreboard(client)
        if (inSkyblock == skyblock) return
        skyblock = inSkyblock
    }

    fun fetchData() {
        if (!ServerStatus.hasData()) {
            val futures = arrayOf(
                CompletableFuture.runAsync { FetchCollectionList.fetchCollectionList() },
                CompletableFuture.runAsync { FetchNpcPrices.fetchPrices() },
                CompletableFuture.runAsync { FetchGemstoneList.fetchGemstoneList() },
                CompletableFuture.runAsync { FetchColors.fetchColorsData() },
                CompletableFuture.runAsync { ColeweightFetcher.fetchColeweightLbTop1k() }
            )
            CompletableFuture.allOf(*futures).thenRun {
                logger.info("[SCT]: API data loaded successfully.")
            }
        }
    }

    private fun loadPlayerData(client: Minecraft) {
        if (client.player != null) {
            playerLoaded = true
            PlayerData.playerUUID
            PlayerData.playerName
        }
    }

    private fun checkScoreboard(client: Minecraft): Boolean {
        val displayName = ScoreboardUtils.getScoreboardTitle(client) ?: return false
        val scoreboardTitle = displayName.removeColor()
        return scoreboardTitlePattern.matches(scoreboardTitle)
    }
}