package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.scheduler
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.trackingTask
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.gemstoneVariant
import io.github.chindeaone.collectiontracker.config.multiDetailedSummary
import io.github.chindeaone.collectiontracker.config.multiTrackingSummary
import io.github.chindeaone.collectiontracker.config.summaryStats
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setMultiTrackingOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.gui.overlays.saveMilestonesProgress
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearAllCache
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearCollectionCache
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates.lastMultiCollectionUpdate
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatInWords
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatCollectionName
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener
import io.github.chindeaone.collectiontracker.utils.parser.CollectionParser
import io.github.chindeaone.collectiontracker.utils.toColor
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object MultiTrackingHandler  {

    private val logger: Logger = LogManager.getLogger(MultiTrackingHandler::class.java)

    @Volatile
    var isMultiTracking = false
    var isMultiPaused = false
    var leaderboardTrackingInitialized = false

    var multiStartTime: EpochMark? = null
    private var multiPauseDuration: Duration = Duration.ZERO
    var multiLastTrackTime: EpochMark? = null

    private const val RESETS: Int = 10
    private var multiRestartCount: Int = 0
    private var multiFirstRestartTime: EpochMark? = null

    fun startMultiTracking() {
        logger.info("[SCT]: Starting multi tracking for player ${PlayerData.playerName}")

        setMultiTrackingOverlayRendering(true)

        // Always do initial fetch
        MultiDataFetcher.fetchMultiCollectionData()

        if (apiTracking) {
            trackingTask = scheduler.scheduleWithFixedDelay(
                { MultiDataFetcher.fetchMultiCollectionData(false) }, 5, 5, TimeUnit.MINUTES)
        }
    }

    fun initMultiTracking() {
        multiStartTime = EpochMark.now()
        multiLastTrackTime = EpochMark.now()
        multiPauseDuration = Duration.ZERO

        isMultiTracking = true
        isMultiPaused = false
        leaderboardTrackingInitialized = collectionLeaderboard
    }

    fun stopMultiTrackingManual() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active!")
            logger.warn("[SCT]: Attempted to stop multi-tracking manually, but no multi-tracking is active.")
            return
        }

        sendMessage("§cStopped multi-tracking!")
        logger.info("[SCT]: Multi-tracking stopped.")

        resetMultiTrackingData(false)
    }

    fun stopMultiTracking() {
        if (!isMultiTracking) {
            logger.warn("[SCT]: Attempted to stop multi-tracking, but no multi-tracking is active.")
            return
        }

        if (!server) {
            logger.info("[SCT]: Multi-tracking stopped because player disconnected from the server.")
            return
        }

        sendMessage("§cAPI server is down. Stopping multi-tracker.")
        logger.info("[SCT]: Multi-tracking stopped because the API server is down.")

        resetMultiTrackingData(false)
    }

    fun restartMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active to restart!")
            logger.warn("[SCT]: Attempted to restart multi-tracking, but no multi-tracking is active.")
            return
        }

        val firstMultiRestart = multiFirstRestartTime
        if (multiRestartCount == 0 || firstMultiRestart == null || firstMultiRestart.timePassedSince() >= 1.hours) {
            multiFirstRestartTime = EpochMark.now()
            multiRestartCount = 0
        }

        if (multiRestartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart multi-tracking.")
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart multi-tracking.")
            return
        }

        multiRestartCount++
        resetMultiTrackingData(true)
        startMultiTracking()
    }

    private fun resetMultiTrackingData(restart: Boolean) {
        if (multiTrackingSummary) sendMultiRates()

        resetVariables()
        if (restart) {
            clearCollectionCache()
        } else {
            clearAllCache()
        }

        saveMilestonesProgress(false)

        val now = EpochMark.now()
        if (!restart) {
            multiLastTrackTime = now
            clearFetchedData()
        } else multiLastTrackTime = now - 10.seconds

        setMultiTrackingOverlayRendering(false)
        MultiCollectionOverlay.trackingDirty = false
    }

    private fun clearFetchedData() {
        CollectionsManager.resetMultiCollections()
        BazaarCollectionsManager.resetBazaarData()
    }

    private fun resetVariables() {
        isMultiTracking = false
        isMultiPaused = false
        multiStartTime = null
        multiPauseDuration = Duration.ZERO
        leaderboardTrackingInitialized = false

        if (trackingTask != null) {
            trackingTask!!.cancel(false)
            trackingTask = null
        }

        clearMaps()
        lastMultiCollectionUpdate = null

        riftTracking = false
        InventoryListener.clearInventoryState()
    }

    fun clearMaps() {
        MultiTrackingRates.collectionAmounts.clear()
        MultiTrackingRates.collectionPerHour.clear()
        MultiTrackingRates.collectionMade.clear()
        MultiTrackingRates.sessionStartCollections.clear()
        MultiTrackingRates.lastApiCollections.clear()
        MultiTrackingRates.moneyPerHourNPC.clear()
        MultiTrackingRates.moneyMadeNPC.clear()
        MultiTrackingRates.moneyMadeBazaar.clear()
        MultiTrackingRates.moneyPerHourBazaar.clear()
        MultiTrackingRates.seenGemstones.clear()
    }

    fun pauseMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active!")
            logger.warn("[SCT]: Attempted to pause multi-tracking, but no multi-tracking is active.")
        }

        if (isMultiPaused) {
            sendMessage("§cMulti-tracking is already paused!")
            logger.warn("[SCT]: Attempted to pause multi-tracking, but multi-tracking is already paused.")
            return
        }

        isMultiPaused = true
        multiStartTime?.let { multiPauseDuration += it.timePassedSince() }
        sendMessage("§7Multi-tracking paused.")
        logger.info("[SCT]: Multi-tracking paused.")

    }

    fun resumeMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active!")
            logger.warn("[SCT]: Attempted to resume multi-tracking, but no multi-tracking is active.")
            return
        }

        if (!isMultiPaused) {
            sendMessage("§cMulti-tracking is already active!")
            logger.warn("[SCT]: Attempted to resume multi-tracking, but multi-tracking is already active.")
            return
        }

        isMultiPaused = false
        multiStartTime = EpochMark.now()
        sendMessage("§7Resuming multi-tracking.")
        logger.info("[SCT]: Resuming multi-tracking.")
    }

    fun resumeMultiRiftTracking() {
        if (!isMultiTracking || !isMultiPaused) return

        if (!riftTracking) {
            logger.warn("[SCT]: Attempted to resume Rift multi tracking, but none of the tracked collections are Rift collections.")
            return
        }

        multiStartTime = EpochMark.now()
        isMultiPaused = false

        sendMessage("§7Resuming multi tracking after rejoining The Rift.")
        logger.info("[SCT]: Resuming multi tracking after rejoining The Rift.")
    }

    fun pauseMultiRiftTracking() {
        if (!isMultiTracking || isMultiPaused) return

        if (!riftTracking) {
            logger.warn("[SCT]: Attempted to pause Rift multi tracking, but none of the tracked collections are Rift collections.")
            return
        }

        multiStartTime?.let { multiPauseDuration += it.timePassedSince() }
        isMultiPaused = true

        sendMessage("§7Pausing multi tracking before leaving The Rift.")
        logger.info("[SCT]: Pausing multi tracking before leaving The Rift.")
    }

    private val totalMultiUptime: Duration
        get() = if (isMultiPaused || multiStartTime == null) {
            multiPauseDuration
        } else {
            multiPauseDuration + multiStartTime!!.timePassedSince()
        }

    val multiUptimeInSeconds: Long
        get() = totalMultiUptime.inWholeSeconds

    private val multiUptimeInWords: String
        get() = totalMultiUptime.formatInWords()

    val multiUptime: String
        get() = totalMultiUptime.formatClock()

    fun sendMultiRates() {
        MultiTrackingRates.updateRates()

        val lines = mutableListOf<Component>()

        val useBazaar = useBazaar
        val useMotes = !useBazaar && riftTracking

        val variant = gemstoneVariant.toString()
        val suffix = CollectionParser.bazaarPriceTypeSuffix()
        val bazaarSuffix = if (useBazaar) if (suffix.contains("BUY")) "Instant Buy" else "Instant Sell" else ""
        val typeKey = CollectionParser.bazaarTypeKey()

        val pricingLabel = if (useBazaar) bazaarSuffix else if (useMotes) "NPC / Motes" else "NPC"

        var totalMoneyMade = 0L
        var totalMoneyRate = 0L

        var totalGemstoneMoneyMade = 0L
        var totalGemstoneMoneyRate = 0L

        val trackedCollections = CollectionTracker.collectionList
        val collectionLines = mutableListOf<Component>()

        val summaryStatsName = summaryStats.name

        // Non gemstone collections
        for (coll in trackedCollections) {
            if (coll == "gemstone") continue

            val collName = formatCollectionName(coll)
            val formattedName = collName.toColor()

            val currentMoney: Long
            val currentRate: Long

            if (!useBazaar) {
                currentMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(coll, 0L)
                currentRate = MultiTrackingRates.moneyPerHourNPC.getOrDefault(coll, 0L)
            } else {
                val type = CollectionsManager.multiCollectionTypes[coll]
                val key = when (type) {
                    "normal" -> "${coll}_normal$suffix"
                    "enchanted" -> "${coll}_${typeKey}$suffix"
                    else -> ""
                }

                currentMoney = MultiTrackingRates.moneyMadeBazaar.getOrDefault(key, 0L)
                currentRate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault(key, 0L)
            }

            totalMoneyMade += currentMoney
            totalMoneyRate += currentRate

            val collectionMade = MultiTrackingRates.collectionMade[coll] ?: 0L
            val collectionRate = MultiTrackingRates.collectionPerHour[coll] ?: 0L

            val line = Component.literal("   ").append(formattedName).append("§r: ")

            when (summaryStatsName) {
                "COLLECTION" -> line.append("§f${NumbersUtils.formatNumber(collectionMade)} §7(${NumbersUtils.formatNumber(collectionRate)}/h)")

                "MONEY" -> {
                    if (useMotes) {
                        line.append("§d${NumbersUtils.formatNumber(currentMoney)} Motes §7(${NumbersUtils.formatNumber(currentRate)} Motes/h)")
                    } else {
                        line.append("§a$${NumbersUtils.formatNumber(currentMoney)} §7($${NumbersUtils.formatNumber(currentRate)}/h)")
                    }
                }

                "BOTH" -> {
                    line.append("§f${NumbersUtils.formatNumber(collectionMade)} §7(${NumbersUtils.formatNumber(collectionRate)}/h)   ")

                    if (useMotes) {
                        line.append("§d${NumbersUtils.formatNumber(currentMoney)} Motes §7(${NumbersUtils.formatNumber(currentRate)} Motes/h)")
                    } else {
                        line.append("§a$${NumbersUtils.formatNumber(currentMoney)} §7($${NumbersUtils.formatNumber(currentRate)}/h)")
                    }
                }

                else -> continue
            }

            collectionLines.add(line)
        }

        // Gemstones
        val gemstoneLines = mutableListOf<Component>()

        if (trackedCollections.contains("gemstone")) {
            for (gemstone in MultiTrackingRates.seenGemstones) {
                val name = formatCollectionName(gemstone)
                val formattedName = name.toColor()

                val gemMoney: Long
                val gemRate: Long

                val keyPrefix = ("${gemstone}_$variant").uppercase()

                if (!useBazaar) {
                    gemMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(keyPrefix, 0L)
                    gemRate = MultiTrackingRates.moneyPerHourNPC.getOrDefault(keyPrefix, 0L)
                } else {
                    val key = keyPrefix + suffix
                    gemMoney = MultiTrackingRates.moneyMadeBazaar.getOrDefault(key, 0L)
                    gemRate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault(key, 0L)
                }

                totalGemstoneMoneyMade += gemMoney
                totalGemstoneMoneyRate += gemRate

                val amount = MultiTrackingRates.collectionMade[gemstone] ?: 0L
                val rate = MultiTrackingRates.collectionPerHour[gemstone] ?: 0L

                val line = Component.literal("    - ").append(formattedName).append(": ")

                when (summaryStatsName) {
                    "COLLECTION" -> line.append("§f${NumbersUtils.formatNumber(amount)} §7(${NumbersUtils.formatNumber(rate)}/h)")

                    "MONEY" -> line.append("§a$${NumbersUtils.formatNumber(gemMoney)} §7($${NumbersUtils.formatNumber(gemRate)}/h)")

                    "BOTH" -> line.append("§f${NumbersUtils.formatNumber(amount)} §7(${NumbersUtils.formatNumber(rate)}/h)   §a$${NumbersUtils.formatNumber(gemMoney)} §7($${NumbersUtils.formatNumber(gemRate)}/h)")
                }

                gemstoneLines.add(line)
            }

            totalMoneyMade += totalGemstoneMoneyMade
            totalMoneyRate += totalGemstoneMoneyRate
        }

        if (summaryStatsName == "MONEY" || summaryStatsName == "BOTH") {
            if (totalMoneyMade > 0) {
                val totalLine = Component.literal("   ")

                if (useMotes) {
                    totalLine.append("§eTotal Motes: §f${NumbersUtils.formatNumber(totalMoneyMade)}   §eRate: §f${NumbersUtils.formatNumber(totalMoneyRate)} Motes/h   §ePricing: §f$pricingLabel")
                } else {
                    totalLine.append("§eTotal Profit: §f$${NumbersUtils.formatNumber(totalMoneyMade)}   §eRate: §f$${NumbersUtils.formatNumber(totalMoneyRate)}/h   §ePricing: §f$pricingLabel")
                }

                lines.add(totalLine)
                lines.add(Component.empty())
            }
        }

        lines.addAll(collectionLines)

        if (trackedCollections.contains("gemstone")) {
            val summaryLine = Component.literal("   Gemstones (${variant.lowercase()}): ").withColor(ColorUtils.collectionColors["gemstone"] ?: Colors.LIGHT_PURPLE.color)

            when (summaryStatsName) {
                "COLLECTION" -> {
                    val totalColl = MultiTrackingRates.collectionMade["gemstone"] ?: 0L
                    val totalRate = MultiTrackingRates.collectionPerHour["gemstone"] ?: 0L

                    summaryLine.append("§f${NumbersUtils.formatNumber(totalColl)} §7(${NumbersUtils.formatNumber(totalRate)}/h)")
                }

                "MONEY" -> summaryLine.append("§a$${NumbersUtils.formatNumber(totalGemstoneMoneyMade)} §7($${NumbersUtils.formatNumber(totalGemstoneMoneyRate)}/h)")

                "BOTH" -> {
                    val totalColl = MultiTrackingRates.collectionMade["gemstone"] ?: 0L
                    val totalRate = MultiTrackingRates.collectionPerHour["gemstone"] ?: 0L

                    summaryLine.append(
                        "§f${NumbersUtils.formatNumber(totalColl)} §7(${NumbersUtils.formatNumber(totalRate)}/h)   §a$${NumbersUtils.formatNumber(totalGemstoneMoneyMade)} §7($${NumbersUtils.formatNumber(totalGemstoneMoneyRate)}/h)"
                    )
                }
            }

            lines.add(summaryLine)

            if (multiDetailedSummary) lines.addAll(gemstoneLines)
        }

        lines.add(Component.empty())
        lines.add(Component.literal("   §7Elapsed time: §f${multiUptimeInWords}"))

        ChatUtils.sendSummary("§e§lMulti-Tracking Summary", lines)
    }
}