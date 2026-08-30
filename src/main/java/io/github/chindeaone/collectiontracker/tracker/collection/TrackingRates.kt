package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isCollectionLeaderboardEnabled
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getNextRankEntry
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPlayerRank
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPreviousRankEntry
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatETA
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.concurrent.Volatile
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object TrackingRates {
    val logger: Logger = LogManager.getLogger(TrackingRates::class.java)

    // Collection tracking data
    @JvmField
    @Volatile
    var collectionAmount: Long = 0

    @JvmField
    @Volatile
    var collectionPerHour: Long = 0

    @JvmField
    @Volatile
    var collectionMade: Long = 0

    @JvmField
    @Volatile
    var collectionSinceLast: Long = 0

    @Volatile
    var sessionStartCollection: Long = -1L

    @JvmField
    @Volatile
    var lastCollectionTime: Long = -1L

    // Sacks tracking data
    @Volatile
    var lastApiCollection: Long = -1L

    @Volatile
    var sacksCollectionGained: Long = 0L

    // Highest and lowest rates
    @Volatile
    var highestCollectionPerHour: Long = 0

    @Volatile
    var lowestCollectionPerHour: Long = Long.MAX_VALUE

    // Money tracking data
    // NPC
    @Volatile
    @JvmField
    var moneyPerHourNPC: Long = 0

    // Highest and lowest rates
    @Volatile
    var highestRatePerHourNPC: Long = 0

    @Volatile
    var lowestRatePerHourNPC: Long = Long.MAX_VALUE

    // Bazaar
    @JvmField
    @Volatile
    var moneyMade: MutableMap<String, Long> = mutableMapOf()

    @Volatile
    @JvmField
    var moneyPerHourBazaar: MutableMap<String, Long> = mutableMapOf()

    // Highest and lowest rates
    @Volatile
    var lowestRatesPerHourBazaar: MutableMap<String, Long> = mutableMapOf()

    @Volatile
    var highestRatesPerHourBazaar: MutableMap<String, Long> = mutableMapOf()

    // Leaderboard tracking data
    @JvmField
    @Volatile
    var playerCurrentRank: Int = -1

    @JvmField
    @Volatile
    var nextRankUsername: String? = null

    @JvmField
    @Volatile
    var nextRankAmount: Long = -1L

    @JvmField
    @Volatile
    var etaToNextRank: String? = null

    @JvmField
    @Volatile
    var collectionTillNextRank: Long = -1L

    @JvmField
    @Volatile
    var isNextWiped: Boolean = false

    @JvmField
    @Volatile
    var previousRankUsername: String? = null

    @JvmField
    @Volatile
    var previousRankAmount: Long = -1L

    @JvmField
    @Volatile
    var collectionAbovePreviousRankAmount: Long = -1L

    @JvmField
    @Volatile
    var isPreviousWiped: Boolean = false

    fun setCollection(value: Long) {
        val now = System.currentTimeMillis()
        lastApiCollection = value
        if (sessionStartCollection == -1L) {
            sessionStartCollection = value
            TrackingHandler.initTracking(now)
        }
        collectionAmount = value
        lastCollectionTime = now

        updateValues(collectionAmount, 0)
    }

    @Synchronized
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

    fun updateLeaderboardStats() {
        playerCurrentRank = getPlayerRank()

        val nextEntry = getNextRankEntry()
        if (nextEntry != null) {
            nextRankUsername = nextEntry.username
            nextRankAmount = nextEntry.amount
            collectionTillNextRank = nextRankAmount - collectionAmount
            isNextWiped = nextEntry.wiped

            if (collectionPerHour > 0) {
                val seconds = (collectionTillNextRank / (collectionPerHour / 3600.0)).toLong()
                etaToNextRank = formatETA(seconds)
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
        collectionSinceLast = collectionSinceLastVal

        if (collectionSinceLastVal > 0) {
            logger.info("[SCT]: Current collection for '{}' (using sacks) is {}", collection, currentCollection)
            logger.info(
                "[SCT]: Change in collection detected (using sacks). Old collection: '{}'. New collection: '{}'",
                currentCollection - collectionSinceLastVal,
                currentCollection
            )
            lastCollectionTime = System.currentTimeMillis()
            logger.info("[SCT]: Collection since last check is {}.", collectionSinceLast)
        }

        val uptime = TrackingHandler.uptimeInSeconds
        val collectedSinceStart = currentCollection - sessionStartCollection

        val priceNPC = NpcPrices.getNpcPrice(collection)
        moneyMade["NPC"] = if (uptime > 0) floor(priceNPC * collectedSinceStart.toDouble()).toLong() else 0L

        if (BazaarCollectionsManager.hasBazaarData) {
            updateBazaarMaps(collectedSinceStart, uptime)
        }

        // Update values
        collectionAmount = currentCollection
        collectionPerHour = if (uptime > 0) floor(collectedSinceStart / (uptime / 3600.0)).toLong() else 0
        collectionMade = collectedSinceStart
        moneyPerHourNPC = if (uptime > 0) floor(priceNPC * collectedSinceStart / (uptime / 3600.0)).toLong() else 0

        // Update highest and lowest rates
        if (collectionPerHour > highestCollectionPerHour && collectionPerHour > 0) {
            highestCollectionPerHour = collectionPerHour
        }
        if (collectionPerHour in 1..<lowestCollectionPerHour) {
            lowestCollectionPerHour = collectionPerHour
        }

        if (moneyPerHourNPC > highestRatePerHourNPC && moneyPerHourNPC > 0) {
            highestRatePerHourNPC = moneyPerHourNPC
        }
        if (moneyPerHourNPC in 1..<lowestRatePerHourNPC) {
            lowestRatePerHourNPC = moneyPerHourNPC
        }

        fillBazaarExtremesFromCurrent() // Ensure extremes are initialized

        // Trigger tracking overlay update
        if (!CollectionOverlay.trackingDirty) {
            if (!isTrackingDataReady) sendMessage(
                "§cWarning! Some maps have not been fully initialized. You have the option to restart the tracker or wait for the next collection update.",
                true
            )
            CollectionOverlay.trackingDirty = true
        }

        if (isCollectionLeaderboardEnabled()) updateLeaderboardStats()
    }

    private fun updateBazaarMaps(collectedSinceStart: Long, uptime: Long) {
        when (CollectionsManager.collectionType) {
            "normal" -> {
                // Instant Buy
                val buyComputed =
                    if (uptime > 0) floor(BazaarPrices.normalInstantBuy * (collectedSinceStart.toDouble() / 160) / (uptime / 3600.0)).toLong() else 0
                moneyPerHourBazaar[CollectionsManager.collectionType + "_INSTANT_BUY"] = buyComputed
                updateBazaarExtremes(CollectionsManager.collectionType + "_INSTANT_BUY", buyComputed)
                moneyMade[CollectionsManager.collectionType + "_INSTANT_BUY"] = if (uptime > 0) floor((BazaarPrices.normalInstantBuy * collectedSinceStart).toDouble()).toLong() else 0

                // Instant Sell
                val sellComputed =
                    if (uptime > 0) floor(BazaarPrices.normalInstantSell * (collectedSinceStart.toDouble() / 160) / (uptime / 3600.0)).toLong() else 0
                moneyPerHourBazaar[CollectionsManager.collectionType + "_INSTANT_SELL"] = sellComputed
                updateBazaarExtremes(CollectionsManager.collectionType + "_INSTANT_SELL", sellComputed)
                moneyMade[CollectionsManager.collectionType + "_INSTANT_SELL"] = if (uptime > 0) floor((BazaarPrices.normalInstantSell * collectedSinceStart).toDouble()).toLong() else 0
            }

            "enchanted" -> {
                val enchantedDivisor =
                    if (BazaarCollectionsManager.enchantedRecipe.isEmpty()) 1.0 else BazaarCollectionsManager.enchantedRecipe.values.iterator()
                        .next().toDouble()
                // Enchanted version - Buy
                val enchantedBuyComputed =
                    if (uptime > 0) floor(BazaarPrices.enchantedInstantBuy * (collectedSinceStart.toDouble() / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0
                moneyPerHourBazaar["Enchanted version_INSTANT_BUY"] = enchantedBuyComputed
                updateBazaarExtremes("Enchanted version_INSTANT_BUY", enchantedBuyComputed)
                moneyMade["Enchanted version_INSTANT_BUY"] = if (uptime > 0) floor(BazaarPrices.enchantedInstantBuy * (collectedSinceStart.toDouble() / enchantedDivisor)).toLong() else 0

                // Enchanted version - Sell
                val enchantedSellComputed =
                    if (uptime > 0) floor(BazaarPrices.enchantedInstantSell * (collectedSinceStart.toDouble() / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0
                moneyPerHourBazaar["Enchanted version_INSTANT_SELL"] = enchantedSellComputed
                updateBazaarExtremes("Enchanted version_INSTANT_SELL", enchantedSellComputed)
                moneyMade["Enchanted version_INSTANT_SELL"] = if (uptime > 0) floor(BazaarPrices.enchantedInstantSell * (collectedSinceStart.toDouble() / enchantedDivisor)).toLong() else 0

                // Super Enchanted version
                if (BazaarPrices.superEnchantedInstantBuy != 0.0f) {
                    val superDivisor =
                        if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) 1.0 else BazaarCollectionsManager.superEnchantedRecipe.values.iterator()
                            .next().toDouble()
                    // Buy
                    val superBuyComputed =
                        if (uptime > 0) floor(BazaarPrices.superEnchantedInstantBuy * (collectedSinceStart.toDouble() / superDivisor) / (uptime / 3600.0)).toLong() else 0
                    moneyPerHourBazaar["Super Enchanted version_INSTANT_BUY"] = superBuyComputed
                    updateBazaarExtremes("Super Enchanted version_INSTANT_BUY", superBuyComputed)
                    moneyMade["Super Enchanted version_INSTANT_BUY"] = if (uptime > 0) floor(BazaarPrices.superEnchantedInstantBuy * (collectedSinceStart.toDouble() / superDivisor)).toLong() else 0

                    // Sell
                    val superSellComputed =
                        if (uptime > 0) floor(BazaarPrices.superEnchantedInstantSell * (collectedSinceStart.toDouble() / superDivisor) / (uptime / 3600.0)).toLong() else 0
                    moneyPerHourBazaar["Super Enchanted version_INSTANT_SELL"] = superSellComputed
                    updateBazaarExtremes("Super Enchanted version_INSTANT_SELL", superSellComputed)
                    moneyMade["Super Enchanted version_INSTANT_SELL"] = if (uptime > 0) floor(BazaarPrices.superEnchantedInstantSell * (collectedSinceStart.toDouble() / superDivisor)).toLong() else 0
                }
            }

            "gemstone" -> {
                for (key in GemstonePrices.gemstoneInstantSellPrices.keys) {
                    // Buy
                    val buyPrice = GemstonePrices.getInstantBuyPrice(key)
                    val buyComputed = if (uptime > 0) floor(
                        buyPrice * (collectedSinceStart.toDouble() / GemstonePrices.recipes[key]!!) / (uptime / 3600.0)
                    ).toLong() else 0
                    moneyPerHourBazaar[key + "_INSTANT_BUY"] = buyComputed
                    updateBazaarExtremes(key + "_INSTANT_BUY", buyComputed)
                    moneyMade[key + "_INSTANT_BUY"] = if (uptime > 0) floor(
                        buyPrice * (collectedSinceStart.toDouble() / GemstonePrices.recipes[key]!!)
                    ).toLong() else 0

                    // Sell
                    val sellPrice = GemstonePrices.getInstantSellPrice(key)
                    val sellComputed = if (uptime > 0) floor(
                        sellPrice * (collectedSinceStart.toDouble() / GemstonePrices.recipes[key]!!) / (uptime / 3600.0)
                    ).toLong() else 0
                    moneyPerHourBazaar[key + "_INSTANT_SELL"] = sellComputed
                    updateBazaarExtremes(key + "_INSTANT_SELL", sellComputed)
                    moneyMade[key + "_INSTANT_SELL"] = if (uptime > 0) floor(
                        sellPrice * (collectedSinceStart.toDouble() / GemstonePrices.recipes[key]!!)
                    ).toLong() else 0
                }
            }
        }
    }


    private fun fillBazaarExtremesFromCurrent() {
        // Only initialize if both extreme maps are empty and there's data to copy
        if (!moneyPerHourBazaar.isEmpty() && lowestRatesPerHourBazaar.isEmpty() && highestRatesPerHourBazaar.isEmpty()) {
            for (e in moneyPerHourBazaar.entries) {
                val key = e.key
                val `val`: Long = e.value
                // skip unwanted values
                if (`val` <= 0L) continue
                lowestRatesPerHourBazaar.putIfAbsent(key, `val`)
                highestRatesPerHourBazaar.putIfAbsent(key, `val`)
            }
        }
    }

    private fun updateBazaarExtremes(key: String?, value: Long) {
        if (key == null || value <= 0L) return

        lowestRatesPerHourBazaar.compute(key) { `_`: String?, old: Long? ->
            if (old == null) value else min(
                old,
                value
            )
        }

        highestRatesPerHourBazaar.compute(key) { `_`: String?, old: Long? ->
            if (old == null) value else max(
                old,
                value
            )
        }
    }

    private val isTrackingDataReady: Boolean
        get() {
            if (moneyMade.isEmpty()) return false

            // NPC data should always be present
            if (!moneyMade.containsKey("NPC")) return false

            // If Bazaar enabled, make sure at least one entry is present
            return !BazaarCollectionsManager.hasBazaarData || !moneyPerHourBazaar.isEmpty()
        }
}
