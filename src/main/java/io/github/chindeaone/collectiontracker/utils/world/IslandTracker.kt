package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.tab.TabWidget

object IslandTracker {

    @JvmStatic var currentMiningIsland: String? = null
        private set

    @JvmStatic var currentForagingIsland: String? = null
        private set

    var currentFarmingIsland: String? = null
        private set

    @JvmStatic var isInPark: Boolean = false
        private set

    @JvmStatic var isInMoongladeMarsh: Boolean = false
        private set

    var currentIsland: String? = null
        private set

    @JvmStatic var isInRift: Boolean = false
        private set

    fun update() {
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

        currentForagingIsland = ForagingMapping.foragingIslands.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }
        isInPark = currentForagingIsland == "The Park"
        isInMoongladeMarsh = currentForagingIsland == "Moonglade Marsh"

        currentFarmingIsland = FarmingMapping.farmingAreas.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }

        updateRiftIsland(island)
    }

    private fun updateRiftIsland(island: String) {
        val currentlyInRift = island.equals("The Rift", ignoreCase = true)

        if (currentlyInRift == isInRift) return
        isInRift = currentlyInRift

        if (!isInRift) return

        TrackingHandler.resumeRiftTracking()
        MultiTrackingHandler.resumeMultiRiftTracking()
    }

    fun reset() {
        currentIsland = null
        currentMiningIsland = null
        currentForagingIsland = null
        isInMoongladeMarsh = false
        isInRift = false
    }
}