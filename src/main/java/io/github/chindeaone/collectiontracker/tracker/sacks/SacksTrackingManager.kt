package io.github.chindeaone.collectiontracker.tracker.sacks

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SacksTrackingManager {

    private val logger: Logger = LogManager.getLogger(SacksTrackingManager::class.java)

    fun onSacksGain(sacksDetails: Map<String, Int>) {

        if (TrackingHandler.isTracking) {
            handleTracking(sacksDetails)
            return
        }

        if (MultiTrackingHandler.isMultiTracking) {
            handleMultiTracking(sacksDetails)
        }
    }

    private fun handleTracking(sacksDetails: Map<String, Int>) {
        val collectionName = CollectionTracker.collection
        val type = CollectionsManager.collectionType
        var totalAmount = 0L

        val normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.enchantedRecipe, false, collectionName)
        val normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.superEnchantedRecipe, true, collectionName)

        for (entry in sacksDetails.entries) {
            val itemName = entry.key
            val amount = entry.value

            val enchantedMultiplier = normalizedEnchantedMap[itemName]
            val superEnchantedMultiplier = normalizedSuperEnchantedMap[itemName]

            val isEnchanted = enchantedMultiplier != null
            val isSuperEnchanted = superEnchantedMultiplier != null

            if (type == null) {
                logger.error("[SCT]: Collection type is null for collection: {}", collectionName)
                return
            }

            val matchesCollection =
                if (type == "gemstone") itemName.contains(collectionName)
                else itemName == collectionName

            if (!matchesCollection && !isEnchanted && !isSuperEnchanted) continue

            totalAmount += if (type == "gemstone") {
                amount * getGemstoneMultiplier(itemName)
            } else if (type == "enchanted") {
                if (isSuperEnchanted) {
                    amount * superEnchantedMultiplier
                } else if (isEnchanted) {
                    amount * enchantedMultiplier
                } else {
                    amount
                }
            } else {
                amount
            }
        }

        if (totalAmount > 0) {
            TrackingRates.calculateRates(totalAmount)
        }
    }

    private fun handleMultiTracking(sacksDetails: Map<String, Int>) {
        val gains = mutableMapOf<String, Long>()

        for (coll in CollectionTracker.collectionList) {
            val type = CollectionsManager.multiCollectionTypes[coll] ?: continue // gemstones

            var totalAmount = 0L
            val normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.multiEnchantedRecipes.getOrDefault(coll, mapOf()), false, coll)
            val normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.multiSuperEnchantedRecipes.getOrDefault(coll, mapOf()), true, coll)

            for (entry in sacksDetails.entries) {
                val itemName = entry.key
                val amount = entry.value

                if ("enchanted" == type) {
                    val enchantedMultiplier = normalizedEnchantedMap[itemName]
                    val superEnchantedMultiplier = normalizedSuperEnchantedMap[itemName]

                    val isEnchanted = enchantedMultiplier != null
                    val isSuperEnchanted = superEnchantedMultiplier != null

                    if (itemName != coll && !isEnchanted && !isSuperEnchanted) continue

                    totalAmount += when {
                        isSuperEnchanted -> amount * superEnchantedMultiplier
                        isEnchanted -> amount * enchantedMultiplier
                        else -> amount
                    }
                } else {
                    if (itemName != coll) continue
                    totalAmount += amount
                }
            }

            if (totalAmount > 0) {
                gains[coll] = totalAmount
            }
        }

        if (CollectionTracker.collectionList.contains("gemstone")) {
            var generalGemstoneGains = 0L

            sacksDetails.entries.forEach { entry ->
                val itemName = entry.key

                if (itemName.contains("gemstone")) {
                    val gain = entry.value.toLong() * getGemstoneMultiplier(itemName)
                    generalGemstoneGains += gain

                    var gemstoneType: String? = null
                    for (gem in GemstonesManager.gemstones) {
                        if (itemName.contains(gem.lowercase())) {
                            gemstoneType = gem.lowercase()
                            break
                        }
                    }
                    if (gemstoneType != null) {
                        gains.merge(gemstoneType, gain, Long::plus)
                    }
                }
            }

            if (generalGemstoneGains > 0) {
                gains.merge("gemstone", generalGemstoneGains, Long::plus)
            }
        }

        if (gains.isNotEmpty()) {
            MultiTrackingRates.calculateMultiRates(gains)
        }
    }

    private fun getGemstoneMultiplier(itemName: String): Int {
        return when {
            itemName.contains("flawless") -> 80 * 80 * 80
            itemName.contains("fine") -> 80 * 80
            itemName.contains("flawed") -> 80
            else -> 1
        }
    }

    private fun  normalizeMap(map: Map<String, Int>, isSuperEnchanted: Boolean, collectionName: String): Map<String, Int> {
        val normalizedMap = mutableMapOf<String, Int>()

        val overrides = if (isSuperEnchanted) SUPER_ENCHANTED_DISPLAY_OVERRIDES else ENCHANTED_DISPLAY_OVERRIDES
        val override = overrides[collectionName]

        map.entries.forEach { entry ->
            val key = entry.key.lowercase().replace("_", " ")
            normalizedMap[override ?: key] = entry.value
        }

        return normalizedMap
    }

    private val ENCHANTED_DISPLAY_OVERRIDES = mapOf(
            "gold ingot" to "enchanted gold ingot",
            "iron ingot" to "enchanted iron ingot",
            "redstone dust" to "enchanted redstone dust",
            "end stone" to "enchanted end stone",
            "nether quartz" to "enchanted nether quartz",
            "cocoa beans" to "enchanted cocoa beans",
            "nether wart" to "enchanted nether wart",
            "melon slice" to "enchanted melon slice",
            "raw rabbit" to "enchanted raw rabbit",
            "raw mutton" to "enchanted raw mutton",
            "raw porkchop" to "enchanted raw porkchop",
            "slimeball" to "enchanted slimeball",
            "lily pad" to "enchanted lily pad",
            "ink sac" to "enchanted ink sac",
            "raw cod" to "enchanted raw cod",
            "tropical fish" to "enchanted tropical fish",
            "magmafish" to "gold magmafish",
            "lotus" to "gold lotus"
    )

    private val SUPER_ENCHANTED_DISPLAY_OVERRIDES = mapOf(
            "red mushroom" to "enchanted red mushroom block",
            "brown mushroom" to "enchanted brown mushroom block",
            "nether wart" to "mutant nether wart",
            "melon slice" to "enchanted melon",
            "raw porkchop" to "enchanted cooked porkchop",
            "lily pad" to "condensed lily pad",
            "raw cod" to "enchanted cooked cod",
            "magmafish" to "silver magmafish",
            "lotus" to "silver lotus"
    )
}