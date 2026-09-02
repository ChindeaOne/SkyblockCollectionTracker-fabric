package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.scheduler
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.trackingTask
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarPriceType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getGemstoneVariant
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isApiTrackingEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isCollectionLeaderboardEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowTrackingRatesAtEndOfSession
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isUsingBazaar
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setTrackingOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.clearAllCache
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.clearCollectionCache
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.fetchData
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.highestCollectionPerHour
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.highestRatePerHourNPC
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lowestCollectionPerHour
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lowestRatePerHourNPC
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lowestRatesPerHourBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.highestRatesPerHourBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionMade
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionPerHour
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.moneyMade
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.moneyPerHourBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.moneyPerHourNPC
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.nextRankAmount
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.nextRankUsername
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.playerCurrentRank
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.etaToNextRank
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lastApiCollection
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.sacksCollectionGained
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.sessionStartCollection
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lastCollectionTime
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionTillNextRank
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

object TrackingHandler {
    private val logger: Logger = LogManager.getLogger(TrackingHandler::class.java)
    val COOLDOWN_MILLIS: Long = TimeUnit.SECONDS.toMillis(10) // 10-second cooldown

    @JvmField
    @Volatile
    var isTracking: Boolean = false
    var isPaused: Boolean = false
    var leaderboardTrackingInitialized: Boolean = false

    var startTime: Long = 0
    private var lastTime: Long = 0
    var lastTrackTime: Long = 0

    private const val RESETS = 10
    private var restartCount = 0
    private var firstRestartTime: Long = 0

    fun startTracking() {
        logger.info("[SCT]: Tracking started for collection: {}", collection)

        setTrackingOverlayRendering(true)

        // Always do initial fetch
        fetchData(true)

        // Schedule API fetching
        CollectionTracker.isApiTracking = isApiTrackingEnabled()

        if (CollectionTracker.isApiTracking) {
            trackingTask = scheduler.scheduleWithFixedDelay({ fetchData(false) }, 5, 5, TimeUnit.MINUTES)
        }
    }

    fun initTracking(now: Long) {
        lastTrackTime = now

        isTracking = true
        isPaused = false
        leaderboardTrackingInitialized = isCollectionLeaderboardEnabled()

        startTime = now
        lastTime = 0

        if (CollectionsManager.collectionType == null) {
            logger.error("[SCT]: Collection type is null for collection: {}", collection)
        }
    }

    fun stopTrackingManual() {
        if (isTracking) {
            sendMessage("§cStopped tracking!", true)

            resetTrackingData(false)

            logger.info("[SCT]: Tracking stopped.")
        } else {
            sendMessage("§cNo tracking active!", true)
            logger.warn("[SCT]: Attempted to stop tracking manually, but no tracking is active.")
        }
    }

    fun stopTracking() {
        if (isTracking) {
            if (!server) {
                logger.info("[SCT]: Tracking stopped because player disconnected from the server.")
            } else {
                sendMessage("§cAPI server is down. Stopping the tracker.", true)
                logger.info("[SCT]: Tracking stopped because the API server is down.")
            }

            resetTrackingData(false)
        } else {
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.")
        }
    }

    fun restartTracking() {
        if (!isTracking) {
            sendMessage("§cNo tracking active to restart!", true)
            logger.warn("[SCT]: Attempted to restart tracking, but no tracking is active.")
            return
        }

        if (restartCount == 0) {
            firstRestartTime = System.currentTimeMillis()
        } else {
            val elapsedTime = System.currentTimeMillis() - firstRestartTime
            if (elapsedTime >= TimeUnit.HOURS.toMillis(1)) {
                restartCount = 0
                firstRestartTime = System.currentTimeMillis()
            }
        }

        if (restartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart tracking.", true)
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart tracking.")
            return
        }

        restartCount++
        resetTrackingData(true)
        startTracking()
    }

    private fun resetTrackingData(restart: Boolean) {
        if (isShowTrackingRatesAtEndOfSession()) sendRates()

        resetVariables()
        // Clear cached data
        if (restart) {
            clearCollectionCache()
        } else {
            clearAllCache()
        }

        // Reset uptime
        val now = System.currentTimeMillis()
        if (!restart) {
            lastTrackTime = now
            clearFetchedData()
        } else lastTrackTime = now - COOLDOWN_MILLIS

        setTrackingOverlayRendering(false)
        CollectionOverlay.trackingDirty = false
    }

    private fun clearFetchedData() {
        CollectionsManager.resetCollections()
        BazaarCollectionsManager.resetBazaarData()
    }

    private fun resetVariables() {
        isTracking = false
        isPaused = false
        leaderboardTrackingInitialized = false
        startTime = 0
        lastTime = 0

        if (trackingTask != null) {
            trackingTask!!.cancel(false)
            trackingTask = null
        }

        // Reset collection tracking
        lastApiCollection = -1L
        sacksCollectionGained = 0L
        sessionStartCollection = -1L
        lastCollectionTime = -1L

        // Clear profit map
        moneyPerHourBazaar.clear()
        moneyMade.clear()

        // Reset highest/lowest rates
        resetLowestHighestRates()

        // Reset leaderboard tracking
        playerCurrentRank = -1
        nextRankUsername = null
        nextRankAmount = -1L
        etaToNextRank = null
        collectionTillNextRank = -1L
    }

    private fun resetLowestHighestRates() {
        highestCollectionPerHour = 0
        lowestCollectionPerHour = Long.MAX_VALUE
        highestRatePerHourNPC = 0
        lowestRatePerHourNPC = Long.MAX_VALUE
        lowestRatesPerHourBazaar.clear()
        highestRatesPerHourBazaar.clear()
    }

    fun pauseTracking() {
        if (isTracking) {
            if (isPaused) {
                sendMessage("§cTracking is already paused!", true)
                logger.warn("[SCT]: Attempted to pause tracking, but tracking is already paused.")
                return
            }
            isPaused = true
            lastTime += (System.currentTimeMillis() - startTime) / 1000
            sendMessage("§7Tracking paused.", true)
            logger.info("[SCT]: Tracking paused.")
        } else {
            sendMessage("§cNo tracking active!", true)
            logger.warn("[SCT]: Attempted to pause tracking, but no tracking is active.")
        }
    }

    fun resumeTracking() {
        if (!isTracking) {
            sendMessage("§cNo tracking active!", true)
            logger.warn("[SCT]: Attempted to resume tracking, but no tracking is active.")
            return
        }

        if (isPaused) {
            sendMessage("§7Resuming tracking.", true)
            logger.info("[SCT]: Resuming tracking.")
            startTime = System.currentTimeMillis()
            isPaused = false
        } else {
            sendMessage("§cTracking is already active!", true)
            logger.warn("[SCT]: Attempted to resume tracking, but tracking is already active.")
        }
    }

    fun resumeRiftTracking() {
        if (!isTracking) {
            return
        }

        if (!CollectionsManager.isRiftCollection(collection)) {
            logger.warn("[SCT]: Attempted to resume Rift tracking, but current collection is not a Rift collection.")
            return
        }

        if (!isPaused) return

        startTime = System.currentTimeMillis()
        isPaused = false

        sendMessage("§7Resuming tracking after rejoining The Rift.", true)
        logger.info("[SCT]: Resuming tracking after rejoining The Rift.")
    }

    fun pauseRiftTracking() {
        if (!isTracking) {
            return
        }

        if (!CollectionsManager.isRiftCollection(collection)) {
            logger.warn("[SCT]: Attempted to pause Rift tracking, but current collection is not a Rift collection.")
            return
        }

        if (isPaused) return

        lastTime += (System.currentTimeMillis() - startTime) / 1000
        isPaused = true

        sendMessage("§7Pausing tracking before leaving The Rift.", true)
        logger.info("[SCT]: Pausing tracking before leaving The Rift.")
    }

    private fun sendRates() {
        val collectionDisplay = StringUtils.formatCollectionName(collection)

        val lines: MutableList<Component> = mutableListOf()
        lines.add(Component.literal(String.format("   §aCollection tracked: §f%s", collectionDisplay)))
        lines.add(
            Component.literal(String.format("   §b%s Made: §f%s   §bRate: §f%s/h", collectionDisplay, formatNumber(collectionMade), formatNumber(collectionPerHour)))
        )

        val useBazaar = isUsingBazaar()
        val bazaarType = getBazaarType()

        if (!useBazaar) {
            val npcMoney: Long = moneyMade["NPC"] ?: 0L
            if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                lines.add(
                    Component.literal(String.format("   §6Motes: §f$%s   §6Rate: §f%s/h", formatNumber(npcMoney), formatNumber(moneyPerHourNPC)))
                )
            } else if (NpcPrices.getNpcPrice(collection) != 0) {
                lines.add(
                    Component.literal(String.format("   §6Money (NPC): §f$%s   §6Rate: §f$%s/h", formatNumber(npcMoney), formatNumber(moneyPerHourNPC)))
                )
            }
        } else {
            val suffix =
                if (getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY) "_INSTANT_BUY" else "_INSTANT_SELL"
            when (CollectionsManager.collectionType) {
                "normal" -> {
                    val bazMoney = moneyMade.getOrDefault(CollectionsManager.collectionType + suffix, 0L)
                    val bazRate = moneyPerHourBazaar.getOrDefault(CollectionsManager.collectionType + suffix, 0L)
                    lines.add(
                        Component.literal(String.format("   §6Money (Bazaar): §f$%s   §6Rate: §f$%s/h", formatNumber(bazMoney), formatNumber(bazRate)))
                    )
                }

                "enchanted" -> {
                    val key = if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION)
                        "Enchanted version"
                    else
                        "Super Enchanted version"
                    val money = moneyMade.getOrDefault(key + suffix, 0L)
                    val rate = moneyPerHourBazaar.getOrDefault(key + suffix, 0L)
                    lines.add(
                        Component.literal(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(money), formatNumber(rate)))
                    )
                }

                "gemstone" -> {
                    val variant: String = getGemstoneVariant().toString()
                    val gMoney = moneyMade.getOrDefault(variant + suffix, 0L)
                    val gRate = moneyPerHourBazaar.getOrDefault(variant + suffix, 0L)
                    lines.add(
                        Component.literal(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(gMoney), formatNumber(gRate)))
                    )
                }
            }
        }

        lines.add(
            Component.literal(String.format("   §7Elapsed time: §f%s", uptimeInWords))
        )

        // If no collection update, skip best/worst rates
        if (collectionMade == 0L) {
            ChatUtils.sendSummary("§e§lTracking Summary", lines)
            return
        }

        lines.add(Component.empty())
        lines.add(Component.literal("   §eBest/Worst Rates:"))
        lines.add(Component.empty())

        // Collection extremes
        if (highestCollectionPerHour > 0) {
            lines.add(
                Component.literal(String.format("   §6Best collection rate: §f%s coll/h", formatNumber(highestCollectionPerHour)))
            )
        }
        if (lowestCollectionPerHour > 0 && lowestCollectionPerHour < Long.MAX_VALUE) {
            lines.add(
                Component.literal(String.format("   §6Worst collection rate: §f%s coll/h", formatNumber(lowestCollectionPerHour)))
            )
        }

        if (!useBazaar) {
            // NPC money extremes
            if (highestRatePerHourNPC > 0) {
                if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(Component.literal(String.format("   §6Best motes rate: §f%s/h", formatNumber(highestRatePerHourNPC))))
                } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(
                    Component.literal(String.format("   §6Best NPC money rate: §f$%s/h", formatNumber(highestRatePerHourNPC)))
                )
            }
            if (lowestRatePerHourNPC > 0 && lowestRatePerHourNPC < Long.MAX_VALUE) {
                if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(
                        Component.literal(String.format("   §6Worst motes rate: §f%s/h", formatNumber(lowestRatePerHourNPC)))
                    )
                } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(
                    Component.literal(String.format("   §6Worst NPC money rate: §f$%s/h", formatNumber(lowestRatePerHourNPC)))
                )
            }
        } else {
            // Bazaar extremes per variant
            if (!moneyPerHourBazaar.isEmpty()) {
                val suffix =
                    if (getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY) "_INSTANT_BUY" else "_INSTANT_SELL"
                when (CollectionsManager.collectionType) {
                    "normal" -> {
                        val low = lowestRatesPerHourBazaar.getOrDefault("normal$suffix", 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault("normal$suffix", 0L)

                        lines.add(Component.literal(String.format("   §6Best money rate: §f$%s/h", formatNumber(high))))
                        lines.add(Component.literal(String.format("   §6Worst money rate: §f$%s/h", formatNumber(low))))
                    }

                    "enchanted" -> {
                        val key = if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION)
                            "Enchanted version"
                        else
                            "Super Enchanted version"
                        val low = lowestRatesPerHourBazaar.getOrDefault(key + suffix, 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault(key + suffix, 0L)

                        lines.add(Component.literal(String.format("   §6Best money rate: §f$%s/h", formatNumber(high))))
                        lines.add(Component.literal(String.format("   §6Worst money rate: §f$%s/h", formatNumber(low))))
                    }

                    "gemstone" -> {
                        val variant: String = getGemstoneVariant().toString()
                        val low = lowestRatesPerHourBazaar.getOrDefault(variant + suffix, 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault(variant + suffix, 0L)

                        lines.add(Component.literal(String.format("   §6Best money rate: §f$%s/h", formatNumber(high))))
                        lines.add(Component.literal(String.format("   §6Worst money rate: §f$%s/h", formatNumber(low))))
                    }
                }
            }
        }
        ChatUtils.sendSummary("§e§lTracking Summary", lines)
    }

    val uptimeInSeconds: Long
        get() {
            return if (isPaused) {
                lastTime
            } else {
                lastTime + (System.currentTimeMillis() - startTime) / 1000
            }
        }

    private val uptimeInWords: String
        get() {
            val uptime = lastTime + (System.currentTimeMillis() - startTime) / 1000
            return StringUtils.formatTimeIntoText(uptime)
        }

    @JvmStatic
    val uptime: String
        get() {
            val uptime = if (isPaused) {
                lastTime
            } else {
                lastTime + (System.currentTimeMillis() - startTime) / 1000
            }
            return StringUtils.formatTime(uptime)
        }
}
