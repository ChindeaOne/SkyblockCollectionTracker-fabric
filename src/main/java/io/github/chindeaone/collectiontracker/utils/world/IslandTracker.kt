package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener

import io.github.chindeaone.collectiontracker.utils.tab.TabWidget

object IslandTracker {

    private var currentIsland: String? = null

    @JvmStatic var currentMiningIsland: String? = null
        private set

    @JvmStatic
    var currentForagingIsland: String? = null
        private set

    var currentFarmingIsland: String? = null
        private set

    var isInPark: Boolean = false
        private set

    var isInMoongladeMarsh: Boolean = false
        private set

    var isInRift: Boolean = false
        private set

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
        currentMiningIsland = MiningMapping.miningAreas.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }
        WaypointsUtils.enableRoutes()
        onMineshaftEnter()

        currentForagingIsland = ForagingMapping.foragingAreas.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }
        isInPark = currentForagingIsland == "The Park"
        isInMoongladeMarsh = currentForagingIsland == "Moonglade Marsh"

        currentFarmingIsland = FarmingMapping.farmingAreas.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }

        updateRiftIsland(island)
    }

    fun isMiningIsland(): Boolean {
        return MiningMapping.miningIslands.find { name ->
            currentMiningIsland?.equals(name, ignoreCase = true) == true
        } != null
    }

    fun isForagingIsland(): Boolean {
        return ForagingMapping.foragingIslands.find { name ->
            currentForagingIsland?.equals(name, ignoreCase = true) == true
        } != null
    }

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