package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collectionList
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.extraStatsText
import io.github.chindeaone.collectiontracker.config.gemstoneVariant
import io.github.chindeaone.collectiontracker.config.leaderboardPosition
import io.github.chindeaone.collectiontracker.config.previousPosition
import io.github.chindeaone.collectiontracker.config.showExtraStats
import io.github.chindeaone.collectiontracker.config.statsText
import io.github.chindeaone.collectiontracker.config.trackingOptions
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.isEmpty
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatBazaarItemName
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatNumberOrPlaceholder
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatCollectionName

object CollectionParser {
    fun updateTrackingLines(list: MutableList<String>) {
        if (statsText.isEmpty()) return

        for (id in statsText) {
            when (id) {
                CollectionConfig.OverlayText.COLLECTION -> addIfNotNull(list, handleCollection())
                CollectionConfig.OverlayText.COLLECTION_SESSION -> addIfNotNull(list, handleCollectionSession())
                CollectionConfig.OverlayText.COLL_PER_HOUR -> addIfNotNull(list, handleCollectionPerHour())
                CollectionConfig.OverlayText.MONEY_PER_HOUR -> addIfNotNull(list, handleMoneyPerHour())
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
            if (TrackingRates.isNextWiped) return "Custom Position (${TrackingRates.nextRankUsername}-wiped): ${formatNumber(TrackingRates.nextRankAmount)}"
            return "Custom Position (${TrackingRates.nextRankUsername}): ${formatNumber(TrackingRates.nextRankAmount)}"
        }

        if (TrackingRates.nextRankUsername == null) return "Next Position: Calculating..."
        if (TrackingRates.isNextWiped) return "Next Position (${TrackingRates.nextRankUsername}-wiped): ${formatNumber(TrackingRates.nextRankAmount)}"
        return "Next Position (${TrackingRates.nextRankUsername}): ${formatNumber(TrackingRates.nextRankAmount)}"
    }

    private fun handleCollectionTillNextRank(): String? {
        if (isEmpty() || TrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (TrackingRates.collectionTillNextRank == -1L) return "Till Custom Position: Calculating..."
            return "Till Custom Position: ${formatNumber(TrackingRates.collectionTillNextRank)}"
        }

        if (TrackingRates.collectionTillNextRank == -1L) return "Till Next Position: Calculating..."
        return "Till Next Position: ${formatNumber(TrackingRates.collectionTillNextRank)}"
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
        if (TrackingRates.isPreviousWiped) return "Passed (${TrackingRates.previousRankUsername}-wiped): ${formatNumber(TrackingRates.previousRankAmount)}"
        return "Passed (${TrackingRates.previousRankUsername}): ${formatNumber(TrackingRates.previousRankAmount)}"
    }

    private fun handleCollectionAbovePreviousRank(): String? {
        if (isEmpty() || !previousPosition) return null

        if (TrackingRates.collectionAbovePreviousRankAmount == -1L) return "Difference: Calculating..."
        return "Difference: ${formatNumber(TrackingRates.collectionAbovePreviousRankAmount)}"
    }

    private fun handleMultiNextPosition(): String? {
        if (isEmpty() || MultiTrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (MultiTrackingRates.nextRankAmount == -1L) return "Custom Position: Calculating..."
            if (MultiTrackingRates.isNextWiped) return "Custom Position (${MultiTrackingRates.nextRankUsername}-wiped): ${formatNumber(MultiTrackingRates.nextRankAmount)}"
            return "Custom Position (${MultiTrackingRates.nextRankUsername}): ${formatNumber(MultiTrackingRates.nextRankAmount)}"
        }

        if (MultiTrackingRates.nextRankUsername == null) return "Next Position: Calculating..."
        if (MultiTrackingRates.isNextWiped) return "Next Position (${MultiTrackingRates.nextRankUsername}-wiped): ${formatNumber(MultiTrackingRates.nextRankAmount)}"
        return "Next Position (${MultiTrackingRates.nextRankUsername}): ${formatNumber(MultiTrackingRates.nextRankAmount)}"
    }

    private fun handleMultiCollectionTillNextRank(): String? {
        if (isEmpty() || MultiTrackingRates.playerCurrentRank == 1) return null

        if (leaderboardPosition) {
            if (MultiTrackingRates.collectionTillNextRank == -1L) return "Till Custom Position: Calculating..."
            return "Till Custom Position: ${formatNumber(MultiTrackingRates.collectionTillNextRank)}"
        }

        if (MultiTrackingRates.collectionTillNextRank == -1L) return "Till Next Position: Calculating..."
        return "Till Next Position: ${formatNumber(MultiTrackingRates.collectionTillNextRank)}"
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
        if (MultiTrackingRates.isPreviousWiped) return "Passed (${MultiTrackingRates.previousRankUsername}-wiped): ${formatNumber(MultiTrackingRates.previousRankAmount)}"
        return "Passed (${MultiTrackingRates.previousRankUsername}): ${formatNumber(MultiTrackingRates.previousRankAmount)}"
    }

    private fun handleMultiCollectionAbovePreviousRank(): String? {
        if (isEmpty() || !previousPosition) return null

        if (MultiTrackingRates.collectionAbovePreviousRankAmount == -1L) return "Difference: Calculating..."
        return "Difference: ${formatNumber(MultiTrackingRates.collectionAbovePreviousRankAmount)}"
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
        return if (TrackingRates.collectionAmount >= 0)
            "${formatCollectionName(collection)} : ${formatNumber(TrackingRates.collectionAmount)}$rankSuffix"
        else
            "${formatCollectionName(collection)} : Calculating..."
    }

    private fun handleCollectionSession(): String =
        if (TrackingRates.collectionMade > 0)
            "${formatCollectionName(collection)} (session): ${formatNumber(TrackingRates.collectionMade)}"
        else
            "${formatCollectionName(collection)} (session): Calculating..."

    private fun handleCollectionPerHour(): String =
        if (TrackingRates.collectionPerHour > 0)
            "Coll/h: ${formatNumber(TrackingRates.collectionPerHour)}"
        else
            "Coll/h: Calculating..."

    private fun handleMoneyPerHour(): String? {
        if (collectionType == null) return null // no collection type (probably rift collection)

        val hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0

        if (!useBazaar && hasNpcPrice) {
            if (!TrackingRates.moneyMade.containsKey("NPC")) {
                return "$/h (NPC): Calculating..."
            }

            val localMoneyPerHourNPC: Long = TrackingRates.moneyPerHourNPC
            if (CollectionsManager.isRiftCollection(collection)) {
                // Use motes instead of money for rift collections
                return "Motes/h: ${formatNumberOrPlaceholder(localMoneyPerHourNPC)}"
            }
            return "$/h (NPC): ${formatNumberOrPlaceholder(localMoneyPerHourNPC)}"
        }

        if (!useBazaar) return null

        val localMoneyPerHour: Long
        val suffix = bazaarPriceTypeSuffix()
        when (collectionType) {
            "normal" -> {
                localMoneyPerHour = TrackingRates.moneyPerHourBazaar.getOrDefault("$collectionType$suffix", 0L)
                return "$/h (Bazaar): ${formatNumberOrPlaceholder(localMoneyPerHour)}"
            }

            "enchanted" -> {
                if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) {
                    localMoneyPerHour = TrackingRates.moneyPerHourBazaar.getOrDefault("Enchanted version$suffix", 0L)
                    return "$/h (Bazaar): ${formatNumberOrPlaceholder(localMoneyPerHour)}"
                } else {
                    localMoneyPerHour = TrackingRates.moneyPerHourBazaar.getOrDefault("Super Enchanted version$suffix", -1L)
                    if (localMoneyPerHour == -1L) {
                        setBazaarType(Bazaar.BazaarType.ENCHANTED_VERSION)
                        return null
                    } else return "$/h (Bazaar): ${formatNumberOrPlaceholder(localMoneyPerHour)}"
                }
            }

            "gemstone" -> {
                localMoneyPerHour = TrackingRates.moneyPerHourBazaar.getOrDefault("$gemstoneVariant$suffix", 0L)
                return "$/h (Bazaar): ${formatNumberOrPlaceholder(localMoneyPerHour)}"
            }

            else -> return null
        }
    }

    private fun handleMoneyMade(): String? {
        if (collectionType == null) return null // no collection type (probably rift collection)

        val hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0

        if (!useBazaar && hasNpcPrice) {
            if (!TrackingRates.moneyMade.containsKey("NPC")) {
                return "$/h (NPC): Calculating..."
            }

            val localMoneyMadeNPC = TrackingRates.moneyMade.getOrDefault("NPC", 0L)
            if (CollectionsManager.isRiftCollection(collection)) {
                // Use motes instead of money for rift collections
                return "Motes made: ${formatNumberOrPlaceholder(localMoneyMadeNPC)}"
            }
            return "$ made (NPC): ${formatNumberOrPlaceholder(localMoneyMadeNPC)}"
        }

        if (!useBazaar) return null

        val localMoneyMade: Long
        val suffix = bazaarPriceTypeSuffix()
        when (collectionType) {
            "normal" -> {
                localMoneyMade = TrackingRates.moneyMade.getOrDefault("$collectionType$suffix", 0L)
                return "$ made (Bazaar): ${formatNumberOrPlaceholder(localMoneyMade)}"
            }

            "enchanted" -> {
                if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) {
                    localMoneyMade = TrackingRates.moneyMade.getOrDefault("Enchanted version$suffix", 0L)
                    return "$ made (Bazaar): ${formatNumberOrPlaceholder(localMoneyMade)}"
                } else {
                    localMoneyMade = TrackingRates.moneyMade.getOrDefault("Super Enchanted version$suffix", -1L)
                    if (localMoneyMade == -1L) {
                        setBazaarType(Bazaar.BazaarType.ENCHANTED_VERSION)
                        return null
                    } else return "$ made (Bazaar): ${formatNumberOrPlaceholder(localMoneyMade)}"
                }
            }

            "gemstone" -> {
                localMoneyMade = TrackingRates.moneyMade.getOrDefault("$gemstoneVariant$suffix", 0L)
                return "$ made (Bazaar): ${formatNumberOrPlaceholder(localMoneyMade)}"
            }

            else -> return null
        }
    }

    private fun handleCollectionSinceLast(): String =
        if (TrackingRates.collectionSinceLast > 0)
            "${formatCollectionName(collection)} since last: ${formatNumber(TrackingRates.collectionSinceLast)}"
        else
            "${formatCollectionName(collection)} since last: Calculating..."

    private fun handleCollectionSinceLastTimer(): String {
        val totalSeconds: Long = (System.currentTimeMillis() - TrackingRates.lastCollectionTime) / 1000
        return "Last updated: ${StringUtils.formatCompactTime(totalSeconds)} ago"
    }

    // Only if it has bazaar data and is enabled
    fun updateTrackingExtraLines(list: MutableList<String>) {
        list.add("§6§lExtra Stats:")
        for (id in extraStatsText) {
            when (id) {
                CollectionConfig.OverlayExtraText.BAZAAR_PRICE_TYPE -> addIfNotNull(list, handleBazaarPriceType())
                CollectionConfig.OverlayExtraText.BAZAAR_ITEM -> addIfNotNull(list, handleBazaarItem())
                CollectionConfig.OverlayExtraText.BAZAAR_PRICE -> addIfNotNull(list, handleBazaarPrice())
            }
        }
    }

    private fun handleBazaarPriceType(): String =
        when (bazaarPriceType) {
            Bazaar.BazaarPriceType.INSTANT_BUY -> "Price type: Instant Buy"
            Bazaar.BazaarPriceType.INSTANT_SELL -> "Price type: Instant Sell"
        }

    private fun handleBazaarItem(): String? {
        when (collectionType) {
            "enchanted" -> {
                if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) {
                    return "Bazaar item: ${formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keys.iterator().next())}"
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        setBazaarType(Bazaar.BazaarType.ENCHANTED_VERSION)
                        return null
                    } else return "Bazaar item: ${formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keys.iterator().next())}"
                }
            }
            "gemstone" -> return "Bazaar variant: $gemstoneVariant"
            else -> return null
        }
    }

    private fun handleBazaarPrice(): String? {
        when (collectionType) {
            "enchanted" -> {
                if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) {
                    val price =
                        if (bazaarPriceType == Bazaar.BazaarPriceType.INSTANT_BUY) BazaarPrices.enchantedInstantBuy else BazaarPrices.enchantedInstantSell
                    if (price == 0f) {
                        return "Item price: Unknown price"
                    }
                    return "Item price: ${formatNumber(price.toLong())}"
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        setBazaarType(Bazaar.BazaarType.ENCHANTED_VERSION)
                        return null
                    } else {
                        val price =
                            if (bazaarPriceType == Bazaar.BazaarPriceType.INSTANT_BUY) BazaarPrices.superEnchantedInstantBuy else BazaarPrices.superEnchantedInstantSell
                        if (price == 0f) {
                            return "Item price: Unknown price"
                        }
                        return "Item price: ${formatNumber(price.toLong())}"
                    }
                }
            }

            "gemstone" -> {
                val price = if (bazaarPriceType == Bazaar.BazaarPriceType.INSTANT_BUY) GemstonePrices.getInstantBuyPrice(gemstoneVariant.toString())
                else GemstonePrices.getInstantSellPrice(gemstoneVariant.toString())

                if (price == 0f) return "Variant price: Unknown price"

                return "Variant price: ${formatNumber(price.toLong())}"
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

                if (mainExpanded) {
                    list.add("${prefix}Gemstones: ")

                    GemstonePrices.multiGemstoneRecipes.forEach { (type: String?) ->
                        if (MultiTrackingRates.seenGemstones.contains(type)) {
                            val line = when (trackingOptions) {
                                MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleCollectionPerHourMulti(type!!)
                                MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleCollectionSessionMulti(type!!)
                                MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMoneyPerHourMulti(type!!)
                                MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMoneyMadeMulti(type!!)
                            }
                            list.add("  $line")
                        }
                    }
                } else {
                    val line = when (trackingOptions) {
                        MultiCollectionConfig.TrackingOptions.COLLECTION -> handleCollectionMulti("gemstone")
                        MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleCollectionPerHourMulti("gemstone")
                        MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleCollectionSessionMulti("gemstone")
                        MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMoneyPerHourMulti("gemstone")
                        MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMoneyMadeMulti("gemstone")
                    }
                    list.add("$prefix$line")
                }
                continue
            }

            val line = when (trackingOptions) {
                MultiCollectionConfig.TrackingOptions.COLLECTION -> handleCollectionMulti(coll)
                MultiCollectionConfig.TrackingOptions.COLLECTION_RATE -> handleCollectionPerHourMulti(coll)
                MultiCollectionConfig.TrackingOptions.COLLECTION_MADE -> handleCollectionSessionMulti(coll)
                MultiCollectionConfig.TrackingOptions.MONEY_RATE -> handleMoneyPerHourMulti(coll)
                MultiCollectionConfig.TrackingOptions.MONEY_MADE -> handleMoneyMadeMulti(coll)
            }
            list.add(line)
        }

        handleMultiLeaderboard(list)

        val suffix = bazaarPriceTypeSuffix()
        val typeKey = bazaarTypeKey()
        val variant = gemstoneVariant.toString()

        val useBazaar = useBazaar

        when (trackingOptions) {
            MultiCollectionConfig.TrackingOptions.MONEY_RATE -> {
                if (!useBazaar) {
                    val total = MultiTrackingRates.moneyPerHourNPC.entries
                        .filter { (key, value) -> value > 0 && (!key.contains('_') || key.endsWith("_$variant")) }
                        .sumOf { it.value }
                    list.add("")
                    if (CollectionsManager.hasAllRiftCollections()) list.add("§eOverall Motes/h: ${formatNumber(total)}")
                    else list.add("§eOverall $/h (NPC): ${formatNumber(total)}")
                } else {
                    val total = MultiTrackingRates.moneyPerHourBazaar.entries
                        .filter { (key, value) -> value > 0 && key.endsWith(suffix) }
                        .filter { (key) -> key.contains("_normal") || key.contains("_$typeKey") || key.contains("_$variant") }
                        .sumOf { it.value }
                    list.add("")
                    list.add("§eOverall $/h (Bazaar): ${formatNumber(total)}")
                }
            }

            MultiCollectionConfig.TrackingOptions.MONEY_MADE -> {
                if (!useBazaar) {
                    val total = MultiTrackingRates.moneyMadeNPC.entries
                        .filter { (key, value) -> value > 0 && (!key.contains('_') || key.endsWith("_$variant")) }
                        .sumOf { it.value }
                    list.add("")
                    if (CollectionsManager.hasAllRiftCollections()) list.add("§eOverall Motes made: ${formatNumber(total)}")
                    else list.add("§eOverall $ made (NPC): ${formatNumber(total)}")
                } else {
                    val total = MultiTrackingRates.moneyMadeBazaar.entries
                        .filter { (key, value) -> value > 0 && (key.endsWith(suffix)) }
                        .filter { (key) -> key.contains("_normal") || key.contains("_$typeKey") || key.contains("_$variant") }
                        .sumOf { it.value }
                    list.add("")
                    list.add("§eOverall $ made (Bazaar): ${formatNumber(total)}")
                }
            }
            else -> {}
        }

        list.add("")
        list.add(handleMultiCollectionSinceLastTimer())
    }

    private fun handleCollectionMulti(coll: String): String {
        var rankSuffix = ""
        if ("gemstone" == coll && collectionLeaderboard && MultiTrackingRates.playerCurrentRank != -1) {
            rankSuffix = if (MultiTrackingRates.playerCurrentRank == 10001) " [Too low]"
            else " [#${MultiTrackingRates.playerCurrentRank}]"
        }
        return if (MultiTrackingRates.collectionAmounts.getOrDefault(coll, -1L) >= 0)
            "${formatCollectionName(coll)}: ${formatNumber(MultiTrackingRates.collectionAmounts.getOrDefault(coll, 0L))}$rankSuffix"
        else
            "${formatCollectionName(coll)}: Calculating..."
    }

    private fun handleCollectionSessionMulti(coll: String): String =
        if (MultiTrackingRates.collectionMade.getOrDefault(coll, -1L) > 0)
            "${formatCollectionName(coll)} (session): ${formatNumber(MultiTrackingRates.collectionMade.getOrDefault(coll, 0L))}"
        else
            "${formatCollectionName(coll)} (session): Calculating..."

    private fun handleCollectionPerHourMulti(coll: String): String =
        if (MultiTrackingRates.collectionPerHour.getOrDefault(coll, -1L) > 0)
            "${formatCollectionName(coll)} Coll/h: ${formatNumber(MultiTrackingRates.collectionPerHour.getOrDefault(coll, 0L))}"
        else
            "${formatCollectionName(coll)} Coll/h: Calculating..."

    private fun handleMoneyPerHourMulti(coll: String): String {
        val useBazaar = useBazaar
        val suffix = bazaarPriceTypeSuffix()
        val variant = gemstoneVariant.toString()

        if ("gemstone" == coll) {
            var totalRate: Long = 0

            for (gem in MultiTrackingRates.seenGemstones) {
                totalRate += if (useBazaar) {
                    MultiTrackingRates.moneyPerHourBazaar.getOrDefault("${gem}_${variant}".uppercase() + suffix, 0L)
                } else {
                    MultiTrackingRates.moneyPerHourNPC.getOrDefault("${gem}_${variant}".uppercase(), 0L)
                }
            }
            return "Gemstone $/h (${if (useBazaar) "Bazaar" else "NPC"}): ${formatNumberOrPlaceholder(totalRate)}"
        }

        if (!useBazaar) {
            var key: String = coll
            if (MultiTrackingRates.seenGemstones.contains(coll)) {
                val variant = gemstoneVariant.toString()
                key = (coll + "_" + variant).uppercase()
            }

            val npcRate = MultiTrackingRates.moneyPerHourNPC.getOrDefault(key, -1L)
            if (CollectionsManager.isRiftCollection(coll)) {
                return "${formatCollectionName(coll)} Motes/h: ${formatNumberOrPlaceholder(npcRate)}"
            }
            return "${formatCollectionName(coll)} $/h (NPC): ${formatNumberOrPlaceholder(npcRate)}"
        } else {
            var actualColl: String = coll
            var gemstoneVariant: String? = null

            if (MultiTrackingRates.seenGemstones.contains(coll)) {
                actualColl = "gemstone"
                gemstoneVariant = ("${coll}_${variant}").uppercase()
            }

            if (gemstoneVariant != null) {
                val rate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault(gemstoneVariant + suffix, 0L)
                return "${formatCollectionName(coll)} $/h (Bazaar): ${formatNumberOrPlaceholder(rate)}"
            }

            val type = CollectionsManager.multiCollectionTypes[actualColl]

            if (type != null) {
                var rate: Long = 0
                when (type) {
                    "normal" -> rate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault("${actualColl}_normal$suffix", 0L)
                    "enchanted" -> rate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault("${actualColl}_${bazaarTypeKey()}$suffix", 0L)
                }

                return "${formatCollectionName(coll)} $/h (Bazaar): ${formatNumberOrPlaceholder(rate)}"
            } else {
                return "${formatCollectionName(coll)} $/h (Bazaar): Calculating..."
            }
        }
    }

    private fun handleMoneyMadeMulti(coll: String): String {
        val useBazaar = useBazaar
        val suffix = bazaarPriceTypeSuffix()
        val variant = gemstoneVariant.toString()

        if ("gemstone" == coll) {
            var totalMoney: Long = 0

            for (gem in MultiTrackingRates.seenGemstones) {
                totalMoney += if (useBazaar) {
                    MultiTrackingRates.moneyMadeBazaar.getOrDefault(("${gem}_$variant").uppercase() + suffix, 0L)
                } else {
                    MultiTrackingRates.moneyMadeNPC.getOrDefault(("${gem}_$variant").uppercase(), 0L)
                }
            }
            return "Gemstone $ made (${if (useBazaar) "Bazaar" else "NPC"}): ${formatNumberOrPlaceholder(totalMoney)}"
        }

        if (!useBazaar) {
            var key: String = coll
            if (MultiTrackingRates.seenGemstones.contains(coll)) {
                val variant: String = gemstoneVariant.toString()
                key = (coll + "_" + variant).uppercase()
            }

            val npcMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(key, -1L)
            if (CollectionsManager.isRiftCollection(coll)) {
                return "${formatCollectionName(coll)} Motes made: ${formatNumberOrPlaceholder(npcMoney)}"
            }
            return "${formatCollectionName(coll)} $ made (NPC): ${formatNumberOrPlaceholder(npcMoney)}"
        } else {
            var actualColl: String = coll
            var gemstoneVariant: String? = null

            if (MultiTrackingRates.seenGemstones.contains(coll)) {
                actualColl = "gemstone"
                gemstoneVariant = ("${coll}_$variant").uppercase()
            }

            if (gemstoneVariant != null) {
                val money = MultiTrackingRates.moneyMadeBazaar.getOrDefault("$gemstoneVariant$suffix", 0L)
                return "${formatCollectionName(coll)} $ made (Bazaar): ${formatNumberOrPlaceholder(money)}"
            }

            val type = CollectionsManager.multiCollectionTypes[actualColl]

            if (type != null) {
                var money: Long = 0
                when (type) {
                    "normal" -> money = MultiTrackingRates.moneyMadeBazaar.getOrDefault("${actualColl}_normal$suffix", 0L)
                    "enchanted" -> money = MultiTrackingRates.moneyMadeBazaar.getOrDefault("${actualColl}_${bazaarTypeKey()}$suffix", 0L)
                }

                return "${formatCollectionName(coll)} $ made (Bazaar): ${formatNumberOrPlaceholder(money)}"
            } else {
                return "${formatCollectionName(coll)} $ made (Bazaar): Calculating..."
            }
        }
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
        val totalSeconds: Long = (System.currentTimeMillis() - MultiTrackingRates.lastCollectionUpdate) / 1000
        return "Last updated: ${StringUtils.formatCompactTime(totalSeconds)} ago"
    }

    fun addToggleableSettingsLines(list: MutableList<String>) {
        list.add("")
        list.add("§e${(if (useBazaar) "[Bazaar Prices]" else "[NPC Prices]")}")

        if (useBazaar) {
            if (collectionList.contains("gemstone") || GemstonesManager.checkIfGemstone(collection)) {
                list.add("§e[$gemstoneVariant]")
            }
            if ("enchanted" == collectionType || CollectionsManager.multiCollectionTypes.containsValue("enchanted")) {
                if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) {
                    list.add("§e[Enchanted version]")
                } else {
                    list.add("§e[Super Enchanted version]")
                }
            }
            list.add("§e[Instant ${(if (bazaarPriceType == Bazaar.BazaarPriceType.INSTANT_BUY) "Buy" else "Sell")}]")

            if (TrackingHandler.isTracking) {
                list.add("${(if (showExtraStats) "§a" else "§e")}[Extra Stats]")
            } else {
                val option = trackingOptions.toString()
                list.add("§e[$option Stats]")
            }
        }
    }

    fun bazaarPriceTypeSuffix(): String =
        when (bazaarPriceType) {
            Bazaar.BazaarPriceType.INSTANT_BUY -> "_INSTANT_BUY"
            Bazaar.BazaarPriceType.INSTANT_SELL -> "_INSTANT_SELL"
        }

    fun bazaarTypeKey(): String =
        when (bazaarType) {
            Bazaar.BazaarType.ENCHANTED_VERSION -> "Enchanted version"
            Bazaar.BazaarType.SUPER_ENCHANTED_VERSION -> "Super Enchanted version"
        }
}
