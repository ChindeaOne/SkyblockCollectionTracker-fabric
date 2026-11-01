package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager.multiEnchantedRecipes
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager.multiSuperEnchantedRecipes
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.CollectionTier
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.multiCollectionTypes
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarType
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collectionList
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.scheduler
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.trackingTask
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.collectionGemstoneTier
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.collectionTier
import io.github.chindeaone.collectiontracker.config.gemstoneTier
import io.github.chindeaone.collectiontracker.config.multiDetailedSummary
import io.github.chindeaone.collectiontracker.config.multiTrackingSummary
import io.github.chindeaone.collectiontracker.config.summaryStats
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setMultiTrackingOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.gui.overlays.saveMilestonesProgress
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearAllCache
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearCollectionCache
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatInWords
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatDouble
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatLong
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatBazaarItemName
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatCollectionName
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener
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

        MultiTrackingRates.clearCollectionData()
        MultiTrackingRates.clearAllMoneyMaps()
        MultiTrackingRates.clearLeaderboardData()

        riftTracking = false
        InventoryListener.clearInventoryState()

        collectionList.clear()
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



    private fun sendMultiRates() {
        val lines = mutableListOf<Component>()
        val collectionLines = mutableListOf<Component>()
        val gemstoneLines = mutableListOf<Component>()

        val trackedCollections = collectionList
        val summaryStatsName = summaryStats.name

        val shouldUseBazaar = useBazaar
        val useMotes = !shouldUseBazaar && riftTracking

        val priceType = bazaarPriceType
        val pricingGemstoneTier = if (shouldUseBazaar) gemstoneTier else GemstoneTier.ROUGH

        val pricingLabel = when {
            shouldUseBazaar -> priceType.toString()
            useMotes -> "NPC / Motes"
            else -> "NPC"
        }

        var totalMoneyMade = 0L
        var totalMoneyRate = 0L

        var totalGemstoneMoneyMade = 0L
        var totalGemstoneMoneyRate = 0L

        // Non-gemstone collections
        for (coll in trackedCollections) {
            if (coll == "gemstone") continue

            val effectiveBazaarType = getMultiSummaryBazaarType(coll)

            val formattedName = formatCollectionName(coll).toColor()
            val (currentMoney, currentRate) = getMultiSummaryMoney(coll, shouldUseBazaar)

            totalMoneyMade += currentMoney
            totalMoneyRate += currentRate

            val collectionMade = MultiTrackingRates.collectionMade[coll] ?: 0L
            val collectionRate = MultiTrackingRates.collectionRate[coll] ?: 0L

            val (compactedAmount, compactSuffix) =
                getMultiSummaryCompactedCollection(collectionMade, coll)
            val (compactedRate, _) =
                getMultiSummaryCompactedCollection(collectionRate, coll)

            val isEnchantedCollection = multiCollectionTypes[coll] == "enchanted"

            val line = Component.literal("   ").append(formattedName)
            line.append("§r: ")

            when (summaryStatsName) {
                "COLLECTION" -> {
                    line.append(
                        "§f${formatDouble(compactedAmount)} " +
                                "§7(${formatDouble(compactedRate)}/h)"
                    )

                    if (compactSuffix.isNotEmpty()) {
                        line.append(" §7[$compactSuffix]")
                    }
                }

                "MONEY" -> {
                    line.append(getMultiSummaryMoneyText(currentMoney, currentRate, useMotes))

                    if (shouldUseBazaar && isEnchantedCollection) {
                        line.append(" §7[$effectiveBazaarType]")
                    }
                }

                "BOTH" -> {
                    line.append(
                        "§f${formatDouble(compactedAmount)} " +
                                "§7(${formatDouble(compactedRate)}/h)"
                    )

                    if (compactSuffix.isNotEmpty()) {
                        line.append(" §7[$compactSuffix]")
                    }

                    line.append("   ")
                    line.append(getMultiSummaryMoneyText(currentMoney, currentRate, useMotes))

                    if (shouldUseBazaar && isEnchantedCollection) {
                        line.append(" §7[$effectiveBazaarType]")
                    }
                }

                else -> continue
            }

            collectionLines.add(line)

            if (shouldUseBazaar && summaryStatsName != "COLLECTION") {
                addMultiSummaryItemPrice(collectionLines, coll)
            }
        }

        // Gemstones
        if (trackedCollections.contains("gemstone")) {
            for (gemstone in MultiTrackingRates.seenGemstones) {
                val formattedName = formatCollectionName(gemstone).toColor()

                val (gemMoney, gemRate) =
                    getMultiSummaryMoney(gemstone, shouldUseBazaar)

                totalGemstoneMoneyMade += gemMoney
                totalGemstoneMoneyRate += gemRate

                val amount = MultiTrackingRates.collectionMade[gemstone] ?: 0L
                val rate = MultiTrackingRates.collectionRate[gemstone] ?: 0L

                val (compactedAmount, _) =
                    getMultiSummaryCompactedCollection(amount, gemstone)
                val (compactedRate, _) =
                    getMultiSummaryCompactedCollection(rate, gemstone)

                val line = Component.literal("    - ").append(formattedName)
                line.append("§r: ")

                when (summaryStatsName) {
                    "COLLECTION" -> {
                        line.append(
                            "§f${formatDouble(compactedAmount)} " +
                                    "§7(${formatDouble(compactedRate)}/h)"
                        )
                    }

                    "MONEY" -> {
                        line.append(getMultiSummaryMoneyText(gemMoney, gemRate, useMotes))
                    }

                    "BOTH" -> {
                        line.append(
                            "§f${formatDouble(compactedAmount)} " +
                                    "§7(${formatDouble(compactedRate)}/h)   "
                        )

                        line.append(getMultiSummaryMoneyText(gemMoney, gemRate, useMotes))
                    }

                    else -> continue
                }

                gemstoneLines.add(line)

                if (shouldUseBazaar && multiDetailedSummary && summaryStatsName != "COLLECTION") {
                    addMultiSummaryItemPrice(gemstoneLines, gemstone)
                }
            }

            totalMoneyMade += totalGemstoneMoneyMade
            totalMoneyRate += totalGemstoneMoneyRate
        }

        if (summaryStatsName == "MONEY" || summaryStatsName == "BOTH") {
            if (totalMoneyMade > 0L || totalMoneyRate > 0L) {
                val totalLine = Component.literal("   ")

                if (useMotes) {
                    totalLine.append(
                        "§eTotal Motes: §f${formatLong(totalMoneyMade)}   " +
                                "§eRate: §f${formatLong(totalMoneyRate)} Motes/h   " +
                                "§ePricing: §f$pricingLabel"
                    )
                } else {
                    totalLine.append(
                        "§eTotal Profit: §f$${formatLong(totalMoneyMade)}   " +
                                "§eRate: §f$${formatLong(totalMoneyRate)}/h   " +
                                "§ePricing: §f$pricingLabel"
                    )
                }

                lines.add(totalLine)
                lines.add(Component.empty())
            }
        }

        lines.addAll(collectionLines)

        if (trackedCollections.contains("gemstone")) {
            val summaryLine = Component.literal("   Gemstones: ")
                .withColor(
                    ColorUtils.collectionColors["gemstone"]
                        ?: Colors.LIGHT_PURPLE.color
                )

            val totalColl = MultiTrackingRates.collectionMade["gemstone"] ?: 0L
            val totalRate = MultiTrackingRates.collectionRate["gemstone"] ?: 0L

            val (compactedAmount, _) =
                getMultiSummaryCompactedCollection(totalColl, "gemstone")
            val (compactedRate, _) =
                getMultiSummaryCompactedCollection(totalRate, "gemstone")

            when (summaryStatsName) {
                "COLLECTION" -> {
                    summaryLine.append(
                        "§f${formatDouble(compactedAmount)} " +
                                "§7(${formatDouble(compactedRate)}/h) " +
                                "§8[${collectionGemstoneTier.displayName}]"
                    )
                }

                "MONEY" -> {
                    summaryLine.append(
                        getMultiSummaryMoneyText(
                            totalGemstoneMoneyMade,
                            totalGemstoneMoneyRate,
                            useMotes
                        )
                    )

                    if (pricingGemstoneTier != GemstoneTier.ROUGH) {
                        summaryLine.append(" §8[${pricingGemstoneTier.displayName}]")
                    }
                }

                "BOTH" -> {
                    summaryLine.append(
                        "§f${formatDouble(compactedAmount)} " +
                                "§7(${formatDouble(compactedRate)}/h) " +
                                "§8[${collectionGemstoneTier.displayName}]   "
                    )

                    summaryLine.append(
                        getMultiSummaryMoneyText(
                            totalGemstoneMoneyMade,
                            totalGemstoneMoneyRate,
                            useMotes
                        )
                    )

                    if (pricingGemstoneTier != GemstoneTier.ROUGH) {
                        summaryLine.append(" §7[${pricingGemstoneTier.displayName}]")
                    }
                }
            }

            lines.add(summaryLine)

            if (multiDetailedSummary) {
                lines.addAll(gemstoneLines)
            }
        }

        lines.add(Component.empty())
        lines.add(Component.literal("   §7Elapsed time: §f$multiUptimeInWords"))

        ChatUtils.sendSummary("§e§lMulti-Tracking Summary", lines)
    }

    private fun getMultiSummaryItemPrice(coll: String): Float? {
        if (GemstonesManager.checkIfGemstone(coll)) {
            val prices = when (bazaarPriceType) {
                BazaarPriceType.INSTANT_BUY -> GemstonePrices.multiGemstoneInstantBuyPrices
                BazaarPriceType.INSTANT_SELL -> GemstonePrices.multiGemstoneInstantSellPrices
            }

            return prices[coll.lowercase()]?.get(gemstoneTier.name)
        }

        return when (multiCollectionTypes[coll]) {
            "normal" -> when (bazaarPriceType) {
                BazaarPriceType.INSTANT_BUY -> BazaarPrices.multiBaseInstantBuy[coll]
                BazaarPriceType.INSTANT_SELL -> BazaarPrices.multiBaseInstantSell[coll]
            }

            "enchanted" -> when (getMultiSummaryBazaarType(coll)) {
                BazaarType.ENCHANTED_VERSION -> when (bazaarPriceType) {
                    BazaarPriceType.INSTANT_BUY -> BazaarPrices.multiEnchantedInstantBuy[coll]
                    BazaarPriceType.INSTANT_SELL -> BazaarPrices.multiEnchantedInstantSell[coll]
                }

                BazaarType.SUPER_ENCHANTED_VERSION -> when (bazaarPriceType) {
                    BazaarPriceType.INSTANT_BUY -> BazaarPrices.multiSuperEnchantedInstantBuy[coll]
                    BazaarPriceType.INSTANT_SELL -> BazaarPrices.multiSuperEnchantedInstantSell[coll]
                }
            }

            else -> null
        }
    }

    private fun getMultiSummaryMoney(coll: String, shouldUseBazaar: Boolean): Pair<Long, Long> {
        val priceType = bazaarPriceType

        if (!shouldUseBazaar) {
            val key = if (GemstonesManager.checkIfGemstone(coll)) {
                "${coll}_${GemstoneTier.ROUGH}".uppercase()
            } else {
                coll
            }

            return Pair(
                MultiTrackingRates.moneyMadeNPC.getOrDefault(key, 0L),
                MultiTrackingRates.moneyRateNPC.getOrDefault(key, 0L)
            )
        }

        if (GemstonesManager.checkIfGemstone(coll)) {
            return Pair(
                MultiTrackingRates.gemstoneMoneyMade[coll]
                    ?.get(gemstoneTier)
                    ?.get(priceType) ?: 0L,
                MultiTrackingRates.gemstoneMoneyRate[coll]
                    ?.get(gemstoneTier)
                    ?.get(priceType) ?: 0L
            )
        }

        return when (multiCollectionTypes[coll]) {
            "normal" -> Pair(
                MultiTrackingRates.baseMoneyMade[coll]?.get(priceType) ?: 0L,
                MultiTrackingRates.baseMoneyRate[coll]?.get(priceType) ?: 0L
            )

            "enchanted" -> {
                val madeMap = when (getMultiSummaryBazaarType(coll)) {
                    BazaarType.ENCHANTED_VERSION -> MultiTrackingRates.enchantedMoneyMade[coll]
                    BazaarType.SUPER_ENCHANTED_VERSION -> MultiTrackingRates.superEnchantedMoneyMade[coll]
                }

                val rateMap = when (getMultiSummaryBazaarType(coll)) {
                    BazaarType.ENCHANTED_VERSION -> MultiTrackingRates.enchantedMoneyRate[coll]
                    BazaarType.SUPER_ENCHANTED_VERSION -> MultiTrackingRates.superEnchantedMoneyRate[coll]
                }

                Pair(madeMap?.get(priceType) ?: 0L, rateMap?.get(priceType) ?: 0L)
            }

            else -> Pair(0L, 0L)
        }
    }

    private fun getMultiSummaryCompactedCollection(amount: Long, coll: String): Pair<Double, String> {
        if (coll == "gemstone" || GemstonesManager.checkIfGemstone(coll)) {
            val tier = collectionGemstoneTier
            return Pair(amount / tier.recipeAmount.toDouble(), tier.displayName)
        }

        return when (getMultiSummaryCollectionTier(coll)) {
            CollectionTier.BASE -> Pair(amount.toDouble(), "")

            CollectionTier.ENCHANTED -> {
                val recipeAmount = multiEnchantedRecipes[coll]
                    ?.values
                    ?.firstOrNull()
                    ?.toLong()
                    ?: 1L

                Pair(amount / recipeAmount.toDouble(), CollectionTier.ENCHANTED.toString())
            }

            CollectionTier.SUPER_ENCHANTED -> {
                val recipeAmount = multiSuperEnchantedRecipes[coll]
                    ?.values
                    ?.firstOrNull()
                    ?.toLong()
                    ?: 1L

                Pair(amount / recipeAmount.toDouble(), CollectionTier.SUPER_ENCHANTED.toString())
            }
        }
    }

    private fun getMultiSummaryMoneyText(made: Long, rate: Long, useMotes: Boolean): String {
        return if (useMotes) {
            "${formatLong(made)} motes §7(${formatLong(rate)} motes/h)"
        } else {
            "$${formatLong(made)} §7($${formatLong(rate)}/h)"
        }
    }

    private fun addMultiSummaryItemPrice(lines: MutableList<Component>, coll: String) {
        val itemPrice = getMultiSummaryItemPrice(coll)

        val priceText = itemPrice
            ?.takeIf { it > 0f }
            ?.let { "$${formatLong(it.toLong())}" }
            ?: "Unknown price"

        val itemName = getMultiSummaryItemName(coll)

        lines.add(Component.literal("        §6Item price: §f$priceText (§7$itemName§f)"))
    }

    private fun getMultiSummaryItemName(coll: String): String? {
        if (GemstonesManager.checkIfGemstone(coll)) {
            return "${gemstoneTier.displayName} ${formatCollectionName(coll)}"
        }

        return when (multiCollectionTypes[coll]) {
            "normal" -> formatCollectionName(coll)

            "enchanted" -> {
                val recipes = when (getMultiSummaryBazaarType(coll)) {
                    BazaarType.ENCHANTED_VERSION -> multiEnchantedRecipes
                    BazaarType.SUPER_ENCHANTED_VERSION -> multiSuperEnchantedRecipes
                }

                recipes[coll]?.keys?.firstOrNull()?.let { formatBazaarItemName(it) }
            }

            else -> null
        }
    }

    private fun getMultiSummaryBazaarType(coll: String): BazaarType {
        return if (bazaarType == BazaarType.SUPER_ENCHANTED_VERSION && multiSuperEnchantedRecipes[coll].isNullOrEmpty()) {
            BazaarType.ENCHANTED_VERSION
        } else bazaarType
    }

    private fun getMultiSummaryCollectionTier(coll: String): CollectionTier {
        return if (collectionTier == CollectionTier.SUPER_ENCHANTED && multiSuperEnchantedRecipes[coll].isNullOrEmpty()) {
            CollectionTier.ENCHANTED
        } else collectionTier
    }
}