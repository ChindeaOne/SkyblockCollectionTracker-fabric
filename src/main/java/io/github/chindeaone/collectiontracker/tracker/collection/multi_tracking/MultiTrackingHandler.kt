package io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.gui.overlays.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiDataFetcher.clearCache
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Hypixel.server
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils.formatCollectionName
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit

object MultiTrackingHandler  {

    private val logger: Logger = LogManager.getLogger(MultiTrackingHandler::class.java)
    @JvmStatic
    val COOLDOWN_MILLIS: Long = TimeUnit.SECONDS.toMillis(10) // 10 seconds cooldown

    @Volatile
    @JvmStatic
    var isMultiTracking = false
    @JvmStatic
    var isMultiPaused = false

    @JvmStatic
    var multiStartTime: Long = 0
    @JvmStatic
    var multiLastTime: Long = 0
    @JvmStatic
    var multiLastTrackTime: Long = 0

    private const val RESETS: Int = 10
    private var multiRestartCount: Int = 0
    private var multiFirstRestartTime: Long = 0

    @JvmStatic
    fun startMultiTracking() {
        initMultiTracking()
        OverlayManager.setMultiTrackingOverlayRendering(true)
        logger.info("[SCT]: Starting multi tracking for player ${PlayerData.playerName}")

        MultiDataFetcher.fetchMultiCollectionData()
    }

    private fun initMultiTracking() {
        val now = System.currentTimeMillis()
        multiStartTime = now
        multiLastTrackTime = now
        multiLastTime = 0

        isMultiTracking = true
        isMultiPaused = false
    }

    @JvmStatic
    fun stopMultiTrackingManual() {
        if (isMultiTracking) {
            sendMultiRates()
            sendMessage("§cStopped multi-tracking!", true)

            resetMultiTrackingData(false)

            logger.info("[SCT]: Multi-tracking stopped.")
        } else {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to stop multi-tracking manually, but no multi-tracking is active.")
        }
    }

    @JvmStatic
    fun stopMultiTracking() {
        if (isMultiTracking) {
            if (!server) {
                logger.info("[SCT]: Multi-tracking stopped because player disconnected from the server.")
            } else {
                sendMessage("§cAPI server is down. Stopping multi-tracker.", true)
                logger.info("[SCT]: Multi-tracking stopped because the API server is down.")
            }

            resetMultiTrackingData(false)
        } else {
            logger.warn("[SCT]: Attempted to stop multi-tracking, but no multi-tracking is active.")
        }
    }

    @JvmStatic
    fun restartMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active to restart!", true)
            logger.warn("[SCT]: Attempted to restart multi-tracking, but no multi-tracking is active.")
            return
        }

        if (multiRestartCount == 0) {
            multiFirstRestartTime = System.currentTimeMillis()
        } else {
            val elapsedTime = System.currentTimeMillis() - multiFirstRestartTime
            if (elapsedTime >= TimeUnit.HOURS.toMillis(1)) {
                multiRestartCount = 0
                multiFirstRestartTime = System.currentTimeMillis()
            }
        }

        if (multiRestartCount >= RESETS) {
            sendMessage("§cHourly restart limit reached! Cannot restart multi-tracking.", true)
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart multi-tracking.")
            return
        }

        multiRestartCount++
        resetMultiTrackingData(true)
        startMultiTracking()
    }

    private fun resetMultiTrackingData(restart: Boolean) {
        resetVariables()
        clearCache()

        val now = System.currentTimeMillis()
        if (!restart) {
            multiLastTrackTime = now
            clearFetchedData()
        } else multiLastTrackTime = now - COOLDOWN_MILLIS

        OverlayManager.setMultiTrackingOverlayRendering(false)
        MultiCollectionOverlay.trackingDirty = false
    }

    private fun clearFetchedData() {
        CollectionsManager.resetMultiCollections()
        BazaarCollectionsManager.resetBazaarData()
    }

    private fun resetVariables() {
        isMultiTracking = false
        isMultiPaused = false
        multiStartTime = 0
        multiLastTime = 0

        clearMaps()
    }

    fun clearMaps() {
        MultiTrackingRates.collectionAmounts.clear()
        MultiTrackingRates.collectionPerHour.clear()
        MultiTrackingRates.collectionMade.clear()
        MultiTrackingRates.collectionSinceLast.clear()
        MultiTrackingRates.sessionStartCollections.clear()
        MultiTrackingRates.lastCollectionTimes.clear()
        MultiTrackingRates.lastApiCollections.clear()
        MultiTrackingRates.moneyPerHourNPC.clear()
        MultiTrackingRates.moneyMadeNPC.clear()
        MultiTrackingRates.moneyMadeBazaar.clear()
        MultiTrackingRates.moneyPerHourBazaar.clear()
        MultiTrackingRates.seenGemstones.clear()
    }

    @JvmStatic
    fun pauseMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to pause multi-tracking, but no multi-tracking is active.")
        }

        if (isMultiPaused) {
            sendMessage("§cMulti-tracking is already paused!", true)
            logger.warn("[SCT]: Attempted to pause multi-tracking, but multi-tracking is already paused.")
            return
        }
        isMultiPaused = true
        multiLastTime += (System.currentTimeMillis() - multiStartTime) / 1000
        sendMessage("§7Multi-tracking paused.", true)
        logger.info("[SCT]: Multi-tracking paused.")

    }

    @JvmStatic
    fun resumeMultiTracking() {
        if (!isMultiTracking) {
            sendMessage("§cNo multi-tracking active!", true)
            logger.warn("[SCT]: Attempted to resume multi-tracking, but no multi-tracking is active.")
            return
        }

        if (!isMultiPaused) {
            sendMessage("§cMulti-tracking is already active!", true)
            logger.warn("[SCT]: Attempted to resume multi-tracking, but multi-tracking is already active.")
            return
        }

        isMultiPaused = false
        multiStartTime = System.currentTimeMillis()
        sendMessage("§7Resuming multi-tracking.", true)
        logger.info("[SCT]: Resuming multi-tracking.")
    }

    fun getMultiUptimeInSeconds(): Long {
        if (multiStartTime == 0L) {
            return 0
        }

        return if (isMultiPaused) {
            multiLastTime
        } else {
            multiLastTime + (System.currentTimeMillis() - multiStartTime) / 1000
        }
    }

    private fun getMultiUptimeInWords(): String {
        if (multiStartTime == 0L) return "0 seconds"

        val uptime: Long = multiLastTime + (System.currentTimeMillis() - multiStartTime) / 1000

        val hours = uptime / 3600
        val minutes = (uptime % 3600) / 60
        val seconds = uptime % 60

        return when {
            hours == 1L -> "$hours hour $minutes minutes $seconds seconds"
            hours > 0 -> "$hours hours $minutes minutes $seconds seconds"
            minutes == 1L -> "$minutes minute $seconds seconds"
            minutes > 1 -> "$minutes minutes $seconds seconds"
            seconds == 1L -> "$seconds second"
            else -> "$seconds seconds"
        }
    }

    @JvmStatic
    fun getMultiUptime(): String {
        if (multiStartTime == 0L) return "00:00:00"

        val uptime: Long = if (isMultiPaused) {
            multiLastTime
        } else {
            multiLastTime + (System.currentTimeMillis() - multiStartTime) / 1000
        }

        val hours = uptime / 3600
        val minutes = (uptime % 3600) / 60
        val seconds = uptime % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @JvmStatic
    fun sendMultiRates() {
        if (!ConfigAccess.isMultiTrackingSummaryEnabled()) return

        val lines = mutableListOf<Component>()
        val useBazaar = ConfigAccess.isUsingBazaar()
        val variant = ConfigAccess.getGemstoneVariant().toString()
        val suffix = if (ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY) "_INSTANT_BUY" else "_INSTANT_SELL"
        val bazaarSuffix = if (useBazaar) if (suffix.contains("BUY")) "Instant Buy" else "Instant Sell" else ""
        val bazaarType = if (ConfigAccess.getBazaarType() == Bazaar.BazaarType.ENCHANTED_VERSION) "Enchanted version" else "Super Enchanted version"

        var totalMoneyMade = 0L
        var totalMoneyRate = 0L

        var totalGemstoneMoneyMade = 0L
        var totalGemstoneMoneyRate = 0L

        val trackedCollections = CollectionTracker.collectionList
        val collectionLines = mutableListOf<Component>()

        // Non gemstone collections
        for (coll in trackedCollections) {
            if (coll == "gemstone") continue

            val collName = formatCollectionName(coll)
            val formattedName = ColorUtils.collToColor(collName)
            var currentMoney: Long
            var currentRate: Long

            if (!useBazaar) {
                currentMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(coll, 0L)
                currentRate = MultiTrackingRates.moneyPerHourNPC.getOrDefault(coll, 0L)
            } else {
                val type = CollectionsManager.multiCollectionTypes[coll]
                val key = when (type) {
                    "normal" -> "${coll}_normal$suffix"
                    "enchanted" -> "${coll}_${bazaarType}$suffix"
                    else -> ""
                }
                currentMoney = MultiTrackingRates.moneyMadeBazaar.getOrDefault(key, 0L)
                currentRate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault(key, 0L)
            }

            totalMoneyMade += currentMoney
            totalMoneyRate += currentRate

            val line = Component.literal("   ").append(formattedName).append("§r: ")

            when (ConfigAccess.getSummaryStats().name) {
                "COLLECTION" -> line.append("§f${formatNumber(MultiTrackingRates.collectionMade[coll] ?: 0L)} §7(${formatNumber(MultiTrackingRates.collectionPerHour[coll] ?: 0L)}/h)")
                "MONEY" -> line.append("§a$${formatNumber(currentMoney)} §7($${formatNumber(currentRate)}/h)")
                "BOTH" -> line.append("§f${formatNumber(MultiTrackingRates.collectionMade[coll] ?: 0L)} §7(${formatNumber(MultiTrackingRates.collectionPerHour[coll] ?: 0L)}/h)   §a$${formatNumber(currentMoney)} §7($${formatNumber(currentRate)}/h)")
                else -> continue
            }
            collectionLines.add(line)
        }

        // gemstones
        val gemstoneLines = mutableListOf<Component>()
        if (trackedCollections.contains("gemstone")) {
            for (gemstone in MultiTrackingRates.seenGemstones) {
                val name = formatCollectionName(gemstone)
                val formattedName = ColorUtils.collToColor(name)
                var gemMoney: Long
                var gemRate: Long

                val keyPrefix = (gemstone + "_" + variant).uppercase()
                if (!useBazaar) {
                    gemMoney = MultiTrackingRates.moneyMadeNPC.getOrDefault(keyPrefix, 0L)
                    gemRate = MultiTrackingRates.moneyPerHourNPC.getOrDefault(keyPrefix, 0L)
                } else {
                    val key = keyPrefix + suffix
                    gemMoney = MultiTrackingRates.moneyMadeBazaar.getOrDefault(key, 0L)
                    gemRate = MultiTrackingRates.moneyPerHourBazaar.getOrDefault(key, 0L)
                }

                totalGemstoneMoneyMade += gemMoney
                totalGemstoneMoneyRate += gemRate

                val line = Component.literal("    - ").append(formattedName).append(": ")

                when (ConfigAccess.getSummaryStats().name) {
                    "COLLECTION" -> {
                        val amount = MultiTrackingRates.collectionMade[gemstone] ?: 0L
                        val rate = MultiTrackingRates.collectionPerHour[gemstone] ?: 0L
                        line.append("§f${formatNumber(amount)} §7(${formatNumber(rate)}/h)")
                    }
                    "MONEY" -> line.append("§a$${formatNumber(gemMoney)} §7($${formatNumber(gemRate)}/h)")
                    "BOTH" -> {
                        val amount = MultiTrackingRates.collectionMade[gemstone] ?: 0L
                        val rate = MultiTrackingRates.collectionPerHour[gemstone] ?: 0L
                        line.append("§f${formatNumber(amount)} §7(${formatNumber(rate)}/h)   §a$${formatNumber(gemMoney)} §7($${formatNumber(gemRate)}/h)")
                    }
                }
                gemstoneLines.add(line)
            }

            totalMoneyMade += totalGemstoneMoneyMade
            totalMoneyRate += totalGemstoneMoneyRate
        }

        if (ConfigAccess.getSummaryStats().name == "MONEY" || ConfigAccess.getSummaryStats().name == "BOTH") {
            if (totalMoneyMade > 0) {
                lines.add(
                    Component.literal("   §eTotal Profit: §f$${formatNumber(totalMoneyMade)}   §eRate: §f$${formatNumber(totalMoneyRate)}/h   §ePricing:§f" + if (useBazaar) " $bazaarSuffix" else " NPC")
                )
                lines.add(Component.empty())
            }
        }
        lines.addAll(collectionLines)

        val summaryLine = Component.literal("   §dGemstones (${variant.lowercase()}): ")
        when (ConfigAccess.getSummaryStats().name) {
            "COLLECTION" -> {
                val totalColl = MultiTrackingRates.collectionMade["gemstone"] ?: 0L
                val totalRate = MultiTrackingRates.collectionPerHour["gemstone"] ?: 0L
                summaryLine.append("§f${formatNumber(totalColl)} §7(${formatNumber(totalRate)}/h)")
            }
            "MONEY" -> summaryLine.append("§a$${formatNumber(totalGemstoneMoneyMade)} §7($${formatNumber(totalGemstoneMoneyRate)}/h)")
            "BOTH" -> {
                val totalColl = MultiTrackingRates.collectionMade["gemstone"] ?: 0L
                val totalRate = MultiTrackingRates.collectionPerHour["gemstone"] ?: 0L
                summaryLine.append("§f${formatNumber(totalColl)} §7(${formatNumber(totalRate)}/h)   §a$${formatNumber(totalGemstoneMoneyMade)} §7($${formatNumber(totalGemstoneMoneyRate)}/h)")
            }
        }

        lines.add(summaryLine)
        if (ConfigAccess.isMultiDetailedSummaryEnabled()) {
            lines.addAll(gemstoneLines)
        }

        lines.add(Component.empty())
        lines.add(Component.literal("   §7Elapsed time: §f${getMultiUptimeInWords()}"))

        ChatUtils.sendSummary("§e§lMulti-Tracking Summary", lines)
    }
}