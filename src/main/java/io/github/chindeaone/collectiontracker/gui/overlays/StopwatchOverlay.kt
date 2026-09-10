package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightStopwatchPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.TimeUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage

class StopwatchOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var stopwatchStartTime = 0L
    private var stopwatchElapsed = 0L
    var stopwatchRunning = false
    var stopwatchPaused = false

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

        stopwatchStartTime = System.currentTimeMillis()
        stopwatchElapsed = 0L
        stopwatchRunning = true
        stopwatchPaused = false

        sendMessage("§aStopwatch started!", true)
        TimeUtils.resetStopwatchNotifier()
    }

    fun stopStopwatch() {
        if (!stopwatchRunning) {
            sendMessage("§cStopwatch is not running!", true)
            return
        }

        val elapsed = (if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + (System.currentTimeMillis() - stopwatchStartTime)) / 1000L
        sendMessage("§cStopwatch stopped at §e" + StringUtils.formatCompactTime(elapsed) + "§c!", true)

        stopwatchStartTime = 0L
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
            stopwatchElapsed += System.currentTimeMillis() - stopwatchStartTime
            stopwatchPaused = true
            sendMessage("§eStopwatch paused!", true)
        } else {
            stopwatchStartTime = System.currentTimeMillis()
            stopwatchPaused = false
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

        if (ModLoader.clientTicks % 5L != 0L) return

        val elapsedSeconds = (if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + (System.currentTimeMillis() - stopwatchStartTime)) / 1000L

        val pauseText = if (stopwatchPaused) "§7 (Paused)" else ""
        cachedLines = listOf("§bStopwatch: §e" + StringUtils.formatCompactTime(elapsedSeconds) + pauseText)
    }

    fun getElapsedTimeInSeconds(): Long = (if (stopwatchPaused) stopwatchElapsed else stopwatchElapsed + (System.currentTimeMillis() - stopwatchStartTime)) / 1000L
}
