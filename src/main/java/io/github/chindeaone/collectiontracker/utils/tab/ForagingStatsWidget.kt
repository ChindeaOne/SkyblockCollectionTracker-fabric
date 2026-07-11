package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker

object ForagingStatsWidget {

    private var lastStats: List<String>? = null
    private var lastBeaconStats: List<String>? = null
    private var lastStarbornTempleStats: List<String>? = null
    @JvmStatic
    var rawStats: List<String> = emptyList()
    @JvmStatic
    var rawBeaconStats: List<String> = emptyList()
    @JvmStatic
    var rawStarbornTempleStats: String = ""

    @JvmStatic
    var currentForagingIsland: String? = null
        private set
    @JvmStatic
    var isInGalatea: Boolean = false

    private var firstInfoSeenTime: Long = 0L

    fun onTabWidgetsUpdate() {
        val now = System.currentTimeMillis()

        currentForagingIsland = IslandTracker.currentForagingIsland
        isInGalatea = IslandTracker.isInGalatea
        if (currentForagingIsland == null) {
            rawStats = emptyList()
            rawBeaconStats = emptyList()
            lastStats = null
            lastBeaconStats = null

            if (!isInGalatea) {
                rawStarbornTempleStats = ""
                lastStarbornTempleStats = null
            }
            return
        }

        if (!ConfigAccess.isForagingStatsOverlayEnabled()) {
            rawStats = emptyList()
            rawBeaconStats = emptyList()
            rawStarbornTempleStats = ""
            lastStats = null
            lastBeaconStats = null
            lastStarbornTempleStats = null
            return
        }

        val widget = TabWidget.STATS
        val beaconWidget = if (isInGalatea) TabWidget.MOONGLADE_BEACON else TabWidget.TORRHUS_BEACON
        val starbornTempleWidget = TabWidget.STARBORN_TEMPLE

        if (!widget.isPresent) {
            // avoid spamming messages when tab widgets are not visible
            if (!TabWidget.INFO.isPresent) {
                firstInfoSeenTime = 0L
                return
            }

            if (firstInfoSeenTime == 0L) {
                firstInfoSeenTime = now
            }

            if (now - firstInfoSeenTime < 5_000L) {
                return // Wait for the 5s buffer
            }

            // disable the overlay if the widget is not found
            ChatUtils.sendMessage("§cWarning: Foraging Stats widget not found. This can happen in low TPS lobbies. Please enable it using /widget or re-enable the foraging stats overlay config in your mod.", true)
            ConfigHelper.disableForagingStats()
            return
        }

        firstInfoSeenTime = 0L

        val currentRaw = TabData.parseWidgetData(widget.lines)
        val beaconDataRaw = TabData.parseWidgetData(beaconWidget.lines)
        val starbornTempleDataRaw = TabData.parseWidgetData(starbornTempleWidget.lines)

        if (currentRaw == lastStats && beaconDataRaw == lastBeaconStats && starbornTempleDataRaw == lastStarbornTempleStats) return

        if (currentRaw != null && currentRaw != lastStats) {
            rawStats = currentRaw
            lastStats = currentRaw
        }

        if (beaconDataRaw != null && beaconDataRaw != lastBeaconStats) {
            rawBeaconStats = beaconDataRaw
            lastBeaconStats = beaconDataRaw
        }

        if (starbornTempleDataRaw != null && starbornTempleDataRaw != lastStarbornTempleStats) {
            rawStarbornTempleStats = starbornTempleDataRaw.toString()
            lastStarbornTempleStats = starbornTempleDataRaw
        }
    }
}