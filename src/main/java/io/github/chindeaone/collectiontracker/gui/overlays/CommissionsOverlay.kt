package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.commissionsPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableCommissionsOverlay
import io.github.chindeaone.collectiontracker.config.enableCommissionsTracking
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getCommissionsPerHour
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getCompletedCount
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getUptime
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser
import io.github.chindeaone.collectiontracker.utils.tab.CommissionWidget

class CommissionsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Commissions"

    override val position: Position get() = commissionsPosition

    override val isEnabled: Boolean get() = enableCommissionsOverlay

    override fun updateDimensions() {
        if (!isEnabled) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    private fun updateLinesIfNeeded() {
        if (!isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val commissions = CommissionWidget.commissions
        if (commissions.isEmpty()) {
            cachedLines = emptyList()
            return
        }

        val isTracking = enableCommissionsTracking
        val completed = getCompletedCount()
        val perHour = getCommissionsPerHour()
        val currentUptime = getUptime()

        val newLines = mutableListOf<String>()
        var detectedArea: CommissionParser.Area? = null

        for (commission in commissions) {
            newLines.add(commission.formattedLine)
            if (detectedArea == null) {
                detectedArea = commission.type.area
            }
        }

        if (detectedArea != null) {
            when (detectedArea) {
                CommissionParser.Area.DWARVEN_MINES -> newLines.addFirst("§2§l" + detectedArea.displayName)
                CommissionParser.Area.CRYSTAL_HOLLOWS -> newLines.addFirst("§5§l" + detectedArea.displayName)
                CommissionParser.Area.GLACITE_TUNNELS -> newLines.addFirst("§b§l" + detectedArea.displayName)
            }
        }

        if (isTracking && completed > 0) {
            if (newLines.isNotEmpty()) {
                newLines.add("")
            }
            newLines.add("§6Commissions Completed: §e$completed")
            newLines.add("§6Commissions/h: §e" + String.format("%.2f", perHour))
            newLines.add("§6Uptime: §e$currentUptime")
        }

        cachedLines = newLines
    }
}
