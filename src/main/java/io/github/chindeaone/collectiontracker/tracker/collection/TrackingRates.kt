package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getNextRankEntry
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPlayerRank
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPreviousRankEntry
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

object TrackingRates {
    val logger: Logger = LogManager.getLogger(TrackingRates::class.java)

    // Collection tracking data
    @Volatile var collectionAmount: Long = 0
    @Volatile var collectionRate: Long = 0
    @Volatile var collectionMade: Long = 0
    @Volatile var collectionSinceLast: Long = 0
    @Volatile var sessionStartCollection: Long = -1L
    @Volatile var lastCollectionUpdate: EpochMark? = null

    // Sacks tracking data
    @Volatile var lastApiCollection: Long = -1L
    @Volatile var sacksCollectionGained: Long = 0L

    // NPC Money Tracking
    @Volatile var moneyMadeNPC: Long = 0L
    @Volatile var moneyRateNPC: Long = 0

    // Bazaar Money Tracking (per tier and per price type)
    val baseMoneyMade = ConcurrentHashMap<BazaarPriceType, Long>()
    val baseMoneyRate = ConcurrentHashMap<BazaarPriceType, Long>()

    val enchantedMoneyMade = ConcurrentHashMap<BazaarPriceType, Long>()
    val enchantedMoneyRate = ConcurrentHashMap<BazaarPriceType, Long>()

    val superEnchantedMoneyMade = ConcurrentHashMap<BazaarPriceType, Long>()
    val superEnchantedMoneyRate = ConcurrentHashMap<BazaarPriceType, Long>()

    val gemstoneMoneyMade = ConcurrentHashMap<GemstoneTier, ConcurrentHashMap<BazaarPriceType, Long>>()
    val gemstoneMoneyRate = ConcurrentHashMap<GemstoneTier, ConcurrentHashMap<BazaarPriceType, Long>>()

    // Leaderboard tracking data
    @Volatile var playerCurrentRank: Int = -1
    @Volatile var nextRankUsername: String? = null
    @Volatile var nextRankAmount: Long = -1L
    @Volatile var etaToNextRank: String? = null
    @Volatile var collectionTillNextRank: Long = -1L
    @Volatile var isNextWiped: Boolean = false
    @Volatile var previousRankUsername: String? = null
    @Volatile var previousRankAmount: Long = -1L
    @Volatile var collectionAbovePreviousRankAmount: Long = -1L
    @Volatile var isPreviousWiped: Boolean = false

    fun setCollection(value: Long) {
        lastApiCollection = value
        if (sessionStartCollection == -1L) {
            sessionStartCollection = value
            TrackingHandler.initTracking()
        }
        collectionAmount = value
        lastCollectionUpdate = EpochMark.now()

        updateValues(collectionAmount, 0)
    }

    fun calculateRates(value: Long) {
        // 'value' here is what you gained from sacks since last check
        sacksCollectionGained += value // update sacks gained
        val currentCollection = lastApiCollection + sacksCollectionGained // increase current collection
        updateValues(currentCollection, value)
    }

    fun updateCollection(value: Long) {
        // 'value' here is the current collection amount fetched from the API
        val gainedSinceLast = value - lastApiCollection
        lastApiCollection = value // update last API collection
        updateValues(value, gainedSinceLast)
    }

    fun updateRates() {
        if (TrackingHandler.isPaused) return
        val currentCollection = lastApiCollection + sacksCollectionGained
        updateValues(currentCollection, 0)
    }

    fun updateLeaderboardStats() {
        playerCurrentRank = getPlayerRank()

        val nextEntry = getNextRankEntry()
        if (nextEntry != null) {
            nextRankUsername = nextEntry.username
            nextRankAmount = nextEntry.amount
            collectionTillNextRank = nextRankAmount - collectionAmount
            isNextWiped = nextEntry.wiped

            if (collectionRate > 0) {
                val seconds = (collectionTillNextRank / (collectionRate / 3600.0)).toLong()
                etaToNextRank = StringUtils.formatCompactTime(seconds)
            } else {
                etaToNextRank = null
            }
        } else {
            nextRankUsername = null
            nextRankAmount = -1L
            collectionTillNextRank = -1L
            etaToNextRank = null
            isNextWiped = false
        }

        val previousEntry = getPreviousRankEntry()
        if (previousEntry != null) {
            previousRankUsername = previousEntry.username
            previousRankAmount = previousEntry.amount
            collectionAbovePreviousRankAmount = collectionAmount - previousRankAmount
            isPreviousWiped = previousEntry.wiped
        } else {
            previousRankUsername = null
            previousRankAmount = -1L
            collectionAbovePreviousRankAmount = -1L
            isPreviousWiped = false
        }
    }

    private fun updateValues(currentCollection: Long, collectionSinceLastVal: Long) {
        if (collectionSinceLastVal != 0L) collectionSinceLast = collectionSinceLastVal

        if (collectionSinceLastVal > 0) {
            lastCollectionUpdate = EpochMark.now()

            if (!riftTracking) {
                logger.info("[SCT]: Current collection for $collection (using sacks) is $currentCollection")
                logger.info("[SCT]: Change in collection detected (using sacks). Old collection: ${currentCollection - collectionSinceLastVal}. New collection: $currentCollection")

                logger.info("[SCT]: Collection since last check is $collectionSinceLast.")
            }
        }

        val uptime = TrackingHandler.uptimeInSeconds
        val collectedSinceStart = currentCollection - sessionStartCollection

        val npcPrice = NpcPrices.getNpcPrice(collection)
        moneyMadeNPC = if (uptime > 0) floor(npcPrice * collectedSinceStart.toDouble()).toLong() else 0L
        moneyRateNPC = if (uptime > 0) floor(npcPrice * collectedSinceStart / (uptime / 3600.0)).toLong() else 0

        if (BazaarCollectionsManager.hasBazaarData) {
            updateBazaarMaps(collectedSinceStart, uptime)
        }

        // Update values
        collectionAmount = currentCollection
        collectionRate = if (uptime > 0) floor(collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
        collectionMade = collectedSinceStart

        // Trigger tracking overlay update
        if (!CollectionOverlay.trackingDirty) {
            if (!isTrackingDataReady) {
                sendMessage("§cWarning! Some maps have not been fully initialized. You have the option to restart the tracker or wait for the next collection update.")
            }
            CollectionOverlay.trackingDirty = true
        }

        if (collectionLeaderboard) updateLeaderboardStats()
    }

    private fun updateBazaarMaps(collectedSinceStart: Long, uptime: Long) {
        if (uptime <= 0) return

        when (CollectionsManager.collectionType) {
            "normal" -> {
                for (priceType in BazaarPriceType.entries) {
                    val price =
                        if (priceType == BazaarPriceType.INSTANT_BUY) BazaarPrices.baseInstantBuy else BazaarPrices.baseInstantSell
                    val hourly = floor(price * (collectedSinceStart.toDouble() / 160) / (uptime / 3600.0)).toLong()
                    val made = floor((price * collectedSinceStart).toDouble()).toLong()

                    baseMoneyRate[priceType] = hourly
                    baseMoneyMade[priceType] = made
                }
            }

            "enchanted" -> {
                val enchDivisor = BazaarCollectionsManager.enchantedRecipe.values.firstOrNull()?.toDouble() ?: 1.0
                for (priceType in BazaarPriceType.entries) {
                    val price = if (priceType == BazaarPriceType.INSTANT_BUY) BazaarPrices.enchantedInstantBuy else BazaarPrices.enchantedInstantSell
                    val hourly = floor(price * (collectedSinceStart.toDouble() / enchDivisor) / (uptime / 3600.0)).toLong()
                    val made = floor(price * (collectedSinceStart.toDouble() / enchDivisor)).toLong()

                    enchantedMoneyRate[priceType] = hourly
                    enchantedMoneyMade[priceType] = made
                }

                if (BazaarPrices.superEnchantedInstantBuy != 0.0f) {
                    val superDivisor = BazaarCollectionsManager.superEnchantedRecipe.values.firstOrNull()?.toDouble() ?: 1.0
                    for (priceType in BazaarPriceType.entries) {
                        val price = if (priceType == BazaarPriceType.INSTANT_BUY) BazaarPrices.superEnchantedInstantBuy else BazaarPrices.superEnchantedInstantSell
                        val hourly = floor(price * (collectedSinceStart.toDouble() / superDivisor) / (uptime / 3600.0)).toLong()
                        val made = floor(price * (collectedSinceStart.toDouble() / superDivisor)).toLong()

                        superEnchantedMoneyRate[priceType] = hourly
                        superEnchantedMoneyMade[priceType] = made
                    }
                }
            }

            "gemstone" -> {
                for (tier in GemstoneTier.entries) {
                    val divisor = tier.recipeAmount.toDouble()
                    val buyMap = gemstoneMoneyRate.computeIfAbsent(tier) { ConcurrentHashMap() }
                    val madeBuyMap = gemstoneMoneyMade.computeIfAbsent(tier) { ConcurrentHashMap() }

                    val buyPrice = GemstonePrices.getInstantBuyPrice(tier)
                    val sellPrice = GemstonePrices.getInstantSellPrice(tier)

                    buyMap[BazaarPriceType.INSTANT_BUY] = floor(buyPrice * (collectedSinceStart.toDouble() / divisor) / (uptime / 3600.0)).toLong()
                    buyMap[BazaarPriceType.INSTANT_SELL] = floor(sellPrice * (collectedSinceStart.toDouble() / divisor) / (uptime / 3600.0)).toLong()

                    madeBuyMap[BazaarPriceType.INSTANT_BUY] = floor(buyPrice * (collectedSinceStart.toDouble() / divisor)).toLong()
                    madeBuyMap[BazaarPriceType.INSTANT_SELL] = floor(sellPrice * (collectedSinceStart.toDouble() / divisor)).toLong()
                }
            }
        }
    }

    private val isTrackingDataReady: Boolean
        get() {
            val npcReady = NpcPrices.getNpcPrice(collection) > 0
            if (npcReady) return true

            if (!BazaarCollectionsManager.hasBazaarData) return true

            return when (CollectionsManager.collectionType) {
                "normal" -> baseMoneyRate.isNotEmpty()
                "enchanted" -> enchantedMoneyRate.isNotEmpty()
                "gemstone" -> gemstoneMoneyRate.isNotEmpty()
                else -> true
            }
        }

    fun clearCollectionData() {
        collectionAmount = 0L
        collectionRate = 0L
        collectionMade = 0L
        collectionSinceLast = 0L
        sessionStartCollection = -1L
        lastCollectionUpdate = null
        lastApiCollection = -1L
        sacksCollectionGained = 0L
    }

    fun clearAllMoneyMaps() {
        moneyMadeNPC = 0L
        moneyRateNPC = 0L
        baseMoneyMade.clear()
        baseMoneyRate.clear()
        enchantedMoneyMade.clear()
        enchantedMoneyRate.clear()
        superEnchantedMoneyMade.clear()
        superEnchantedMoneyRate.clear()
        gemstoneMoneyMade.clear()
        gemstoneMoneyRate.clear()
    }

    fun clearLeaderboardData() {
        playerCurrentRank = -1
        nextRankUsername = null
        nextRankAmount = -1L
        etaToNextRank = null
        collectionTillNextRank = -1L
        isNextWiped = false
        previousRankUsername = null
        previousRankAmount = -1L
        collectionAbovePreviousRankAmount = -1L
        isPreviousWiped = false
    }
}
