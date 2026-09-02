package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping

object StatsWidget {

    var rawStats: List<String> = emptyList()
    private var lastStats: List<String>? = null

    var rawBeaconStats: List<String> = emptyList()
    var rawStarbornTempleStats: String = ""
    private var lastBeaconStats: List<String>? = null
    private var lastStarbornTempleStats: List<String>? = null

    fun onTabUpdate() {
        val widget = TabWidget.STATS

        if (IslandTracker.currentMiningIsland == null && IslandTracker.currentForagingIsland == null) {
            clearStats()
            return
        }

        val beaconWidget = when {
            IslandTracker.isInMoongladeMarsh && !IslandTracker.isInPark -> TabWidget.MOONGLADE_BEACON
            !IslandTracker.isInMoongladeMarsh && !IslandTracker.isInPark -> TabWidget.TORRHUS_BEACON
            else -> null
        }
        val starbornTempleWidget = if (IslandTracker.isInMoongladeMarsh) TabWidget.STARBORN_TEMPLE else null

        val hasMiningStats = widget.lines.any { line ->
            MiningMapping.miningStats.any { stat -> line.contains(stat, ignoreCase = true) }
        }

        val hasForagingStats = widget.lines.any { line ->
            ForagingMapping.foragingStats.any { stat -> line.contains(stat, ignoreCase = true) }
        }

        if (!hasMiningStats && !hasForagingStats) {
            clearStats()
            return
        }

        val currentRaw = TabData.parseWidgetData(widget.lines)
        if (currentRaw == null || currentRaw == lastStats) return

        val currentBeaconRaw = beaconWidget?.lines?.let { TabData.parseWidgetData(it) }
        val currentStarbornTempleRaw = starbornTempleWidget?.lines?.let { TabData.parseWidgetData(it) }

        rawStats = currentRaw
        lastStats = currentRaw

        rawBeaconStats = currentBeaconRaw ?: emptyList()
        lastBeaconStats = currentBeaconRaw

        rawStarbornTempleStats = currentStarbornTempleRaw.toString()
        lastStarbornTempleStats = currentStarbornTempleRaw
    }

    fun clearStats() {
        rawStats = emptyList()
        lastStats = null

        rawBeaconStats = emptyList()
        lastBeaconStats = null

        rawStarbornTempleStats = ""
        lastStarbornTempleStats = null

        MiningStatsParser.clear()
        ForagingStatsParser.clear()
    }
}