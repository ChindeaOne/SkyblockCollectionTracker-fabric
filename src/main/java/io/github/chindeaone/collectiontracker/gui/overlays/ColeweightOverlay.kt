package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.coleweightTrackerPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler.uptime
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightAmount
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightGained
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightPerHour
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.coleweightSinceLast
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates.lastColeweightUpdate
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatFloat
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.seconds

class ColeweightOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Coleweight Tracker"

    override val position: Position get() = coleweightTrackerPosition

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
        if (!isEnabled || !trackingDirty) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 4L != 0L) return

        val lastUpdate = lastColeweightUpdate ?: return
        val timeAgo = if (lastUpdate.timePassedSince() >= 1.seconds) {
            lastUpdate.timePassedSince().formatCompact()
        } else ""

        val newLines = mutableListOf(
            "Coleweight: ${StringUtils.formatFloat(coleweightAmount)}",
            "CW (Session): ${StringUtils.formatFloat(coleweightGained)}",
            "CW/h: ${StringUtils.formatFloat(coleweightPerHour)}",
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
