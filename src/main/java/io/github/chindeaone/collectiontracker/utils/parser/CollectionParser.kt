package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.CollectionTier
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarType
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collectionList
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.collectionGemstoneTier
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.collectionTier
import io.github.chindeaone.collectiontracker.config.extraStatsText
import io.github.chindeaone.collectiontracker.config.gemstoneTier
import io.github.chindeaone.collectiontracker.config.leaderboardPosition
import io.github.chindeaone.collectiontracker.config.previousPosition
import io.github.chindeaone.collectiontracker.config.showExtraStats
import io.github.chindeaone.collectiontracker.config.statsText
import io.github.chindeaone.collectiontracker.config.trackingOptions
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.isEmpty
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatDouble
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatLong
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatBazaarItemName
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatCollectionName

object CollectionParser {
    fun updateTrackingLines(list: MutableList<String>) {
        if (statsText.isEmpty()) return

        for (id in statsText) {
            when (id) {
                CollectionConfig.OverlayText.COLLECTION -> addIfNotNull(list, handleCollection())
                CollectionConfig.OverlayText.COLLECTION_SESSION -> addIfNotNull(list, handleCollectionSession())
                CollectionConfig.OverlayText.COLL_PER_HOUR -> addIfNotNull(list, handleCollectionRate())
                CollectionConfig.OverlayText.MONEY_PER_HOUR -> addIfNotNull(list, handleMoneyRate())
                CollectionConfig.OverlayText.MONEY_MADE -> addIfNotNull(list, handleMoneyMade())
                CollectionConfig.OverlayText.COLLECTION_SINCE_LAST -> addIfNotNull(list, handleCollectionSinceLast())
                CollectionConfig.OverlayText.COLLECTION_SINCE_LAST_TIMER -> addIfNotNull(list, handleCollectionSinceLastTimer())
            }
        }

        if (collectionLeaderboard) {
            addIfNotNull(list, "")
            addIfNotNull(list, handleNextPosition())
            addIfNotNull(list, handleCollectionTillNextRank())
            addIfNotNull(list, handleEta())
            addIfNotNull(list, "")
            addIfNotNull(list, handlePreviousPosition())
            addIfNotNull(list, handleCollectionAbovePreviousRank())
        }
    }

    private fun handleNextPosition(): String? {
        if (isEmpty() || TrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (TrackingRates.nextRankAmount == -1L) return "Custom Position: Calculating..."
            if (TrackingRates.isNextWiped) return "Custom Position (${TrackingRates.nextRankUsername}-wiped): ${formatLong(TrackingRates.nextRankAmount)}"
            return "Custom Position (${TrackingRates.nextRankUsername}): ${formatLong(TrackingRates.nextRankAmount)}"
        }

        if (TrackingRates.nextRankUsername == null) return "Next Position: Calculating..."
        if (TrackingRates.isNextWiped) return "Next Position (${TrackingRates.nextRankUsername}-wiped): ${formatLong(TrackingRates.nextRankAmount)}"
        return "Next Position (${TrackingRates.nextRankUsername}): ${formatLong(TrackingRates.nextRankAmount)}"
    }

    private fun handleCollectionTillNextRank(): String? {
        if (isEmpty() || TrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (TrackingRates.collectionTillNextRank == -1L) return "Till Custom Position: Calculating..."
            return "Till Custom Position: ${formatLong(TrackingRates.collectionTillNextRank)}"
        }

        if (TrackingRates.collectionTillNextRank == -1L) return "Till Next Position: Calculating..."
        return "Till Next Position: ${formatLong(TrackingRates.collectionTillNextRank)}"
    }

    private fun handleEta(): String? {
        if (isEmpty() || TrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (TrackingRates.etaToNextRank == null) return "ETA to Custom Position: Calculating..."
            return "ETA to Custom Position: ${TrackingRates.etaToNextRank}"
        }

        if (TrackingRates.etaToNextRank == null) return "ETA: Calculating..."
        return "ETA: ${TrackingRates.etaToNextRank}"
    }

    private fun handlePreviousPosition(): String? {
        if (isEmpty() || !previousPosition) return null

        if (TrackingRates.previousRankUsername == null) return "Passed: Calculating..."
        if (TrackingRates.isPreviousWiped) return "Passed (${TrackingRates.previousRankUsername}-wiped): ${formatLong(TrackingRates.previousRankAmount)}"
        return "Passed (${TrackingRates.previousRankUsername}): ${formatLong(TrackingRates.previousRankAmount)}"
    }

    private fun handleCollectionAbovePreviousRank(): String? {
        if (isEmpty() || !previousPosition) return null

        if (TrackingRates.collectionAbovePreviousRankAmount == -1L) return "Difference: Calculating..."
        return "Difference: ${formatLong(TrackingRates.collectionAbovePreviousRankAmount)}"
    }

    private fun handleMultiNextPosition(): String? {
        if (isEmpty() || MultiTrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (MultiTrackingRates.nextRankAmount == -1L) return "Custom Position: Calculating..."
            if (MultiTrackingRates.isNextWiped) return "Custom Position (${MultiTrackingRates.nextRankUsername}-wiped): ${formatLong(MultiTrackingRates.nextRankAmount)}"
            return "Custom Position (${MultiTrackingRates.nextRankUsername}): ${formatLong(MultiTrackingRates.nextRankAmount)}"
        }

        if (MultiTrackingRates.nextRankUsername == null) return "Next Position: Calculating..."
        if (MultiTrackingRates.isNextWiped) return "Next Position (${MultiTrackingRates.nextRankUsername}-wiped): ${formatLong(MultiTrackingRates.nextRankAmount)}"
        return "Next Position (${MultiTrackingRates.nextRankUsername}): ${formatLong(MultiTrackingRates.nextRankAmount)}"
    }

    private fun handleMultiCollectionTillNextRank(): String? {
        if (isEmpty() || MultiTrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (MultiTrackingRates.collectionTillNextRank == -1L) return "Till Custom Position: Calculating..."
            return "Till Custom Position: ${formatLong(MultiTrackingRates.collectionTillNextRank)}"
        }

        if (MultiTrackingRates.collectionTillNextRank == -1L) return "Till Next Position: Calculating..."
        return "Till Next Position: ${formatLong(MultiTrackingRates.collectionTillNextRank)}"
    }

    private fun handleMultiEta(): String? {
        if (isEmpty() || MultiTrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (MultiTrackingRates.etaToNextRank == null) return "ETA to Custom Position: Calculating..."
            return "ETA to Custom Position: ${MultiTrackingRates.etaToNextRank}"
        }

        if (MultiTrackingRates.etaToNextRank == null) return "ETA: Calculating..."
        return "ETA: ${MultiTrackingRates.etaToNextRank}"
    }

    private fun handleMultiPreviousPosition(): String? {
        if (isEmpty() || !previousPosition) return null

        if (MultiTrackingRates.previousRankUsername == null) return "Passed: Calculating..."
        if (MultiTrackingRates.isPreviousWiped) return "Passed (${MultiTrackingRates.previousRankUsername}-wiped): ${formatLong(MultiTrackingRates.previousRankAmount)}"
        return "Passed (${MultiTrackingRates.previousRankUsername}): ${formatLong(MultiTrackingRates.previousRankAmount)}"
    }

    private fun handleMultiCollectionAbovePreviousRank(): String? {
        if (isEmpty() || !previousPosition) return null

        if (MultiTrackingRates.collectionAbovePreviousRankAmount == -1L) return "Difference: Calculating..."
        return "Difference: ${formatLong(MultiTrackingRates.collectionAbovePreviousRankAmount)}"
    }

    private fun addIfNotNull(list: MutableList<String>, line: String?) {
        if (line != null) list.add(line)
    }

    private fun handleCollection(): String? {
        if (CollectionsManager.collectionSource == "sacks") return null
        var rankSuffix = ""
        if (collectionLeaderboard && TrackingRates.playerCurrentRank != -1) {
            rankSuffix = if (TrackingRates.playerCurrentRank == 10001) " [Too low]"
            else " [#${TrackingRates.playerCurrentRank}]"
        }

        if (TrackingRates.collectionAmount <= 0) return "${formatCollectionName(collection)}: Calculating...$rankSuffix"
        return "${formatCollectionName(collection)}: ${formatLong(TrackingRates.collectionAmount)}$rankSuffix"
    }

    private fun handleCollectionSession(): String {
        if (TrackingRates.collectionMade <= 0) return "${formatCollectionName(collection)} (session): Calculating..."

        val (convertedAmount, indicator) = getCompactedCollection(TrackingRates.collectionMade)
        val formatted = formatDouble(convertedAmount)

        return "${formatCollectionName(collection)} (session): $formatted$indicator"
    }

    private fun handleCollectionRate(): String {
        if (TrackingRates.collectionRate <= 0) return "Coll/h: Calculating..."

        val (convertedRate, indicator) = getCompactedCollection(TrackingRates.collectionRate)
        val formatted = formatDouble(convertedRate)

        return "Coll/h: $formatted$indicator"
    }

    private fun handleMoneyRate(): String? {
        if (collectionType == null) return null

        val hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0

        if (!useBazaar) {
            if (!hasNpcPrice) return null

            if (TrackingRates.moneyRateNPC <= 0L) {
                return if (riftTracking) "Motes/h: Calculating..." else "$/h (NPC): Calculating..."
            }
            if (riftTracking) {
                return "Motes/h: ${StringUtils.formatLong(TrackingRates.moneyRateNPC)}"
            }

            return "$/h (NPC): ${StringUtils.formatLong(TrackingRates.moneyRateNPC)}"
        }

        val money = getSingleBazaarRate()
        return "$/h (Bazaar): ${StringUtils.formatLong(money)}${if (money > 0L) getMoneyDisplayLabel() else ""}"
    }

    private fun handleMoneyMade(): String? {
        if (collectionType == null) return null

        val hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0

        if (!useBazaar) {
            if (!hasNpcPrice) return null

            if (TrackingRates.moneyMadeNPC == 0L) {
                return if (riftTracking) "Motes made: Calculating..." else "$ made (NPC): Calculating..."
            }
            if (riftTracking) {
                return "Motes made: ${StringUtils.formatLong(TrackingRates.moneyMadeNPC)}"
            }

            return "$ made (NPC): ${StringUtils.formatLong(TrackingRates.moneyMadeNPC)}"
        }

        val money = getSingleBazaarMade()
        return "$ made (Bazaar): ${StringUtils.formatLong(money)}${if (money > 0L) getMoneyDisplayLabel() else ""}"
    }

    private fun handleCollectionSinceLast(): String {
        if (TrackingRates.collectionSinceLast <= 0) return "${formatCollectionName(collection)} since last: Calculating..."
        return "${formatCollectionName(collection)} since last: ${formatLong(TrackingRates.collectionSinceLast)}"
    }

    private fun handleCollectionSinceLastTimer(): String {
        val lastUpdate = TrackingRates.lastCollectionUpdate ?: return "Last updated: Calculating..."
        val totalSeconds = lastUpdate.timePassedSince().inWholeSeconds

        return "Last updated: ${StringUtils.formatCompactTime(totalSeconds)} ago"
    }

    // Only if it has bazaar data and is enabled
    fun updateTrackingExtraLines(list: MutableList<String>) {
        list.add("§6§lBazaar Info:")
        for (id in extraStatsText) {
            when (id) {
                CollectionConfig.OverlayExtraText.BAZAAR_ITEM -> addIfNotNull(list, handleBazaarItem())
                CollectionConfig.OverlayExtraText.BAZAAR_PRICE -> addIfNotNull(list, handleBazaarPrice())
            }
        }
    }

    private fun handleBazaarItem(): String? {
        when (collectionType) {
            "enchanted" -> {
                val recipe = when (getEffectiveBazaarType()) {
                    BazaarType.ENCHANTED_VERSION -> BazaarCollectionsManager.enchantedRecipe
                    BazaarType.SUPER_ENCHANTED_VERSION -> BazaarCollectionsManager.superEnchantedRecipe
                }

                return recipe.keys.firstOrNull()?.let { "Bazaar item: ${formatBazaarItemName(it)}" }
            }
            "gemstone" -> return "Bazaar item: $gemstoneTier"
            else -> return null
        }
    }

    private fun handleBazaarPrice(): String? {
        when (collectionType) {
            "enchanted" -> {
                val effectiveBazaarType = getEffectiveBazaarType()
                val price = when (effectiveBazaarType) {
                    BazaarType.ENCHANTED_VERSION -> if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) BazaarPrices.enchantedInstantBuy else BazaarPrices.enchantedInstantSell
                    BazaarType.SUPER_ENCHANTED_VERSION -> if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) BazaarPrices.superEnchantedInstantBuy else BazaarPrices.superEnchantedInstantSell
                }

                if (price == 0f) return "Item price: Unknown price"
                return "Item price: ${formatLong(price.toLong())}"
            }

            "gemstone" -> {
                val price = if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) GemstonePrices.getInstantBuyPrice(gemstoneTier) else GemstonePrices.getInstantSellPrice(gemstoneTier)
                if (price == 0f) return "Item price: Unknown price"

                return "Item price: ${formatLong(price.toLong())}"
            }

            else -> return null
        }
    }

    fun updateMultiTrackingLines(list: MutableList<String>, expanded: MutableList<String>, showPrefixes: Boolean) {
        val trackingOptions = trackingOptions
        for (coll in collectionList) {
            if ("gemstone" == coll) {
                val mainExpanded = expanded.contains("gemstone") && trackingOptions != MultiCollectionConfig.TrackingOptions.COLLECTION
                val showingCollection = trackingOptions == MultiCollectionConfig.TrackingOptions.COLLECTION
                val prefix = if (showPrefixes && !showingCollection) (if (mainExpanded) "§e[-]§r " else "§e[+]§r ") else ""

                val totalLine = when (trackingOptions) {
                    MultiCollectionConfig.TrackingOptions.COLLECTION -> handleMultiCollection("gemstone")
                    MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleMultiCollectionRate("gemstone")
                    MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleMultiCollectionSession("gemstone")
                    MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMultiMoneyRate("gemstone")
                    MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMultiMoneyMade("gemstone")
                }

                if (mainExpanded) {
                    list.add("${prefix}${totalLine.replaceFirst("Gemstone", "Gemstones")}")
                    MultiTrackingRates.seenGemstones.forEach { gem ->
                        val line = when (trackingOptions) {
                            MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleMultiCollectionRate(gem, addIndicator = false)
                            MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleMultiCollectionSession(gem, addIndicator = false)
                            MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMultiMoneyRate(gem)
                            MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMultiMoneyMade(gem)
                        }
                        list.add("  $line")
                    }
                } else {
                    list.add("$prefix$totalLine")
                }
                continue
            }

            val line = when (trackingOptions) {
                MultiCollectionConfig.TrackingOptions.COLLECTION -> handleMultiCollection(coll)
                MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleMultiCollectionRate(coll)
                MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleMultiCollectionSession(coll)
                MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMultiMoneyRate(coll)
                MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMultiMoneyMade(coll)
            }
            list.add(line)
        }

        handleMultiLeaderboard(list)

        when (trackingOptions) {
            MultiCollectionConfig.TrackingOptions.MONEY_RATE -> {
                val total = if (!useBazaar) {
                    MultiTrackingRates.moneyRateNPC.entries
                        .filter { (key, value) -> value > 0 && (!key.contains('_') || key.endsWith("_${gemstoneTier}")) }
                        .sumOf { it.value }
                } else {
                    val normalAndEnchanted = collectionList
                        .filterNot { it == "gemstone" }
                        .sumOf { coll ->
                            when (CollectionsManager.multiCollectionTypes[coll]) {
                                "normal" -> MultiTrackingRates.baseMoneyRate[coll]?.get(bazaarPriceType) ?: 0L
                                "enchanted" -> {
                                    val map = if (getEffectiveBazaarType(coll) == BazaarType.ENCHANTED_VERSION) {
                                        MultiTrackingRates.enchantedMoneyRate[coll]
                                    } else {
                                        MultiTrackingRates.superEnchantedMoneyRate[coll]
                                    }
                                    map?.get(bazaarPriceType) ?: 0L
                                }
                                else -> 0L
                            }
                        }

                    val gemstoneTotal = if (collectionList.contains("gemstone")) {
                        MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneBazaarRate(gem) }
                    } else 0L

                    normalAndEnchanted + gemstoneTotal
                }
                list.add("")
                list.add("§eOverall $/h (Bazaar): ${formatLong(total)}")
            }

            MultiCollectionConfig.TrackingOptions.MONEY_MADE -> {
                val total = if (!useBazaar) {
                    MultiTrackingRates.moneyMadeNPC.entries
                        .filter { (key, value) -> value > 0 && (!key.contains('_') || key.endsWith("_${gemstoneTier}")) }
                        .sumOf { it.value }
                } else {
                    val normalAndEnchanted = collectionList
                        .filterNot { it == "gemstone" }
                        .sumOf { coll ->
                            when (CollectionsManager.multiCollectionTypes[coll]) {
                                "normal" -> MultiTrackingRates.baseMoneyMade[coll]?.get(bazaarPriceType) ?: 0L
                                "enchanted" -> {
                                    val map = if (getEffectiveBazaarType(coll) == BazaarType.ENCHANTED_VERSION) {
                                        MultiTrackingRates.enchantedMoneyMade[coll]
                                    } else {
                                        MultiTrackingRates.superEnchantedMoneyMade[coll]
                                    }
                                    map?.get(bazaarPriceType) ?: 0L
                                }
                                else -> 0L
                            }
                        }

                    val gemstoneTotal = if (collectionList.contains("gemstone")) {
                        MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneBazaarMade(gem) }
                    } else 0L

                    normalAndEnchanted + gemstoneTotal
                }

                list.add("")
                list.add("§eOverall $ made (Bazaar): ${formatLong(total)}")
            }

            else -> {}
        }

        list.add("")
        list.add(handleMultiCollectionSinceLastTimer())
    }

    private fun handleMultiCollection(coll: String): String {
        var rankSuffix = ""
        if ("gemstone" == coll && collectionLeaderboard && MultiTrackingRates.playerCurrentRank != -1) {
            rankSuffix = if (MultiTrackingRates.playerCurrentRank == 10001) " [Too low]"
            else " [#${MultiTrackingRates.playerCurrentRank}]"
        }

        return if (MultiTrackingRates.collectionAmounts.getOrDefault(coll, -1L) >= 0)
            "${formatCollectionName(coll)}: ${formatLong(MultiTrackingRates.collectionAmounts.getOrDefault(coll, 0L))}$rankSuffix"
        else
            "${formatCollectionName(coll)}: Calculating..."
    }

    private fun handleMultiCollectionSession(coll: String, addIndicator: Boolean = true): String {
        if (MultiTrackingRates.collectionMade.getOrDefault(coll, -1L) <= 0) {
            return "${formatCollectionName(coll)} (session): Calculating..."
        }

        val (convertAmount, indicator) = getCompactedCollection(MultiTrackingRates.collectionMade.getOrDefault(coll, 0L), coll)
        val formatted = formatDouble(convertAmount)

        val suffix = when {
            coll == "gemstone" && addIndicator -> indicator
            GemstonesManager.checkIfGemstone(coll) -> ""
            addIndicator -> indicator
            else -> ""
        }

        return "${formatCollectionName(coll)} (session): $formatted$suffix"
    }

    private fun handleMultiCollectionRate(coll: String, addIndicator: Boolean = true): String {
        if (MultiTrackingRates.collectionRate.getOrDefault(coll, -1L) <= 0) {
            return "${formatCollectionName(coll)} Coll/h: Calculating..."
        }

        val (convertedRate, indicator) = getCompactedCollection(MultiTrackingRates.collectionRate.getOrDefault(coll, 0L), coll)
        val formatted = formatDouble(convertedRate)

        val suffix = when {
            coll == "gemstone" && addIndicator -> indicator
            GemstonesManager.checkIfGemstone(coll) -> ""
            addIndicator -> indicator
            else -> ""
        }

        return "${formatCollectionName(coll)} Coll/h: $formatted$suffix"
    }

    private fun handleMultiMoneyRate(coll: String): String {
        val useBazaar = useBazaar
        if ("gemstone" == coll) {
            val total = if (useBazaar) {
                MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneBazaarRate(gem) }
            } else {
                MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneNpcRate(gem) }
            }
            return "Gemstone $/h (${if (useBazaar) "Bazaar" else "NPC"}): ${StringUtils.formatLong(total)}${getMoneyDisplayLabel(coll)}"
        }

        if (!useBazaar) {
            val key = if (MultiTrackingRates.seenGemstones.contains(coll)) "${coll}_${GemstoneTier.ROUGH}".uppercase() else coll
            val npcRate = MultiTrackingRates.moneyRateNPC.getOrDefault(key, -1L)
            if (riftTracking) {
                return "${formatCollectionName(coll)} Motes/h: ${StringUtils.formatLong(npcRate)}"
            }
            return "${formatCollectionName(coll)} $/h (NPC): ${StringUtils.formatLong(npcRate)}"
        }

        val rate = getMultiBazaarRate(coll)
        return "${formatCollectionName(coll)} $/h (Bazaar): ${StringUtils.formatLong(rate)}${getMoneyDisplayLabel(coll)}"
    }

    private fun handleMultiMoneyMade(coll: String): String {
        val useBazaar = useBazaar
        if ("gemstone" == coll) {
            val total = if (useBazaar) {
                MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneBazaarMade(gem) }
            } else {
                MultiTrackingRates.seenGemstones.sumOf { gem -> getMultiGemstoneNpcMade(gem) }
            }
            return "Gemstone $ made (${if (useBazaar) "Bazaar" else "NPC"}): ${StringUtils.formatLong(total)}${getMoneyDisplayLabel(coll)}"
        }

        if (!useBazaar) {
            val key = if (MultiTrackingRates.seenGemstones.contains(coll)) "${coll}_${GemstoneTier.ROUGH}".uppercase() else coll
            val npcMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(key, -1L)
            if (riftTracking) {
                return "${formatCollectionName(coll)} Motes made: ${StringUtils.formatLong(npcMoney)}"
            }
            return "${formatCollectionName(coll)} $ made (NPC): ${StringUtils.formatLong(npcMoney)}"
        }

        val money = getMultiBazaarMade(coll)
        return "${formatCollectionName(coll)} $ made (Bazaar): ${StringUtils.formatLong(money)}${getMoneyDisplayLabel(coll)}"
    }

    private fun handleMultiLeaderboard(list: MutableList<String>) {
        if (collectionLeaderboard) {
            val tracked: MutableList<String> = collectionList
            if (tracked.size == 1 && tracked.contains("gemstone")) {
                addIfNotNull(list, "")
                addIfNotNull(list, handleMultiNextPosition())
                addIfNotNull(list, handleMultiCollectionTillNextRank())
                addIfNotNull(list, handleMultiEta())
                addIfNotNull(list, "")
                addIfNotNull(list, handleMultiPreviousPosition())
                addIfNotNull(list, handleMultiCollectionAbovePreviousRank())
            }
        }
    }

    private fun handleMultiCollectionSinceLastTimer(): String {
        val lastUpdate = MultiTrackingRates.lastMultiCollectionUpdate ?: return "Last updated: Calculating..."
        val totalSeconds = lastUpdate.timePassedSince()

        return "Last updated: ${totalSeconds.formatCompact()} ago"
    }

    fun addToggleableSettingsLines(list: MutableList<String>) {
        list.add("")

        if (MultiTrackingHandler.isMultiTracking) {
            list.add("§eStats: [$trackingOptions]")
            list.add("")
        }

        if (MultiTrackingHandler.isMultiTracking) {
            if (collectionList.contains("gemstone")) {
                list.add("§eGemstone Tier: [${collectionGemstoneTier}]")
            }
            if (collectionList.any { it != "gemstone" }) {
                list.add("§eCollection Tier: [${collectionTier}]")
            }
        } else {
            if (GemstonesManager.checkIfGemstone(collection)) {
                list.add("§eGemstone Tier: [${collectionGemstoneTier}]")
            } else {
                list.add("§eCollection Tier: [${collectionTier}]")
            }
        }

        list.add("§ePrice Source: ${(if (useBazaar) "[Bazaar]" else "[NPC]")}")
        list.add("")

        if (useBazaar) {
            list.add("§eBazaar Price: [Instant ${(if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) "Buy" else "Sell")}]")

            if (collectionList.contains("gemstone") || GemstonesManager.checkIfGemstone(collection)) {
                list.add("§eGemstone Price Tier: [${gemstoneTier}]")
            }
            if ("enchanted" == collectionType || CollectionsManager.multiCollectionTypes.containsValue("enchanted")) {
                list.add("§eCollection Price Tier: [${bazaarType}]")
            }

            if (TrackingHandler.isTracking) {
                list.add("${(if (showExtraStats) "§a" else "§e")}[Extra Stats]")
            }
        }
    }

    private fun getSingleBazaarRate(): Long {
        return when (collectionType) {
            "normal" -> TrackingRates.baseMoneyRate[bazaarPriceType] ?: 0L
            "enchanted" -> {
                val map = if (getEffectiveBazaarType() == BazaarType.ENCHANTED_VERSION) {
                    TrackingRates.enchantedMoneyRate
                } else {
                    TrackingRates.superEnchantedMoneyRate
                }
                map[bazaarPriceType] ?: 0L
            }
            "gemstone" -> TrackingRates.gemstoneMoneyRate[gemstoneTier]?.get(bazaarPriceType) ?: 0L
            else -> 0L
        }
    }

    private fun getSingleBazaarMade(): Long {
        return when (collectionType) {
            "normal" -> TrackingRates.baseMoneyMade[bazaarPriceType] ?: 0L
            "enchanted" -> {
                val map = if (getEffectiveBazaarType() == BazaarType.ENCHANTED_VERSION) {
                    TrackingRates.enchantedMoneyMade
                } else {
                    TrackingRates.superEnchantedMoneyMade
                }
                map[bazaarPriceType] ?: 0L
            }
            "gemstone" -> TrackingRates.gemstoneMoneyMade[gemstoneTier]?.get(bazaarPriceType) ?: 0L
            else -> 0L
        }
    }

    private fun getMultiBazaarRate(coll: String): Long {
        if (GemstonesManager.checkIfGemstone(coll)) {
            return MultiTrackingRates.gemstoneMoneyRate[coll]?.get(gemstoneTier)?.get(bazaarPriceType) ?: 0L
        }

        return when (CollectionsManager.multiCollectionTypes[coll]) {
            "normal" -> MultiTrackingRates.baseMoneyRate[coll]?.get(bazaarPriceType) ?: 0L
            "enchanted" -> {
                val map = if (getEffectiveBazaarType(coll) == BazaarType.ENCHANTED_VERSION) {
                    MultiTrackingRates.enchantedMoneyRate[coll]
                } else {
                    MultiTrackingRates.superEnchantedMoneyRate[coll]
                }
                map?.get(bazaarPriceType) ?: 0L
            }
            else -> 0L
        }
    }

    private fun getMultiBazaarMade(coll: String): Long {
        if (GemstonesManager.checkIfGemstone(coll)) {
            return MultiTrackingRates.gemstoneMoneyMade[coll]?.get(gemstoneTier)?.get(bazaarPriceType) ?: 0L
        }

        return when (CollectionsManager.multiCollectionTypes[coll]) {
            "normal" -> MultiTrackingRates.baseMoneyMade[coll]?.get(bazaarPriceType) ?: 0L
            "enchanted" -> {
                val map = if (getEffectiveBazaarType(coll) == BazaarType.ENCHANTED_VERSION) {
                    MultiTrackingRates.enchantedMoneyMade[coll]
                } else {
                    MultiTrackingRates.superEnchantedMoneyMade[coll]
                }
                map?.get(bazaarPriceType) ?: 0L
            }
            else -> 0L
        }
    }

    private fun getEffectiveBazaarType(coll: String? = null): BazaarType {
        if (bazaarType != BazaarType.SUPER_ENCHANTED_VERSION) return bazaarType

        return if (coll != null) {
            val type = CollectionsManager.multiCollectionTypes[coll]
            if (type == "enchanted" && BazaarCollectionsManager.multiSuperEnchantedRecipes[coll].isNullOrEmpty()) {
                BazaarType.ENCHANTED_VERSION
            } else {
                bazaarType
            }
        } else {
            if (collectionType == "enchanted" && BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                BazaarType.ENCHANTED_VERSION
            } else {
                bazaarType
            }
        }
    }

    private fun getEffectiveCollectionTier(coll: String? = null): CollectionTier {
        if (collectionTier != CollectionTier.SUPER_ENCHANTED) return collectionTier

        return if (coll != null) {
            val type = CollectionsManager.multiCollectionTypes[coll]
            if (type == "enchanted" && BazaarCollectionsManager.multiSuperEnchantedRecipes[coll].isNullOrEmpty()) {
                CollectionTier.ENCHANTED
            } else {
                collectionTier
            }
        } else {
            if (collectionType == "enchanted" && BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                CollectionTier.ENCHANTED
            } else {
                collectionTier
            }
        }
    }

    private fun getMultiGemstoneNpcRate(gem: String): Long =
        MultiTrackingRates.moneyRateNPC.getOrDefault("${gem}_${GemstoneTier.ROUGH}".uppercase(), 0L)

    private fun getMultiGemstoneNpcMade(gem: String): Long =
        MultiTrackingRates.moneyMadeNPC.getOrDefault("${gem}_${GemstoneTier.ROUGH}".uppercase(), 0L)

    private fun getMultiGemstoneBazaarRate(gem: String): Long =
        MultiTrackingRates.gemstoneMoneyRate[gem]?.get(gemstoneTier)?.get(bazaarPriceType) ?: 0L

    private fun getMultiGemstoneBazaarMade(gem: String): Long =
        MultiTrackingRates.gemstoneMoneyMade[gem]?.get(gemstoneTier)?.get(bazaarPriceType) ?: 0L

    private fun getCompactedCollection(amount: Long, coll: String? = null): Pair<Double, String> {
        val targetCollection = coll ?: collection

        val type = if (coll != null) {
            CollectionsManager.multiCollectionTypes[coll]
        } else {
            collectionType
        }

        if (coll == "gemstone" || GemstonesManager.checkIfGemstone(targetCollection)) {
            val tier = collectionGemstoneTier
            val divisor = tier.recipeAmount.toDouble()

            return Pair(amount.toDouble() / divisor, " (${tier.displayName.lowercase()})")
        }

        if (type != "enchanted") return Pair(amount.toDouble(), "")

        return when (getEffectiveCollectionTier(coll)) {
            CollectionTier.BASE -> Pair(amount.toDouble(), "")

            CollectionTier.ENCHANTED -> {
                val craftCost = if (coll != null) {
                    BazaarCollectionsManager.multiEnchantedRecipes[coll]?.values?.firstOrNull()?.toDouble() ?: 160.0
                } else {
                    BazaarCollectionsManager.enchantedRecipe.values.firstOrNull()?.toDouble() ?: 160.0
                }

                Pair(amount.toDouble() / craftCost, " (${CollectionTier.ENCHANTED.shortName})")
            }

            CollectionTier.SUPER_ENCHANTED -> {
                val craftCost = if (coll != null) {
                    BazaarCollectionsManager.multiSuperEnchantedRecipes[coll]?.values?.firstOrNull()?.toDouble() ?: 25600.0
                } else {
                    BazaarCollectionsManager.superEnchantedRecipe.values.firstOrNull()?.toDouble() ?: 25600.0
                }

                Pair(amount.toDouble() / craftCost, " (${CollectionTier.SUPER_ENCHANTED.shortName})")
            }
        }
    }

    private fun getMoneyDisplayLabel(coll: String? = null): String {
        val targetCollection = coll ?: collection

        if (targetCollection == "gemstone") {
            return if (gemstoneTier == GemstoneTier.ROUGH) "" else " (${gemstoneTier.displayName.lowercase()})"
        }

        if (GemstonesManager.checkIfGemstone(targetCollection)) {
            return ""
        }

        val type = if (coll != null) CollectionsManager.multiCollectionTypes[coll] else collectionType
        if (type != "enchanted") return ""

        return " (${getEffectiveBazaarType(coll).shortName})"
    }
}
