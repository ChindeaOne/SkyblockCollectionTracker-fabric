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
        updateMiningIsland(lines)
        updateForagingIsland(lines)
        updateFarmingIsland(lines)
        updateRiftIsland(lines)
    }

    private fun updateMiningIsland(lines: List<String>) {
        currentMiningIsland = lines.firstNotNullOfOrNull { line ->
            MiningMapping.miningAreas.firstOrNull { name ->
                line.contains(name, ignoreCase = true)
            }
        }
    }

    private fun updateForagingIsland(lines: List<String>) {
        val foragingLines = lines.find { it.contains("The Park") || it.contains("Galatea") }
        if (foragingLines != null) {
            isInGalatea = foragingLines.contains("Galatea")
            currentForagingIsland = if (isInGalatea) "Galatea" else "The Park"
        } else {
            currentForagingIsland = null
            isInGalatea = false
        }
    }

    private fun updateFarmingIsland(lines: List<String>) {
        currentFarmingIsland = lines.firstNotNullOfOrNull { line ->
            FarmingMapping.farmingAreas.firstOrNull { name ->
                line.contains(name, ignoreCase = true)
            }
        }
    }

    private fun updateRiftIsland(lines: List<String>) {
        if (riftCheckTicks > 0) {
            riftCheckTicks--
            return
        }
        riftCheckTicks = 20

        val currentlyInRift = lines.any {
            it.contains("The Rift", ignoreCase = true)
        }

        if (currentlyInRift == isInRift) return
        isInRift = currentlyInRift

        if (!isInRift) return

        TrackingHandler.resumeRiftTracking()
        MultiTrackingHandler.resumeMultiRiftTracking()
    }

    fun reset() {
        currentMiningIsland = null
        currentForagingIsland = null
        isInGalatea = false
        isInRift = false
    }
}