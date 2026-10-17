package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.isRiftCollection
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.scheduler
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.trackingTask
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.collectionConfig
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.gemstoneVariant
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setTrackingOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.gui.overlays.saveMilestonesProgress
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
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.lastCollectionUpdate
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionTillNextRank
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatInWords
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.parser.CollectionParser
import io.github.chindeaone.collectiontracker.utils.toColor
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object TrackingHandler {
    private val logger: Logger = LogManager.getLogger(TrackingHandler::class.java)

    @Volatile
    var isTracking: Boolean = false
    var isPaused: Boolean = false
    var leaderboardTrackingInitialized: Boolean = false

    var startTime: EpochMark? = null
    private var pauseDuration: Duration = Duration.ZERO
    var lastTrackTime: EpochMark? = null

    private const val RESETS = 10
    private var restartCount = 0
    private var firstRestartTime: EpochMark? = null

    fun startTracking() {
        logger.info("[SCT]: Started tracking collection $collection")

        setTrackingOverlayRendering(true)

        // Always do initial fetch
        fetchData(true)

        // Schedule API fetching
        CollectionTracker.isApiTracking = apiTracking

        if (CollectionTracker.isApiTracking) {
            trackingTask = scheduler.scheduleWithFixedDelay({ fetchData(false) }, 5, 5, TimeUnit.MINUTES)
        }
    }

    fun initTracking() {
        lastTrackTime = EpochMark.now()

        isTracking = true
        isPaused = false
        leaderboardTrackingInitialized = collectionLeaderboard

        startTime = EpochMark.now()
        pauseDuration = Duration.ZERO

        if (collectionType == null) logger.error("[SCT]: Collection type is null for collection $collection")
    }

    fun stopTrackingManual() {
        if (!isTracking) {
            sendMessage("§cNo tracking active!")
            logger.warn("[SCT]: Attempted to stop tracking manually, but no tracking is active.")
            return
        }

        sendMessage("§cStopped tracking!")
        logger.info("[SCT]: Tracking stopped.")

        resetTrackingData(false)
    }

    fun stopTracking() {
        if (!isTracking) {
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.")
            return
        }

        if (!server) {
            logger.info("[SCT]: Tracking stopped because player disconnected from the server.")
            return
        }

        sendMessage("§cAPI server is down. Stopping the tracker.")
        logger.info("[SCT]: Tracking stopped because the API server is down.")

        resetTrackingData(false)
    }

    fun restartTracking() {
        if (!isTracking) {
            sendMessage("§cNo tracking active to restart!")
            logger.warn("[SCT]: Attempted to restart tracking, but no tracking is active.")
            return
        }

        val firstRestart = firstRestartTime
        if (restartCount == 0 || firstRestart == null || firstRestart.timePassedSince() >= 1.hours) {
            firstRestartTime = EpochMark.now()
            restartCount = 0
        }

        if (restartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart tracking.")
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart tracking.")
            return
        }

        restartCount++
        resetTrackingData(true)
        startTracking()
    }

    private fun resetTrackingData(restart: Boolean) {
        if (collectionConfig.showTrackingRatesAtEndOfSession) sendRates()

        resetVariables()
        // Clear cached data
        if (restart) {
            clearCollectionCache()
        } else {
            clearAllCache()
        }

        saveMilestonesProgress(true)

        // Reset uptime
        val now = EpochMark.now()
        if (!restart) {
            lastTrackTime = now
            clearFetchedData()
        } else lastTrackTime = now - 10.seconds

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
        startTime = null
        pauseDuration = Duration.ZERO

        if (trackingTask != null) {
            trackingTask!!.cancel(false)
            trackingTask = null
        }

        // Reset collection tracking
        lastApiCollection = -1L
        sacksCollectionGained = 0L
        sessionStartCollection = -1L
        lastCollectionUpdate = null

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
        if (!isTracking) {
            sendMessage("§cNo tracking active!")
            logger.warn("[SCT]: Attempted to pause tracking, but no tracking is active.")
            return
        }

        if (isPaused) {
            sendMessage("§cTracking is already paused!")
            logger.warn("[SCT]: Attempted to pause tracking, but tracking is already paused.")
            return
        }

        isPaused = true
        startTime?.let { pauseDuration += it.timePassedSince() }

        sendMessage("§7Tracking paused.")
        logger.info("[SCT]: Tracking paused.")
    }

    fun resumeTracking() {
        if (!isTracking) {
            sendMessage("§cNo tracking active!")
            logger.warn("[SCT]: Attempted to resume tracking, but no tracking is active.")
            return
        }

        if (!isPaused) {
            sendMessage("§cTracking is already active!")
            logger.warn("[SCT]: Attempted to resume tracking, but tracking is already active.")
            return
        }

        startTime = EpochMark.now()
        isPaused = false

        sendMessage("§7Resuming tracking.")
        logger.info("[SCT]: Resuming tracking.")
    }

    fun resumeRiftTracking() {
        if (!isTracking || !isPaused) return

        if (!isRiftCollection(collection)) {
            logger.warn("[SCT]: Attempted to resume Rift tracking, but current collection is not a Rift collection.")
            return
        }

        startTime = EpochMark.now()
        isPaused = false

        sendMessage("§7Resuming tracking after rejoining The Rift.")
        logger.info("[SCT]: Resuming tracking after rejoining The Rift.")
    }

    fun pauseRiftTracking() {
        if (!isTracking || isPaused) return

        if (!isRiftCollection(collection)) {
            logger.warn("[SCT]: Attempted to pause Rift tracking, but current collection is not a Rift collection.")
            return
        }

        startTime?.let { pauseDuration += it.timePassedSince() }
        isPaused = true

        sendMessage("§7Pausing tracking before leaving The Rift.")
        logger.info("[SCT]: Pausing tracking before leaving The Rift.")
    }

    private val totalUptime: Duration
        get() = if (isPaused || startTime == null) {
            pauseDuration
        } else {
            pauseDuration + startTime!!.timePassedSince()
        }

    val uptimeInSeconds: Long
        get() = totalUptime.inWholeSeconds

    private val uptimeInWords: String
        get() = totalUptime.formatInWords()

    val uptime: String
        get() = totalUptime.formatClock()

    private fun sendRates() {
        TrackingRates.updateRates()

        val collectionDisplay = StringUtils.formatCollectionName(collection)

        val lines: MutableList<Component> = mutableListOf()
        lines.add(Component.literal("   §aCollection tracked: §f").append(collectionDisplay.toColor()))
        lines.add(Component.literal("   §b${collectionDisplay} Made: §f${NumbersUtils.formatNumber(collectionMade)}   §bRate: §f${NumbersUtils.formatNumber(collectionPerHour)}/h"))

        val useBazaar = useBazaar
        val typeKey = CollectionParser.bazaarTypeKey()
        val suffix = CollectionParser.bazaarPriceTypeSuffix()

        if (!useBazaar) {
            val npcMoney: Long = moneyMade["NPC"] ?: 0L
            if (isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                lines.add(Component.literal("   §6Motes: §f$${NumbersUtils.formatNumber(npcMoney)}   §6Rate: §f${NumbersUtils.formatNumber(moneyPerHourNPC)}/h"))
            } else if (NpcPrices.getNpcPrice(collection) != 0) {
                lines.add(Component.literal("   §6Money (NPC): §f$${NumbersUtils.formatNumber(npcMoney)}   §6Rate: §f$${NumbersUtils.formatNumber(moneyPerHourNPC)}/h"))
            }
        } else {
            when (collectionType) {
                "normal" -> {
                    val bazMoney = moneyMade.getOrDefault("$collectionType$suffix", 0L)
                    val bazRate = moneyPerHourBazaar.getOrDefault("$collectionType$suffix", 0L)
                    lines.add(Component.literal("   §6Money (Bazaar): §f$${NumbersUtils.formatNumber(bazMoney)}   §6Rate: §f$${NumbersUtils.formatNumber(bazRate)}/h"))
                }

                "enchanted" -> {
                    val money = moneyMade.getOrDefault("$typeKey$suffix", 0L)
                    val rate = moneyPerHourBazaar.getOrDefault("$typeKey$suffix", 0L)
                    lines.add(Component.literal("   §6Money (Bazaar): §f$${NumbersUtils.formatNumber(money)}  §6Rate: §f$${NumbersUtils.formatNumber(rate)}/h"))
                }

                "gemstone" -> {
                    val gemstoneVariant = gemstoneVariant

                    val gMoney = moneyMade.getOrDefault("$gemstoneVariant$suffix", 0L)
                    val gRate = moneyPerHourBazaar.getOrDefault("$gemstoneVariant$suffix", 0L)
                    lines.add(Component.literal("   §6Money (Bazaar): §f$${NumbersUtils.formatNumber(gMoney)}  §6Rate: §f$${NumbersUtils.formatNumber(gRate)}/h"))
                }
            }
        }

        lines.add(Component.literal("   §7Elapsed time: §f${uptimeInWords}"))

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
            lines.add(Component.literal("   §6Best collection rate: §f${NumbersUtils.formatNumber(highestCollectionPerHour)} coll/h"))
        }
        if (lowestCollectionPerHour > 0 && lowestCollectionPerHour < Long.MAX_VALUE) {
            lines.add(Component.literal("   §6Worst collection rate: §f${NumbersUtils.formatNumber(lowestCollectionPerHour)} coll/h"))
        }

        if (!useBazaar) {
            // NPC money extremes
            if (highestRatePerHourNPC > 0) {
                if (isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(Component.literal("   §6Best motes rate: §f${NumbersUtils.formatNumber(highestRatePerHourNPC)}/h"))
                } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(
                    Component.literal("   §6Best NPC money rate: §f$${NumbersUtils.formatNumber(highestRatePerHourNPC)}/h")
                )
            }
            if (lowestRatePerHourNPC > 0 && lowestRatePerHourNPC < Long.MAX_VALUE) {
                if (isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(Component.literal("   §6Worst motes rate: §f${NumbersUtils.formatNumber(lowestRatePerHourNPC)}/h"))
                } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(Component.literal("   §6Worst NPC money rate: §f$${NumbersUtils.formatNumber(lowestRatePerHourNPC)}/h"))
            }
        } else {
            // Bazaar extremes per variant
            if (!moneyPerHourBazaar.isEmpty()) {
                when (collectionType) {
                    "normal" -> {
                        val low = lowestRatesPerHourBazaar.getOrDefault("normal$suffix", 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault("normal$suffix", 0L)

                        lines.add(Component.literal("   §6Best money rate: §f$${NumbersUtils.formatNumber(high)}/h"))
                        lines.add(Component.literal("   §6Worst money rate: §f$${NumbersUtils.formatNumber(low)}/h"))
                    }

                    "enchanted" -> {
                        val low = lowestRatesPerHourBazaar.getOrDefault("$typeKey$suffix", 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault("$typeKey$suffix", 0L)

                        lines.add(Component.literal("   §6Best money rate: §f$${NumbersUtils.formatNumber(high)}/h"))
                        lines.add(Component.literal("   §6Worst money rate: §f$${NumbersUtils.formatNumber(low)}/h"))
                    }

                    "gemstone" -> {
                        val gemstoneVariant = gemstoneVariant

                        val low = lowestRatesPerHourBazaar.getOrDefault("$gemstoneVariant$suffix", 0L)
                        val high = highestRatesPerHourBazaar.getOrDefault("$gemstoneVariant$suffix", 0L)

                        lines.add(Component.literal("   §6Best money rate: §f$${NumbersUtils.formatNumber(high)}/h"))
                        lines.add(Component.literal("   §6Worst money rate: §f$${NumbersUtils.formatNumber(low)}/h"))
                    }
                }
            }
        }
        ChatUtils.sendSummary("§e§lTracking Summary", lines)
    }
}
