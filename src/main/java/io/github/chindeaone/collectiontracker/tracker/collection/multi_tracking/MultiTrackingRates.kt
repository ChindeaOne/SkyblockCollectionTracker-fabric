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

    var afk: Boolean = false
    var unchangedStreaks = mutableMapOf<String, Int>()
    private const val THRESHOLD = 2

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

    fun calculateMultiRates(values: Map<String, Long> = emptyMap(), gemstone: String = "", amount: Int = 0) {
        var anyChange = false
        val uptime = MultiTrackingHandler.getMultiUptimeInSeconds()
        val now = System.currentTimeMillis()

        // Handle gemstone update from chat
        if (gemstone.isNotEmpty() && amount > 0) {
            seenGemstones.add(gemstone)
            updateGemstoneRates(gemstone, amount.toLong(), uptime.toDouble())
            if (!MultiCollectionOverlay.trackingDirty) MultiCollectionOverlay.trackingDirty = true
            return
        }

        for ((coll, currentCollection) in values) {
            val lastCollection = lastApiCollections.getOrDefault(coll, -1L)

            // Special handling for gemstones
            if (coll == "gemstone") {
                // Only initialize session start on the first call
                if (sessionStartCollections.getOrDefault(coll, -1L) == -1L) {
                    sessionStartCollections[coll] = currentCollection
                    lastApiCollections[coll] = currentCollection
                    collectionAmounts[coll] = currentCollection

                    // Initialize NPC money maps for gemstones
                    GemstonesManager.gemstones.forEach { gemstoneType ->
                        val gemstoneKey = gemstoneType.lowercase()
                        GemstonePrices.multiGemstoneRecipes[gemstoneKey]?.keys?.forEach { tier ->
                            val keyPrefix = (gemstoneType + "_" + tier).uppercase()
                            moneyPerHourNPC.putIfAbsent(keyPrefix, 0L)
                            moneyMadeNPC.putIfAbsent(keyPrefix, 0L)
                        }
                    }
                }
                continue
            }

            val sinceLast = if (lastCollection != -1L) 0.coerceAtLeast((currentCollection - lastCollection).toInt()) else 0L
            collectionSinceLast[coll] = sinceLast.toLong()
            lastCollectionTimes[coll] = now

            // AFK detection
            if (currentCollection == lastCollection) {
                unchangedStreaks[coll] = unchangedStreaks.getOrDefault(coll, 0) + 1
            } else {
                unchangedStreaks[coll] = 0
                lastApiCollections[coll] = currentCollection
                anyChange = true
            }

            // Set starting collection
            if (sessionStartCollections.getOrDefault(coll, -1L) == -1L) {
                sessionStartCollections[coll] = currentCollection
            }

            // NPC Prices
            val type = CollectionsManager.multiCollectionTypes[coll]
            if (type == "gemstone") {
                // Skip `gemstone`
                continue
            } else {
                val collectedSinceStart = currentCollection - sessionStartCollections.getOrDefault(coll, currentCollection)
                collectionMade[coll] = collectedSinceStart
                collectionAmounts[coll] = currentCollection
                collectionPerHour[coll] = if (uptime > 0) (collectedSinceStart / (uptime / 3600.0)).toLong() else 0L

                val priceNPC = NpcPrices.getNpcPrice(coll)
                moneyPerHourNPC[coll] = if (uptime > 0) (priceNPC * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
                moneyMadeNPC[coll] = (priceNPC * collectedSinceStart)

                // Bazaar Prices
                if (BazaarCollectionsManager.hasBazaarData) {
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
            }
        }

        // Set AFK in NONE of the collections change for 2 consecutive checks
        if (!anyChange) {
            val allAfk = values.keys.all { unchangedStreaks.getOrDefault(it, 0) >= THRESHOLD }
            if (allAfk) {
                afk = true
                MultiTrackingHandler.stopMultiTracking()
            }
        }

        if (!MultiCollectionOverlay.trackingDirty) MultiCollectionOverlay.trackingDirty = true
    }

    private fun updateGemstoneRates(gemstoneType: String, amountAdded: Long, uptime: Double) {
        val currentMade = collectionMade.getOrDefault(gemstoneType, 0L) + amountAdded
        collectionMade[gemstoneType] = currentMade

        collectionMade["gemstone"] = (collectionMade["gemstone"] ?: 0L) + amountAdded
        collectionAmounts["gemstone"] = (collectionAmounts["gemstone"] ?: 0L) + amountAdded
        collectionPerHour["gemstone"] = if (uptime > 0) (collectionMade["gemstone"]!! / (uptime / 3600.0)).toLong() else 0L

        for (seenType in seenGemstones) {
            val seenMade = collectionMade.getOrDefault(seenType, 0L)
            collectionPerHour[seenType] = if (uptime > 0) (seenMade / (uptime / 3600.0)).toLong() else 0L

            val gemstoneKey = seenType.lowercase()
            val gemstoneRecipes = GemstonePrices.multiGemstoneRecipes[gemstoneKey] ?: emptyMap()

            // NPC Prices
            for (variant in gemstoneRecipes.keys) {
                val basePriceNPC = NpcPrices.getNpcPrice(seenType)
                val keyPrefix = (seenType + "_" + variant).uppercase()

                if (basePriceNPC != -1) {
                    moneyPerHourNPC[keyPrefix] = if (uptime > 0) (basePriceNPC * seenMade / (uptime / 3600.0)).toLong() else 0L
                    moneyMadeNPC[keyPrefix] = (basePriceNPC * seenMade)
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
                    val keyPrefix = (seenType + "_" + tier).uppercase()

                    moneyPerHourBazaar["${keyPrefix}_INSTANT_BUY"] = if (uptime > 0) (buyPrice * (seenMade / recipe) / (uptime / 3600.0)).toLong() else 0L
                    moneyPerHourBazaar["${keyPrefix}_INSTANT_SELL"] = if (uptime > 0) (sellPrice * (seenMade / recipe) / (uptime / 3600.0)).toLong() else 0L
                    moneyMadeBazaar["${keyPrefix}_INSTANT_BUY"] = (buyPrice * (seenMade / recipe)).toLong()
                    moneyMadeBazaar["${keyPrefix}_INSTANT_SELL"] = (sellPrice * (seenMade / recipe)).toLong()
                }
            }
        }
    }
}