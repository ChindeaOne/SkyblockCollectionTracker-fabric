package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener

import io.github.chindeaone.collectiontracker.utils.tab.TabWidget

object IslandTracker {

    private var currentIsland: String? = null

    var currentMiningIsland: String? = null
    var currentForagingIsland: String? = null
    var currentFarmingIsland: String? = null

    var isInPark: Boolean = false
    var isInMoongladeMarsh: Boolean = false

    var isInRift: Boolean = false

    private var wasReset: Boolean = false

    fun onTabUpdate() {
        val areaWidget = TabWidget.AREA
        if (!areaWidget.isPresent) {
            reset()
            return
        }

        val lines = areaWidget.lines
        if (lines.isEmpty()) return

        val detectedIsland = lines.toString()
            .trim('[', ']')
            .substringAfter("Area:")
            .trim()

        if (detectedIsland.isEmpty() || detectedIsland == currentIsland) return
        currentIsland = detectedIsland

        updateIslands(detectedIsland)
    }

    private fun updateIslands(island: String) {
        currentMiningIsland = island.takeIf { it in MiningMapping.miningAreas }

        WaypointsUtils.enableRoutes()
        onMineshaftEnter()

        currentForagingIsland = island.takeIf { it in ForagingMapping.foragingAreas }
        isInPark = currentForagingIsland == "The Park"
        isInMoongladeMarsh = currentForagingIsland == "Moonglade Marsh"

        currentFarmingIsland = island.takeIf { it in FarmingMapping.farmingAreas }

        updateRiftIsland(island)
    }

    fun isMiningIsland() = currentMiningIsland in MiningMapping.miningIslands

    fun isForagingIsland() = currentForagingIsland in ForagingMapping.foragingIslands

    private fun updateRiftIsland(island: String) {
        val currentlyInRift = island.equals("The Rift", ignoreCase = true)

        if (currentlyInRift == isInRift) return
        isInRift = currentlyInRift

        if (!isInRift) return

        TrackingHandler.resumeRiftTracking()
        MultiTrackingHandler.resumeMultiRiftTracking()
    }

    private fun onMineshaftEnter() {
        if (currentMiningIsland == "Mineshaft") {
            if (!wasReset) {
                ChatListener.resetPickaxeAbilities()
                wasReset = true
            }
        } else {
            wasReset = false
        }
    }

    fun reset() {
        currentIsland = null
        currentMiningIsland = null
        currentForagingIsland = null
        currentFarmingIsland = null
        isInPark = false
        isInMoongladeMarsh = false
        isInRift = false
    }
}