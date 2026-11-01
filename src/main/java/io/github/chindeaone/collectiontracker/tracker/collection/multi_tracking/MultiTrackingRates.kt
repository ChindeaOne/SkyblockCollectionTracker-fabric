package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.StringUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

object MultiTrackingRates {

    // Collection tracking data
    val collectionAmounts = ConcurrentHashMap<String, Long>()
    val collectionRate = ConcurrentHashMap<String, Long>()
    val collectionMade = ConcurrentHashMap<String, Long>()
    val sessionStartCollections = ConcurrentHashMap<String, Long>()
    val lastApiCollections = ConcurrentHashMap<String, Long>()
    @Volatile var lastMultiCollectionUpdate: EpochMark? = null

    // Track seen gemstones to only render them if they've been received from chat
    val seenGemstones: MutableSet<String> = ConcurrentHashMap.newKeySet()

    // NPC Money Tracking
    val moneyMadeNPC = ConcurrentHashMap<String, Long>()
    val moneyRateNPC = ConcurrentHashMap<String, Long>()

    // Bazaar Money Tracking (per tier and per price type)
    val baseMoneyMade = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()
    val baseMoneyRate = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()

    val enchantedMoneyMade = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()
    val enchantedMoneyRate = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()

    val superEnchantedMoneyMade = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()
    val superEnchantedMoneyRate = ConcurrentHashMap<String, ConcurrentHashMap<BazaarPriceType, Long>>()

    val gemstoneMoneyMade = ConcurrentHashMap<String, ConcurrentHashMap<GemstoneTier, ConcurrentHashMap<BazaarPriceType, Long>>>()
    val gemstoneMoneyRate = ConcurrentHashMap<String, ConcurrentHashMap<GemstoneTier, ConcurrentHashMap<BazaarPriceType, Long>>>()

    // Leaderboard tracking data
    @Volatile var playerCurrentRank = -1
    @Volatile var nextRankUsername: String? = null
    @Volatile var nextRankAmount = -1L
    @Volatile var etaToNextRank: String? = null
    @Volatile var collectionTillNextRank = -1L
    @Volatile var isNextWiped = false
    @Volatile var previousRankUsername: String? = null
    @Volatile var previousRankAmount = -1L
    @Volatile var collectionAbovePreviousRankAmount = -1L
    @Volatile var isPreviousWiped = false

    fun setCollections(values: Map<String, Long>) {
        MultiTrackingHandler.initMultiTracking()

        for ((coll, value) in values) {
            lastApiCollections[coll] = value
            lastMultiCollectionUpdate = EpochMark.now()
            if (sessionStartCollections.getOrDefault(coll, -1L) == -1L) {
                sessionStartCollections[coll] = value

                // Initialize NPC money maps for gemstones
                if (coll == "gemstone") {
                    GemstonesManager.gemstones.forEach { gemstoneType ->
                        GemstoneTier.entries.forEach { tier ->
                            val keyPrefix = ("${gemstoneType}_${tier.name}").uppercase()
                            moneyRateNPC.putIfAbsent(keyPrefix, 0L)
                            moneyMadeNPC.putIfAbsent(keyPrefix, 0L)
                        }
                    }
                }
            }
            collectionAmounts[coll] = value
            updateValues(coll, value, 0L)
        }
    }

    fun updateCollections(values: Map<String, Long>) {
        for ((coll, newTotal) in values) {
            val previousTotal = lastApiCollections.getOrDefault(coll, newTotal)
            val gained = (newTotal - previousTotal).coerceAtLeast(0L)

            lastApiCollections[coll] = newTotal

            if (sessionStartCollections.getOrDefault(coll, -1L) == -1L) sessionStartCollections[coll] = newTotal

            updateValues(coll, newTotal, gained)
        }
    }

    fun calculateMultiRates(gains: Map<String, Long>) {
        for ((coll, amount) in gains) {
            val isGemstone = GemstonesManager.checkIfGemstone(coll)
            if (isGemstone) seenGemstones.add(coll)

            val startValue = sessionStartCollections[coll] ?: -1L
            if (startValue == -1L) sessionStartCollections[coll] = (collectionAmounts[coll] ?: lastApiCollections.getOrDefault(coll, 0L))

            val currentTotal = (collectionAmounts[coll] ?: lastApiCollections.getOrDefault(coll, 0L)) + amount
            updateValues(coll, currentTotal, amount)
        }
    }

    fun updateRates() {
        for (coll in collectionAmounts.keys) {
            val currentTotal = collectionAmounts[coll] ?: lastApiCollections.getOrDefault(coll, 0L)
            updateValues(coll, currentTotal, 0)
        }
    }

    private fun updateValues(coll: String, currentCollection: Long, sinceLast: Long) {
        val uptime = MultiTrackingHandler.multiUptimeInSeconds

        if (sinceLast > 0) lastMultiCollectionUpdate = EpochMark.now()

        val sessionStart = sessionStartCollections.getOrDefault(coll, currentCollection)
        val collectedSinceStart = currentCollection - sessionStart

        collectionAmounts[coll] = currentCollection
        collectionMade[coll] = collectedSinceStart
        collectionRate[coll] = if (uptime > 0) (collectedSinceStart / (uptime / 3600.0)).toLong() else 0L

        if (coll != "gemstone") {
            // NPC Prices
            val npcPrice = NpcPrices.getNpcPrice(coll)
            moneyRateNPC[coll] = if (uptime > 0) (npcPrice * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
            moneyMadeNPC[coll] = npcPrice * collectedSinceStart

            // Bazaar Prices
            if (BazaarCollectionsManager.hasBazaarData) updateBazaarRates(coll, collectedSinceStart, uptime)

            // Special handling for gemstones
            if (GemstonesManager.checkIfGemstone(coll)) updateGemstoneDetailedRates(coll, collectedSinceStart, uptime)
        }

        if (!MultiCollectionOverlay.trackingDirty) MultiCollectionOverlay.trackingDirty = true

        updateMultiLeaderboardStats()
    }

    fun updateMultiLeaderboardStats() {
        if (!collectionLeaderboard || CollectionTracker.collectionList.size != 1 || !CollectionTracker.collectionList.contains("gemstone")) return

        val currentGemstoneAmount = collectionAmounts["gemstone"] ?: 0L
        playerCurrentRank = LeaderboardManager.getPlayerRank(currentGemstoneAmount)

        val nextEntry = LeaderboardManager.getNextRankEntry(currentGemstoneAmount)
        if (nextEntry != null) {
            nextRankUsername = nextEntry.username
            nextRankAmount = nextEntry.amount
            collectionTillNextRank = nextRankAmount - currentGemstoneAmount
            isNextWiped = nextEntry.wiped

            val currentRate = collectionRate["gemstone"] ?: 0L
            if (currentRate > 0) {
                val seconds = (collectionTillNextRank / (currentRate / 3600.0)).toLong()
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

        val previousEntry = LeaderboardManager.getPreviousRankEntry(currentGemstoneAmount)
        if (previousEntry != null) {
            previousRankUsername = previousEntry.username
            previousRankAmount = previousEntry.amount
            collectionAbovePreviousRankAmount = currentGemstoneAmount - previousRankAmount
            isPreviousWiped = previousEntry.wiped
        } else {
            previousRankUsername = null
            previousRankAmount = -1L
            collectionAbovePreviousRankAmount = -1L
            isPreviousWiped = false
        }
    }

    private fun updateBazaarRates(coll: String, collectedSinceStart: Long, uptime: Long) {
        if (uptime <= 0) return

        val type = CollectionsManager.multiCollectionTypes[coll] ?: return
        when (type) {
            "normal" -> {
                val buyPrice = BazaarPrices.multiBaseInstantBuy[coll] ?: 0f
                val sellPrice = BazaarPrices.multiBaseInstantSell[coll] ?: 0f

                val rateMap = baseMoneyRate.computeIfAbsent(coll) { ConcurrentHashMap() }
                val madeMap = baseMoneyMade.computeIfAbsent(coll) { ConcurrentHashMap() }

                rateMap[BazaarPriceType.INSTANT_BUY] = floor(buyPrice * collectedSinceStart  / (uptime / 3600.0)).toLong()
                rateMap[BazaarPriceType.INSTANT_SELL] = floor(sellPrice * collectedSinceStart  / (uptime / 3600.0)).toLong()
                madeMap[BazaarPriceType.INSTANT_BUY] = floor(buyPrice * collectedSinceStart).toLong()
                madeMap[BazaarPriceType.INSTANT_SELL] = floor(sellPrice * collectedSinceStart).toLong()
            }
            "enchanted" -> {
                val enchantedRecipe = BazaarCollectionsManager.multiEnchantedRecipes[coll]
                val enchantedDivisor = enchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                val eBuyPrice = BazaarPrices.multiEnchantedInstantBuy[coll] ?: 0f
                val eSellPrice = BazaarPrices.multiEnchantedInstantSell[coll] ?: 0f

                val enchRateMap = enchantedMoneyRate.computeIfAbsent(coll) { ConcurrentHashMap() }
                val enchMadeMap = enchantedMoneyMade.computeIfAbsent(coll) { ConcurrentHashMap() }

                enchRateMap[BazaarPriceType.INSTANT_BUY] = floor(eBuyPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong()
                enchRateMap[BazaarPriceType.INSTANT_SELL] = floor(eSellPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong()
                enchMadeMap[BazaarPriceType.INSTANT_BUY] = floor(eBuyPrice * (collectedSinceStart / enchantedDivisor)).toLong()
                enchMadeMap[BazaarPriceType.INSTANT_SELL] = floor(eSellPrice * (collectedSinceStart / enchantedDivisor)).toLong()

                val seBuyPrice = BazaarPrices.multiSuperEnchantedInstantBuy[coll] ?: 0f
                val seSellPrice = BazaarPrices.multiSuperEnchantedInstantSell[coll] ?: 0f

                if (seBuyPrice != 0f) {
                    val superEnchantedRecipe = BazaarCollectionsManager.multiSuperEnchantedRecipes[coll]
                    val superDivisor = superEnchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                    val seRateMap = superEnchantedMoneyRate.computeIfAbsent(coll) { ConcurrentHashMap() }
                    val seMadeMap = superEnchantedMoneyMade.computeIfAbsent(coll) { ConcurrentHashMap() }

                    seRateMap[BazaarPriceType.INSTANT_BUY] = floor(seBuyPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong()
                    seRateMap[BazaarPriceType.INSTANT_SELL] = floor(seSellPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong()
                    seMadeMap[BazaarPriceType.INSTANT_BUY] = floor(seBuyPrice * (collectedSinceStart / superDivisor)).toLong()
                    seMadeMap[BazaarPriceType.INSTANT_SELL] = floor(seSellPrice * (collectedSinceStart / superDivisor)).toLong()
                }
            }
        }
    }

    private fun updateGemstoneDetailedRates(gemstoneType: String, collectedSinceStart: Long, uptime: Long) {
        val gemstoneKey = gemstoneType.lowercase()

        // NPC Prices
        val baseNpcPrice = NpcPrices.getNpcPrice(gemstoneKey)

        if (baseNpcPrice != -1) {
            for (tier in GemstoneTier.entries) {
                val keyPrefix = "${gemstoneType.uppercase()}_${tier.name}"
                val multiplier = tier.recipeAmount.toDouble()
                moneyRateNPC[keyPrefix] = if (uptime > 0) floor(baseNpcPrice * multiplier * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
                moneyMadeNPC[keyPrefix] = floor(baseNpcPrice * multiplier * collectedSinceStart).toLong()
            }
        }

        // Bazaar Prices
        if (BazaarCollectionsManager.hasBazaarData) {
            val gemstoneBuyPrices = GemstonePrices.multiGemstoneInstantBuyPrices[gemstoneKey] ?: emptyMap()
            val gemstoneSellPrices = GemstonePrices.multiGemstoneInstantSellPrices[gemstoneKey] ?: emptyMap()

            val collRateMap = gemstoneMoneyRate.computeIfAbsent(gemstoneType) { ConcurrentHashMap() }
            val collMadeMap = gemstoneMoneyMade.computeIfAbsent(gemstoneType) { ConcurrentHashMap() }

            for (tier in GemstoneTier.entries) {
                val buyPrice = gemstoneBuyPrices[tier.name] ?: 0f
                val sellPrice = gemstoneSellPrices[tier.name] ?: 0f
                val divisor = tier.recipeAmount.toDouble()

                val tierRateMap = collRateMap.computeIfAbsent(tier) { ConcurrentHashMap() }
                val tierMadeMap = collMadeMap.computeIfAbsent(tier) { ConcurrentHashMap() }

                tierRateMap[BazaarPriceType.INSTANT_BUY] = if (uptime > 0) floor(buyPrice * (collectedSinceStart / divisor) / (uptime / 3600.0)).toLong() else 0L
                tierRateMap[BazaarPriceType.INSTANT_SELL] = if (uptime > 0) floor(sellPrice * (collectedSinceStart / divisor) / (uptime / 3600.0)).toLong() else 0L

                tierMadeMap[BazaarPriceType.INSTANT_BUY] = floor(buyPrice * (collectedSinceStart / divisor)).toLong()
                tierMadeMap[BazaarPriceType.INSTANT_SELL] = floor(sellPrice * (collectedSinceStart / divisor)).toLong()
            }
        }
    }

    fun clearCollectionData() {
        collectionAmounts.clear()
        collectionRate.clear()
        collectionMade.clear()
        sessionStartCollections.clear()
        lastApiCollections.clear()
        lastMultiCollectionUpdate = null
        seenGemstones.clear()
    }

    fun clearAllMoneyMaps() {
        moneyRateNPC.clear()
        moneyMadeNPC.clear()
        baseMoneyMade.clear()
        baseMoneyRate.clear()
        enchantedMoneyMade.clear()
        enchantedMoneyRate.clear()
        superEnchantedMoneyMade.clear()
        superEnchantedMoneyRate.clear()
        gemstoneMoneyMade.clear()
        gemstoneMoneyRate.clear()
        seenGemstones.clear()
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