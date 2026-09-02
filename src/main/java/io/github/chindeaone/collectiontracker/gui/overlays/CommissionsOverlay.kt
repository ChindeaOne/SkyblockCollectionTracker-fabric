package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getCommissionsPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isCommissionsOverlayEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isCommissionsTrackingEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getCommissionsPerHour
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getCompletedCount
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker.getUptime
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser
import io.github.chindeaone.collectiontracker.utils.tab.CommissionWidget

class CommissionsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var lastCommissionsHash: Int = 0
    private var lastTracking: Boolean = false
    private var lastCompleted: Int = -1
    private var lastPerHour: Double = -1.0
    private var lastUptime: String = ""

    override val overlayLabel: String = "Commissions"

    override val position: Position get() = getCommissionsPosition()

    override val isEnabled: Boolean get() = isCommissionsOverlayEnabled()

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
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val commissions = CommissionWidget.commissions
        if (commissions.isEmpty()) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val commissionsHash = commissions.fold(1) { acc, c -> 31 * acc + c.formattedLine.hashCode() } // detect any change in the list
        val isTracking = isCommissionsTrackingEnabled()
        val completed = getCompletedCount()
        val perHour = getCommissionsPerHour()
        val currentUptime = getUptime()

        if (cachedLines.isNotEmpty()
            && isTracking == lastTracking
            && completed == lastCompleted
            && perHour == lastPerHour
            && commissionsHash == lastCommissionsHash
            && currentUptime == lastUptime) return


        lastTracking = isTracking
        lastCompleted = completed
        lastPerHour = perHour
        lastUptime = currentUptime
        lastCommissionsHash = commissionsHash

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
