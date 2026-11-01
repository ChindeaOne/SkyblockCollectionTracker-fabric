package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.CollectionTier
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarType
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.scheduler
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.trackingTask
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.collectionConfig
import io.github.chindeaone.collectiontracker.config.collectionGemstoneTier
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.collectionTier
import io.github.chindeaone.collectiontracker.config.gemstoneTier
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager.setTrackingOverlayRendering
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.gui.overlays.saveMilestonesProgress
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.clearAllCache
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.clearCollectionCache
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher.fetchData
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionMade
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.collectionRate
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.moneyRateNPC
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.moneyMadeNPC
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatInWords
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatDouble
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatLong
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatBazaarItemName
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener
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

        TrackingRates.clearCollectionData()
        TrackingRates.clearAllMoneyMaps()
        TrackingRates.clearLeaderboardData()

        riftTracking = false
        InventoryListener.clearInventoryState()

        collection = ""
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

        if (!riftTracking) {
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

        if (!riftTracking) {
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
        val collectionDisplay = StringUtils.formatCollectionName(collection)
        val lines: MutableList<Component> = mutableListOf()

        lines.add(Component.literal("   §aCollection tracked: §f").append(collectionDisplay.toColor()))

        val convertedMade = compactCollection(collectionMade)
        val convertedRate= compactCollection(collectionRate)

        val collectionTierName = when (collectionType) {
            "gemstone" -> collectionGemstoneTier.displayName.takeUnless { collectionGemstoneTier == GemstoneTier.ROUGH }

            "enchanted" -> {
                val effectiveTier = getEffectiveCollectionTier()
                effectiveTier.takeUnless { it == CollectionTier.BASE }?.shortName
            }

            else -> null
        }

        val collectionLabel = collectionTierName?.let { "Collection Made ($it)" } ?: "Collection Made"

        lines.add(Component.literal("   §b$collectionLabel: §f${formatDouble(convertedMade)}   " + "§bRate: §f${formatDouble(convertedRate)}/h"))

        if (!useBazaar) {
            val npcMoney = moneyMadeNPC

            if (NpcPrices.getNpcPrice(collection) != 0) {
                if (riftTracking) {
                    lines.add(Component.literal("   §6Motes Made: §f$${formatLong(npcMoney)}   " + "§6Rate: §f$${formatLong(moneyRateNPC)}/h"))
                } else {
                    lines.add(Component.literal("   §6Money Made (NPC): §f$${formatLong(npcMoney)}   " + "§6Rate: §f$${formatLong(moneyRateNPC)}/h"))
                }
            }
        } else {
            val priceType = bazaarPriceType

            when (collectionType) {
                "normal" -> {
                    val bazMoney = TrackingRates.baseMoneyMade[priceType] ?: 0L
                    val bazRate = TrackingRates.baseMoneyRate[priceType] ?: 0L

                    lines.add(Component.literal("   §6Money Made (Bazaar): §f$${formatLong(bazMoney)}   " + "§6Rate: §f$${formatLong(bazRate)}/h"))
                }

                "enchanted" -> {
                    val effectiveBazaarType = getEffectiveBazaarType()

                    val bazMoney = if (effectiveBazaarType == BazaarType.ENCHANTED_VERSION) {
                        TrackingRates.enchantedMoneyMade[priceType] ?: 0L
                    } else {
                        TrackingRates.superEnchantedMoneyMade[priceType] ?: 0L
                    }

                    val bazRate = if (effectiveBazaarType == BazaarType.ENCHANTED_VERSION) {
                        TrackingRates.enchantedMoneyRate[priceType] ?: 0L
                    } else {
                        TrackingRates.superEnchantedMoneyRate[priceType] ?: 0L
                    }

                    lines.add(Component.literal("   §6Money Made (Bazaar - ${effectiveBazaarType.shortName}): §f$${formatLong(bazMoney)}   " + "§6Rate: §f$${formatLong(bazRate)}/h"))
                }

                "gemstone" -> {
                    val tier = gemstoneTier

                    val gMoney = TrackingRates.gemstoneMoneyMade[tier]?.get(priceType) ?: 0L
                    val gRate = TrackingRates.gemstoneMoneyRate[tier]?.get(priceType) ?: 0L

                    lines.add(Component.literal("   §6Money Made (${tier.displayName}): §f$${formatLong(gMoney)}   " + "§6Rate: §f$${formatLong(gRate)}/h"))
                }
            }

            getBazaarItemName(collectionDisplay)?.let { itemName ->
                lines.add(Component.empty())
                lines.add(Component.literal("   §6Pricing based on: §f$itemName §7($priceType)"))

                val itemPrice = getBazaarItemPrice()

                lines.add(
                    Component.literal("   §6Item price: §f${itemPrice?.let {
                        if (it == 0f) "Unknown price"
                        else "$${formatLong(it.toLong())}" 
                    } ?: "Unknown price"}")
                )
            }
        }

        lines.add(Component.empty())
        lines.add(Component.literal("   §7Elapsed time: §f$uptimeInWords"))

        ChatUtils.sendSummary("§e§lTracking Summary", lines)
    }

    private fun getEffectiveBazaarType(): BazaarType {
        return if (bazaarType == BazaarType.SUPER_ENCHANTED_VERSION && BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
            BazaarType.ENCHANTED_VERSION
        } else {
            bazaarType
        }
    }

    private fun getEffectiveCollectionTier(): CollectionTier {
        return if (collectionTier == CollectionTier.SUPER_ENCHANTED && BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
            CollectionTier.ENCHANTED
        } else {
            collectionTier
        }
    }

    private fun compactCollection(amount: Long): Double {
        return when (collectionType) {
            "gemstone" -> {
                val divisor = collectionGemstoneTier.recipeAmount.toDouble()
                amount / divisor
            }

            "enchanted" -> {
                when (getEffectiveCollectionTier()) {
                    CollectionTier.BASE -> amount.toDouble()

                    CollectionTier.ENCHANTED -> {
                        val craftCost = BazaarCollectionsManager.enchantedRecipe.values.firstOrNull()?.toDouble() ?: 160.0
                        amount / craftCost
                    }

                    CollectionTier.SUPER_ENCHANTED -> {
                        val craftCost = BazaarCollectionsManager.superEnchantedRecipe.values.firstOrNull()?.toDouble() ?: 25600.0
                        amount / craftCost
                    }
                }
            }

            else -> amount.toDouble()
        }
    }

    private fun getBazaarItemName(collectionDisplay: String): String? {
        return when (collectionType) {
            "enchanted" -> {
                val recipe = when (getEffectiveBazaarType()) {
                    BazaarType.ENCHANTED_VERSION -> BazaarCollectionsManager.enchantedRecipe
                    BazaarType.SUPER_ENCHANTED_VERSION -> BazaarCollectionsManager.superEnchantedRecipe
                }

                recipe.keys.firstOrNull()?.let { formatBazaarItemName(it) }
            }

            "gemstone" -> "${gemstoneTier.displayName} $collectionDisplay"

            else -> null
        }
    }

    private fun getBazaarItemPrice(): Float? {
        return when (collectionType) {
            "enchanted" -> {
                when (getEffectiveBazaarType()) {
                    BazaarType.ENCHANTED_VERSION -> {
                        if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) {
                            BazaarPrices.enchantedInstantBuy
                        } else {
                            BazaarPrices.enchantedInstantSell
                        }
                    }

                    BazaarType.SUPER_ENCHANTED_VERSION -> {
                        if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) {
                            BazaarPrices.superEnchantedInstantBuy
                        } else {
                            BazaarPrices.superEnchantedInstantSell
                        }
                    }
                }
            }

            "gemstone" -> {
                if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) {
                    GemstonePrices.getInstantBuyPrice(gemstoneTier)
                } else {
                    GemstonePrices.getInstantSellPrice(gemstoneTier)
                }
            }

            else -> null
        }
    }
}
