package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import java.util.concurrent.ConcurrentHashMap

object MultiTrackingRates {

    // Collection tracking data
    val collectionAmounts = ConcurrentHashMap<String, Long>()
    val collectionPerHour = ConcurrentHashMap<String, Long>()
    val collectionMade = ConcurrentHashMap<String, Long>()
    val collectionSinceLast = ConcurrentHashMap<String, Long>()
    val sessionStartCollections = ConcurrentHashMap<String, Long>()
    val lastCollectionTimes = ConcurrentHashMap<String, Long>()
    val lastApiCollections = ConcurrentHashMap<String, Long>()

    // Track seen gemstones to only render them if they've been received from chat
    val seenGemstones: MutableSet<String>  = ConcurrentHashMap.newKeySet()

    // Money tracking data
    // NPC
    val moneyPerHourNPC = ConcurrentHashMap<String, Long>()
    val moneyMadeNPC = ConcurrentHashMap<String, Long>()

    // Bazaar
    val moneyMadeBazaar = ConcurrentHashMap<String, Long>()
    val moneyPerHourBazaar = ConcurrentHashMap<String, Long>()

    @JvmStatic
    fun setCollections(values: Map<String, Long>) {
        val now = System.currentTimeMillis()
        println("[SCT DEBUG] setCollections called with values: $values")
        for ((coll, value) in values) {
            lastApiCollections[coll] = value
            lastCollectionTimes[coll] = now
            if (sessionStartCollections.getOrDefault(coll, -1L) == -1L) {
                sessionStartCollections[coll] = value

                // Initialize NPC money maps for gemstones
                if (coll == "gemstone") {
                    println("[SCT DEBUG] Initializing gemstone NPC money maps")
                    GemstonesManager.gemstones?.forEach { gemstoneType ->
                        val gemstoneKey = gemstoneType.lowercase()
                        GemstonePrices.multiGemstoneRecipes[gemstoneKey]?.keys?.forEach { tier ->
                            val keyPrefix = (gemstoneType + "_" + tier).uppercase()
                            moneyPerHourNPC.putIfAbsent(keyPrefix, 0L)
                            moneyMadeNPC.putIfAbsent(keyPrefix, 0L)
                        }
                    }
                }
            }
            collectionAmounts[coll] = value
            updateValues(coll, value, 0L)
        }
    }

    @JvmStatic
    fun calculateMultiRates(gains: Map<String, Long>) {
        for ((coll, amount) in gains) {
            val isGemstone = GemstonesManager.checkIfGemstone(coll)
            if (isGemstone) {
                seenGemstones.add(coll)
            }

            val startValue = sessionStartCollections[coll] ?: -1L
            if (startValue == -1L) {
                sessionStartCollections[coll] = (collectionAmounts[coll] ?: lastApiCollections.getOrDefault(coll, 0L))
            }

            val currentTotal = (collectionAmounts[coll] ?: lastApiCollections.getOrDefault(coll, 0L)) + amount
            updateValues(coll, currentTotal, amount)
        }
    }

    private fun updateValues(coll: String, currentCollection: Long, sinceLast: Long) {
        val uptime = MultiTrackingHandler.getMultiUptimeInSeconds()
        val now = System.currentTimeMillis()

        collectionSinceLast[coll] = sinceLast
        if (sinceLast > 0) {
            lastCollectionTimes[coll] = now
        }

        val sessionStart = sessionStartCollections.getOrDefault(coll, currentCollection)
        val collectedSinceStart = currentCollection - sessionStart

        collectionAmounts[coll] = currentCollection
        collectionMade[coll] = collectedSinceStart
        collectionPerHour[coll] = if (uptime > 0) (collectedSinceStart / (uptime / 3600.0)).toLong() else 0L

        if (coll != "gemstone") {
            // NPC Prices
            val priceNPC = NpcPrices.getNpcPrice(coll)
            moneyPerHourNPC[coll] = if (uptime > 0) (priceNPC * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
            moneyMadeNPC[coll] = (priceNPC * collectedSinceStart)

            // Bazaar Prices
            if (BazaarCollectionsManager.hasBazaarData) {
                updateBazaarRates(coll, collectedSinceStart, uptime)
            }

            // Special handling for gemstones
            if (GemstonesManager.checkIfGemstone(coll)) {
                updateGemstoneDetailedRates(coll, collectedSinceStart, uptime)
            }
        }

        if (!MultiCollectionOverlay.trackingDirty) {
            MultiCollectionOverlay.trackingDirty = true
        }
    }

    private fun updateBazaarRates(coll: String, collectedSinceStart: Long, uptime: Long) {
        val type = CollectionsManager.multiCollectionTypes[coll] ?: return
        when (type) {
            "normal" -> {
                val buyPrice = BazaarPrices.multiNormalInstantBuy[coll] ?: 0f
                val sellPrice = BazaarPrices.multiNormalInstantSell[coll] ?: 0f

                moneyPerHourBazaar["${coll}_normal_INSTANT_BUY"] = if (uptime > 0) (buyPrice * (collectedSinceStart / 160.0) / (uptime / 3600.0)).toLong() else 0L
                moneyPerHourBazaar["${coll}_normal_INSTANT_SELL"] = if (uptime > 0) (sellPrice * (collectedSinceStart / 160.0) / (uptime / 3600.0)).toLong() else 0L
                moneyMadeBazaar["${coll}_normal_INSTANT_BUY"] = (buyPrice * collectedSinceStart).toLong()
                moneyMadeBazaar["${coll}_normal_INSTANT_SELL"] = (sellPrice * collectedSinceStart).toLong()
            }
            "enchanted" -> {
                val enchantedRecipe = BazaarCollectionsManager.multiEnchantedRecipes[coll]
                val enchantedDivisor = enchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                val eBuyPrice = BazaarPrices.multiEnchantedInstantBuy[coll] ?: 0f
                val eSellPrice = BazaarPrices.multiEnchantedInstantSell[coll] ?: 0f

                moneyPerHourBazaar["${coll}_Enchanted version_INSTANT_BUY"] = if (uptime > 0) (eBuyPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0L
                moneyPerHourBazaar["${coll}_Enchanted version_INSTANT_SELL"] = if (uptime > 0) (eSellPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0L
                moneyMadeBazaar["${coll}_Enchanted version_INSTANT_BUY"] = (eBuyPrice * (collectedSinceStart / enchantedDivisor)).toLong()
                moneyMadeBazaar["${coll}_Enchanted version_INSTANT_SELL"] = (eSellPrice * (collectedSinceStart / enchantedDivisor)).toLong()

                val seBuyPrice = BazaarPrices.multiSuperEnchantedInstantBuy[coll] ?: 0f
                val seSellPrice = BazaarPrices.multiSuperEnchantedInstantSell[coll] ?: 0f

                if (seBuyPrice != 0f) {
                    val superEnchantedRecipe = BazaarCollectionsManager.multiSuperEnchantedRecipes[coll]
                    val superDivisor = superEnchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                    moneyPerHourBazaar["${coll}_Super Enchanted version_INSTANT_BUY"] = if (uptime > 0) (seBuyPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong() else 0L
                    moneyPerHourBazaar["${coll}_Super Enchanted version_INSTANT_SELL"] = if (uptime > 0) (seSellPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong() else 0L
                    moneyMadeBazaar["${coll}_Super Enchanted version_INSTANT_BUY"] = (seBuyPrice * (collectedSinceStart / superDivisor)).toLong()
                    moneyMadeBazaar["${coll}_Super Enchanted version_INSTANT_SELL"] = (seSellPrice * (collectedSinceStart / superDivisor)).toLong()
                }
            }
        }
    }

    private fun updateGemstoneDetailedRates(gemstoneType: String, collectedSinceStart: Long, uptime: Long) {
        val gemstoneKey = gemstoneType.lowercase()
        val gemstoneRecipes = GemstonePrices.multiGemstoneRecipes[gemstoneKey] ?: emptyMap()

        // NPC Prices
        val basePriceNPC = NpcPrices.getNpcPrice(gemstoneKey)

        for (variant in gemstoneRecipes.keys) {
            val tier = variant.split("_")[0].uppercase()
            val keyPrefix = "${gemstoneType.uppercase()}_$tier"

            if (basePriceNPC != -1) {
                moneyPerHourNPC[keyPrefix] = if (uptime > 0) (basePriceNPC * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
                moneyMadeNPC[keyPrefix] = (basePriceNPC * collectedSinceStart)
            }
        }

        // Bazaar Prices
        if (BazaarCollectionsManager.hasBazaarData) {
            val gemstoneBuyPrices = GemstonePrices.multiGemstoneInstantBuyPrices[gemstoneKey] ?: emptyMap()
            val gemstoneSellPrices = GemstonePrices.multiGemstoneInstantSellPrices[gemstoneKey] ?: emptyMap()

            for (tier in gemstoneRecipes.keys) {
                val buyPrice = gemstoneBuyPrices[tier] ?: 0f
                val sellPrice = gemstoneSellPrices[tier] ?: 0f
                val recipe = gemstoneRecipes[tier]?.toDouble() ?: 1.0

                val tierName = tier.split("_")[0].uppercase()
                val keyPrefix = "${gemstoneType.uppercase()}_$tierName"

                moneyPerHourBazaar["${keyPrefix}_INSTANT_BUY"] = if (uptime > 0) (buyPrice * (collectedSinceStart / recipe) / (uptime / 3600.0)).toLong() else 0L
                moneyPerHourBazaar["${keyPrefix}_INSTANT_SELL"] = if (uptime > 0) (sellPrice * (collectedSinceStart / recipe) / (uptime / 3600.0)).toLong() else 0L
                moneyMadeBazaar["${keyPrefix}_INSTANT_BUY"] = (buyPrice * (collectedSinceStart / recipe)).toLong()
                moneyMadeBazaar["${keyPrefix}_INSTANT_SELL"] = (sellPrice * (collectedSinceStart / recipe)).toLong()
            }
        }
    }
}