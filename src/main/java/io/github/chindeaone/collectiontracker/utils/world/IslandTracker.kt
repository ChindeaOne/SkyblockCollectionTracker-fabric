package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.tab.TabWidget

object IslandTracker {

    @JvmStatic
    var currentMiningIsland: String? = null
        private set

    var currentForagingIsland: String? = null
        private set

    var currentFarmingIsland: String? = null
        private set
    
    var isInGalatea: Boolean = false
        private set

    var currentIsland: String? = null
        private set

    @JvmStatic
    var isInRift: Boolean = false
        private set
    private var riftCheckTicks = 0

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
        isInGalatea = currentForagingIsland == "Galatea"

        currentFarmingIsland = FarmingMapping.farmingAreas.firstOrNull { name ->
            island.equals(name, ignoreCase = true)
        }

        updateRiftIsland(island)
    }

    private fun updateRiftIsland(island: String) {
        if (riftCheckTicks > 0) {
            riftCheckTicks--
            return
        }
        riftCheckTicks = 20

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
        isInGalatea = false
        isInRift = false
    }
}