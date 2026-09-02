package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightStopwatchPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage

class StopwatchOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var stopwatchStart = 0L
    private var stopwatchElapsed = 0L
    private var stopwatchRunning = false
    private var stopwatchPaused = false

    private var lastElapsedSeconds: Long = -1L
    private var lastPaused: Boolean = false
    private var lastRunning: Boolean = false

    override val overlayLabel: String = "Stopwatch Overlay"

    override val position: Position get() = getColeweightStopwatchPosition()

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

        stopwatchStart = System.currentTimeMillis()
        stopwatchElapsed = 0L
        stopwatchRunning = true
        stopwatchPaused = false
        lastElapsedSeconds = -1L

        sendMessage("§aStopwatch started!", true)
    }

    fun stopStopwatch() {
        if (!stopwatchRunning) {
            sendMessage("§cStopwatch is not running!", true)
            return
        }

        val elapsed = if (stopwatchPaused)
            stopwatchElapsed
        else
            stopwatchElapsed + (System.currentTimeMillis() - stopwatchStart)

        sendMessage("§cStopwatch stopped at §e" + StringUtils.formatCompactTime(elapsed / 1000L) + "§c!", true)

        stopwatchStart = 0L
        stopwatchElapsed = 0L
        stopwatchRunning = false
        stopwatchPaused = false
        cachedLines = emptyList()
    }

    fun pauseStopwatch() {
        if (!stopwatchRunning) {
            sendMessage("§cStopwatch is not running!", true)
            return
        }

        if (!stopwatchPaused) {
            stopwatchElapsed += System.currentTimeMillis() - stopwatchStart
            stopwatchPaused = true
            lastElapsedSeconds = -1L
            sendMessage("§eStopwatch paused!", true)
        } else {
            stopwatchStart = System.currentTimeMillis()
            stopwatchPaused = false
            lastElapsedSeconds = -1L
            sendMessage("§aStopwatch resumed!", true)
        }
    }

    private fun updateLinesIfNeeded() {
        if (!stopwatchRunning || !isEnabled) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val elapsed = if (stopwatchPaused)
            stopwatchElapsed
        else
            stopwatchElapsed + (System.currentTimeMillis() - stopwatchStart)

        val elapsedSeconds = elapsed / 1000L

        if (cachedLines.isNotEmpty() && elapsedSeconds == lastElapsedSeconds && stopwatchPaused == lastPaused && stopwatchRunning == lastRunning) return

        lastElapsedSeconds = elapsedSeconds
        lastPaused = stopwatchPaused
        lastRunning = stopwatchRunning

        val pauseText = if (stopwatchPaused) "§7 (Paused)" else ""
        val newLines = listOf("§bStopwatch: §e" + StringUtils.formatCompactTime(elapsedSeconds) + pauseText)

        cachedLines = newLines
    }
}
