package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.coleweightStopwatchPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.chat.TimeNotifierUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import kotlin.time.Duration

class StopwatchOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var stopwatchStartTime: EpochMark? = null
    private var stopwatchElapsed: Duration = Duration.ZERO
    var stopwatchRunning = false
    var stopwatchPaused = false

    override val overlayLabel: String = "Stopwatch Overlay"

    override val position: Position get() = coleweightStopwatchPosition

    override val isEnabled: Boolean get() = stopwatchRunning

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

    fun startStopwatch() {
        if (stopwatchRunning && !stopwatchPaused) {
            sendMessage("§cStopwatch is already running!", true)
            return
        }

        stopwatchStartTime = EpochMark.now()
        stopwatchElapsed = Duration.ZERO
        stopwatchRunning = true
        stopwatchPaused = false

        sendMessage("§aStopwatch started!", true)
        TimeNotifierUtils.resetStopwatchNotifier()
    }

    fun stopStopwatch() {
        if (!stopwatchRunning) {
            sendMessage("§cStopwatch is not running!", true)
            return
        }

        val startTime = stopwatchStartTime ?: return
        val elapsed = if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + startTime.timePassedSince()

        sendMessage("§cStopwatch stopped at §e" + elapsed.formatCompact() + "§c!", true)

        stopwatchStartTime = null
        stopwatchElapsed = Duration.ZERO
        stopwatchRunning = false
        stopwatchPaused = false
        cachedLines = emptyList()
    }

    fun pauseStopwatch() {
        if (!stopwatchRunning) {
            sendMessage("§cStopwatch is not running!", true)
            return
        }

        val startTime = stopwatchStartTime ?: return

        if (!stopwatchPaused) {
            stopwatchElapsed += startTime.timePassedSince()
            stopwatchPaused = true
            sendMessage("§eStopwatch paused!", true)
        } else {
            stopwatchStartTime = EpochMark.now()
            stopwatchPaused = false
            sendMessage("§aStopwatch resumed!", true)
        }
    }

    private fun updateLinesIfNeeded() {
        if (!stopwatchRunning || !isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 2L != 0L) return

        val startTime = stopwatchStartTime ?: return

        val elapsedSeconds = if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + startTime.timePassedSince()

        val pauseText = if (stopwatchPaused) "§7 (Paused)" else ""
        cachedLines = listOf("§bStopwatch: §e" + elapsedSeconds.formatCompact() + pauseText)
    }

    fun getElapsedTimeInSeconds(): Duration = if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + (stopwatchStartTime?.timePassedSince() ?: Duration.ZERO)
}
