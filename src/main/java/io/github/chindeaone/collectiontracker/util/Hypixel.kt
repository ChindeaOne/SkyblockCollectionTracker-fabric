package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.autoupdate.UpdaterManager
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass
import io.github.chindeaone.collectiontracker.util.ServerUtils.serverStatus
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
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
        TrackingHandlerClass.stopTracking()
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
                loadPlayerData()
                if (playerLoaded) {
                    serverStatus = ServerStatus.checkServer()

                    if (!serverStatus) {
                        ChatUtils.sendMessage("§cThe API server is currently under maintenance. Tracking will be unavailable until the server is back online. We apologize for the inconvenience.")
                        logger.warn("[SCT]: The API server is currently under maintenance.")
                    } else {
                        if (TokenManager.getToken() == null) {
                            TokenManager.fetchAndStoreToken()
                        }
                        CompletableFuture.runAsync { fetchData() }
                        logger.info("[SCT]: API data loaded successfully.")
                    }

                    logger.info("[SCT]: Update stream status: {}", SkyblockCollectionTracker.configManager.config!!.about.update)

                    if (!SkyblockCollectionTracker.configManager.config!!.about.update.equals(About.UpdateType.NONE)) {
                        CompletableFuture.runAsync {
                            RepoUtils.checkForUpdates(SkyblockCollectionTracker.configManager.config!!.about.update.toString())
                        }.thenAcceptAsync  {
                            if (RepoUtils.latestVersion != null) {

                                ChatUtils.sendMessage(
                                    ("§eA new version for SkyblockCollectionTracker found: §a${RepoUtils.latestVersion}§e. It will be downloaded after closing the game.")
                                )
                                logger.info("[SCT]: New version found: ${RepoUtils.latestVersion}")

                                UpdaterManager.update()
                                SkyblockCollectionTracker.configManager.config!!.about.hasCheckedUpdate = false

                            } else {
                                if(!SkyblockCollectionTracker.configManager.config!!.about.hasCheckedUpdate) {
                                    ChatUtils.sendMessage("§aThe mod has been updated successfully.")
                                    SkyblockCollectionTracker.configManager.config!!.about.hasCheckedUpdate = true
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

        val inSkyblock = checkScoreboard()
        if (inSkyblock == skyblock) return
        skyblock = inSkyblock
    }

    private fun fetchData() {
        // Request collection data
        if (!FetchCollectionList.hasCollectionList) {
            CompletableFuture.runAsync {
                FetchCollectionList.fetchCollectionList()
                FetchCollectionList.hasCollectionList = true
            }
        }
        // Request NPC prices
        if (!FetchNpcPrices.hasNpcPrice) {
            CompletableFuture.runAsync {
                FetchNpcPrices.fetchPrices()
                FetchNpcPrices.hasNpcPrice = true
            }
        }
        // Request gemstone list
        if(!FetchGemstoneList.hasGemstoneList) {
            CompletableFuture.runAsync {
                FetchGemstoneList.fetchGemstoneList()
                FetchGemstoneList.hasGemstoneList = true
            }
        }
    }

    private fun loadPlayerData() {
        val client = Minecraft.getInstance()
        if (client.player != null) {
            playerLoaded = true
            PlayerData.playerUUID
            PlayerData.playerName
        }
    }

    private fun checkScoreboard(): Boolean {
        val displayName = getScoreboardTitle() ?: return false
        val scoreboardTitle = displayName.removeColor()
        return scoreboardTitlePattern.matches(scoreboardTitle)
    }

    private fun getScoreboardTitle(): String? {
        val world =Minecraft.getInstance().level ?: return null
        val objective = world.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
        val displayName = objective.displayName?.string ?: return null
        return displayName
    }
}