package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.tab.TabWidget

object IslandTracker {

    var currentMiningIsland: String? = null
        private set

    var currentForagingIsland: String? = null
        private set
    
    var isInGalatea: Boolean = false
        private set

    fun update() {
        val areaWidget = TabWidget.AREA
        if (!areaWidget.isPresent) {
            reset()
            return
        }

        val lines = areaWidget.lines
        updateMiningIsland(lines)
        updateForagingIsland(lines)
    }

    private fun updateMiningIsland(lines: List<String>) {
        currentMiningIsland = if (ConfigAccess.isMiningStatsOverlayInMiningIslandsOnly()) {
            lines.firstNotNullOfOrNull { line ->
                MiningMapping.miningIslands.firstOrNull { name ->
                    line.contains(name, ignoreCase = true)
                }
            }
        } else {
            lines.firstNotNullOfOrNull { line ->
                MiningMapping.miningAreas.firstOrNull { name ->
                    line.contains(name, ignoreCase = true)
                }
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

    fun reset() {
        currentMiningIsland = null
        currentForagingIsland = null
        isInGalatea = false
    }
}