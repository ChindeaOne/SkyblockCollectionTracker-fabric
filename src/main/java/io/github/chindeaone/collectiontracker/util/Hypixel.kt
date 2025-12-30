package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.serverapi.ServerStatus
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.autoupdate.UpdaterManager
import io.github.chindeaone.collectiontracker.util.ServerUtils.serverStatus
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object Hypixel {

    private val scoreboardTitlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?")

    private val formattingChars = "kmolnrKMOLNR".toSet()
    private val colorChars = "abcdefABCDEF0123456789".toSet()

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
//        TrackingHandlerClass.stopTracking()
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

//                    setConfigForNewVersion()

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

                    if (SkyblockCollectionTracker.configManager.config!!.about.update != 0) {
                        CompletableFuture.runAsync {
                            RepoUtils.checkForUpdates(SkyblockCollectionTracker.configManager.config!!.about.update)
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

//    private fun setConfigForNewVersion() {
//        val scale = SkyblockCollectionTracker.configManager.config!!.overlay.overlaySingle.overlayPosition.scale
//        if(scale == 0.0f){
//            SkyblockCollectionTracker.configManager.config!!.overlay.overlaySingle.overlayPosition.setScaling(1.0f)
//        }
//    }

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

    fun getScoreboardTitle(): String? {
        val world =Minecraft.getInstance().level ?: return null

        val objective = world.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
        val displayName = objective.displayName?.string ?: return null
        return displayName
    }

    // Method taken from Skyhanni
    private fun CharSequence.removeColor(keepFormatting: Boolean = false): String {
        // Glossary:
        // Formatting indicator: The '§' character indicating the beginning of a formatting sequence
        // Formatting code: The character following a formatting indicator which specifies what color or text style this sequence corresponds to
        // Formatting sequence: The combination of a formatting indicator and code that changes the color or format of a string

        // Flag for whether there is a text style (non-color and non-reset formatting code) currently being applied
        var isFormatted = false

        // Find the first formatting indicator
        var nextFormattingSequence = indexOf('§')

        // If this string does not contain any formatting indicators, just return this string directly
        if (nextFormattingSequence < 0) return this.toString()

        // Let's create a new string, and pre-allocate enough space to store this entire string
        val cleanedString = StringBuilder(this.length)

        // Read index stores the position in `this` which we have written up until now
        // a/k/a where we need to start reading from
        var readIndex = 0

        // As long as there still is a formatting indicator left in our string
        while (nextFormattingSequence >= 0) {

            // Write everything from the read index up to the next formatting indicator into our clean string
            cleanedString.append(this, readIndex, nextFormattingSequence)

            // Get the formatting code (note: this may not be a valid formatting code)
            val formattingCode = this.getOrNull(nextFormattingSequence + 1)

            // If the next formatting sequence's code indicates a non-color format and we should keep those
            if (keepFormatting && formattingCode in formattingChars) {
                // Update formatted flag based on whether this is a reset or a style format code
                isFormatted = formattingCode?.lowercaseChar() != 'r'

                // Set the readIndex to the formatting indicator, so that the next loop will start writing from that paragraph symbol
                readIndex = nextFormattingSequence
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex + 1)
            } else {
                // If this formatting sequence should be skipped (either a color code, or !keepFormatting or an incomplete formatting sequence without a code)

                // If being formatted and color code encountered, reset the current formatting code
                if (isFormatted && formattingCode in colorChars) {
                    cleanedString.append("§r")
                    isFormatted = false
                }

                // Set the readIndex to after this formatting sequence, so that the next loop will skip over it before writing the string
                readIndex = nextFormattingSequence + 2
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex)

                // If the next read would be out of bound, reset the readIndex to the very end of the string, resulting in a "" string to be appended
                readIndex = readIndex.coerceAtMost(this.length)
            }
        }
        // Finally, after the last formatting sequence was processed, copy over the last sequence of the string
        cleanedString.append(this, readIndex, this.length)

        // And turn the string builder into a string
        return cleanedString.toString()
    }
}