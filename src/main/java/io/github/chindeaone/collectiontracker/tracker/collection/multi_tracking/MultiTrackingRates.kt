package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
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

    // Money tracking data
    // NPC
    val moneyPerHourNPC = ConcurrentHashMap<String, Long>()
    val moneyMadeNPC = ConcurrentHashMap<String, Long>()

    // Bazaar
    val moneyMadeBazaar = ConcurrentHashMap<String, Map<String, Long>>()
    val moneyPerHourBazaar = ConcurrentHashMap<String, Map<String, Long>>()

    fun calculateMultiRates(values: Map<String, Long>) {
        var anyChange = false
        val uptime = MultiTrackingHandler.getMultiUptimeInSeconds()
        val now = System.currentTimeMillis()

        for ((coll, currentCollection) in values) {
            val lastCollection = lastApiCollections.getOrDefault(coll, -1L)

            // Calculate since last
            val sinceLast = if (lastCollection != -1L) 0.coerceAtLeast((currentCollection - lastCollection).toInt()) else 0L
            collectionSinceLast[coll] = sinceLast.toLong()
            lastCollectionTimes[coll] = now

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

            val collectedSinceStart = currentCollection - sessionStartCollections.getOrDefault(coll, currentCollection)
            collectionMade[coll] = collectedSinceStart
            collectionAmounts[coll] = currentCollection
            collectionPerHour[coll] = if (uptime > 0) (collectedSinceStart / (uptime / 3600.0)).toLong() else 0L

            // NPC Prices
            val priceNPC = NpcPrices.getNpcPrice(coll)
            moneyPerHourNPC[coll] = if (uptime > 0) (priceNPC * collectedSinceStart / (uptime / 3600.0)).toLong() else 0L
            moneyMadeNPC[coll] = (priceNPC * collectedSinceStart)

            // Bazaar Prices
            if (BazaarCollectionsManager.hasBazaarData) {
                val type = CollectionsManager.multiCollectionTypes[coll]
                val collMoneyMade = mutableMapOf<String, Long>()
                val collMoneyPerHour = mutableMapOf<String, Long>()

                when (type) {
                    "normal" -> {
                        val buyPrice = BazaarPrices.multiNormalInstantBuy[coll] ?: 0f
                        val sellPrice = BazaarPrices.multiNormalInstantSell[coll] ?: 0f

                        val buyPerHour = if (uptime > 0) (buyPrice * (collectedSinceStart / 160.0) / (uptime / 3600.0)).toLong() else 0L
                        val sellPerHour = if (uptime > 0) (sellPrice * (collectedSinceStart / 160.0) / (uptime / 3600.0)).toLong() else 0L

                        collMoneyPerHour["normal_INSTANT_BUY"] = buyPerHour
                        collMoneyPerHour["normal_INSTANT_SELL"] = sellPerHour
                        collMoneyMade["normal_INSTANT_BUY"] = (buyPrice * collectedSinceStart).toLong()
                        collMoneyMade["normal_INSTANT_SELL"] = (sellPrice * collectedSinceStart).toLong()
                    }
                    "enchanted" -> {
                        val enchantedRecipe = BazaarCollectionsManager.multiEnchantedRecipes[coll]
                        val enchantedDivisor = enchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                        val eBuyPrice = BazaarPrices.multiEnchantedInstantBuy[coll] ?: 0f
                        val eSellPrice = BazaarPrices.multiEnchantedInstantSell[coll] ?: 0f

                        collMoneyPerHour["Enchanted version_INSTANT_BUY"] = if (uptime > 0) (eBuyPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0L
                        collMoneyPerHour["Enchanted version_INSTANT_SELL"] = if (uptime > 0) (eSellPrice * (collectedSinceStart / enchantedDivisor) / (uptime / 3600.0)).toLong() else 0L
                        collMoneyMade["Enchanted version_INSTANT_BUY"] = (eBuyPrice * (collectedSinceStart / enchantedDivisor)).toLong()
                        collMoneyMade["Enchanted version_INSTANT_SELL"] = (eSellPrice * (collectedSinceStart / enchantedDivisor)).toLong()

                        val seBuyPrice = BazaarPrices.multiSuperEnchantedInstantBuy[coll] ?: 0f
                        val seSellPrice = BazaarPrices.multiSuperEnchantedInstantSell[coll] ?: 0f

                        if (seBuyPrice != 0f) {
                            val superEnchantedRecipe = BazaarCollectionsManager.multiSuperEnchantedRecipes[coll]
                            val superDivisor = superEnchantedRecipe?.values?.firstOrNull()?.toDouble() ?: 1.0

                            collMoneyPerHour["Super Enchanted version_INSTANT_BUY"] = if (uptime > 0) (seBuyPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong() else 0L
                            collMoneyPerHour["Super Enchanted version_INSTANT_SELL"] = if (uptime > 0) (seSellPrice * (collectedSinceStart / superDivisor) / (uptime / 3600.0)).toLong() else 0L
                            collMoneyMade["Super Enchanted version_INSTANT_BUY"] = (seBuyPrice * (collectedSinceStart / superDivisor)).toLong()
                            collMoneyMade["Super Enchanted version_INSTANT_SELL"] = (seSellPrice * (collectedSinceStart / superDivisor)).toLong()
                        }
                    }
                    "gemstone" -> {
                        val gemstoneBuyPrices = GemstonePrices.multiGemstoneInstantBuyPrices[coll] ?: emptyMap()
                        val gemstoneSellPrices = GemstonePrices.multiGemstoneInstantSellPrices[coll] ?: emptyMap()
                        val gemstoneRecipes = GemstonePrices.multiGemstoneRecipes[coll] ?: emptyMap()

                        for (key in gemstoneSellPrices.keys) {
                            val buyPrice = gemstoneBuyPrices[key] ?: 0f
                            val sellPrice = gemstoneSellPrices[key] ?: 0f
                            val recipe = gemstoneRecipes[key]?.toDouble() ?: 1.0

                            collMoneyPerHour["${key}_INSTANT_BUY"] = if (uptime > 0) (buyPrice * (collectedSinceStart / recipe) / (uptime / 3600.0)).toLong() else 0L
                            collMoneyPerHour["${key}_INSTANT_SELL"] = if (uptime > 0) (sellPrice * (collectedSinceStart / recipe) / (uptime / 3600.0)).toLong() else 0L
                            collMoneyMade["${key}_INSTANT_BUY"] = (buyPrice * (collectedSinceStart / recipe)).toLong()
                            collMoneyMade["${key}_INSTANT_SELL"] = (sellPrice * (collectedSinceStart / recipe)).toLong()
                        }
                    }
                }
                moneyMadeBazaar[coll] = collMoneyMade
                moneyPerHourBazaar[coll] = collMoneyPerHour
            }
        }

        if (!anyChange) {
            val allAfk = values.keys.all { unchangedStreaks.getOrDefault(it, 0) >= THRESHOLD }
            if (allAfk) {
                afk = true
                MultiTrackingHandler.stopMultiTracking()
            }
        }

        if (!MultiCollectionOverlay.trackingDirty) MultiCollectionOverlay.trackingDirty = true
    }
}