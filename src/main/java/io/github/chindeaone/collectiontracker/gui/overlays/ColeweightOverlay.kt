package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightTrackerPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler.uptime
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightAmount
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightGained
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightPerHour
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightSinceLast
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.lastColeweightTime
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatFloat
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.concurrent.Volatile

class ColeweightOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var lastFormattedTime: String = ""

    override val overlayLabel: String = "Coleweight Tracker"

    override val position: Position get() = getColeweightTrackerPosition()

    override val isEnabled: Boolean get() = ColeweightTrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled || !trackingDirty) return

        val lines = lines
        if (lines.isEmpty()) return

        RenderUtils.drawOverlayFrame(context, position) { RenderUtils.renderColeweightStrings(context, lines) }
    }

    override fun updateDimensions() {
        if (!isEnabled || !trackingDirty) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    private fun updateLinesIfNeeded() {
        val lastUpdateTime = lastColeweightTime
        val timeAgo = if (lastUpdateTime > 0) {
            val totalSeconds = (System.currentTimeMillis() - lastUpdateTime) / 1000
            StringUtils.formatCompactTime(totalSeconds)
        } else {
            ""
        }

        if (cachedLines.isNotEmpty() && timeAgo == lastFormattedTime) return

        lastFormattedTime = timeAgo

        val newLines = mutableListOf(
            "Coleweight: ${StringUtils.formatFloatOrPlaceholder(coleweightAmount)}",
            "CW (Session): ${StringUtils.formatFloatOrPlaceholder(coleweightGained)}",
            "CW/h: ${StringUtils.formatFloatOrPlaceholder(coleweightPerHour)}",
            "Since Last: ${formatFloat(coleweightSinceLast)}"
        )

        if (timeAgo.isNotEmpty()) {
            newLines.add("Last updated: $timeAgo ago")
        }
        newLines.add("Uptime: $uptime")

        cachedLines = newLines
    }

    companion object {
        @Volatile
        var trackingDirty: Boolean = false
    }
}
