package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightTimerPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowTimerTitle
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import net.minecraft.network.chat.Component

class TimerOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var coleweightTimerEnd: Long = 0
    private var remainingTime: Long = 0
    private var isPaused = false
    private var hasEnded = true

    private var lastRemainingSeconds: Long = -1L
    private var lastPaused: Boolean = false

    override val overlayLabel: String = "Timer Overlay"

    override val position: Position get() = getColeweightTimerPosition()

    override val isEnabled: Boolean get() = !hasEnded

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

    fun setTimer(duration: Long) {
        if (duration == 0L) {
            sendMessage("§cTimer cancelled!", true)
            hasEnded = true
            isPaused = false
            cachedLines = emptyList()
            return
        }
        coleweightTimerEnd = System.currentTimeMillis() + duration * 1000L
        isPaused = false
        hasEnded = false
        lastRemainingSeconds = -1L

        sendMessage("§aTimer set for ${StringUtils.formatCompactTime(duration)}!", true)
    }

    fun pauseTimer() {
        if (hasEnded) {
            sendMessage("§cTimer has already ended!", true)
            return
        }
        if (!isPaused && coleweightTimerEnd > System.currentTimeMillis()) {
            remainingTime = coleweightTimerEnd - System.currentTimeMillis()
            isPaused = true
            lastRemainingSeconds = -1L
            sendMessage("§eTimer paused!", true)
        } else {
            coleweightTimerEnd = System.currentTimeMillis() + remainingTime
            isPaused = false
            lastRemainingSeconds = -1L
            sendMessage("§aTimer resumed!", true)
        }
    }

    private fun updateLinesIfNeeded() {
        if (hasEnded || !isEnabled) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val now = System.currentTimeMillis()
        val remaining = (if (isPaused) remainingTime else coleweightTimerEnd - now) / 1000

        if (cachedLines.isNotEmpty() && remaining == lastRemainingSeconds && isPaused == lastPaused) {
            return
        }

        lastRemainingSeconds = remaining
        lastPaused = isPaused

        if (remaining > 0) {
            val pauseTarget = if (isPaused) "§7 (Paused)" else ""
            val timeFormat = StringUtils.formatCompactTime(remaining)
            cachedLines = listOf("§bTimer: §e$timeFormat$pauseTarget")
        } else {
            if (isShowTimerTitle()) {
                val title = "§6[§3§kd§6] §b§lTimer Finished! §6[§3§kd§6]"
                showTitle(Component.literal(title), getTitleDisplayTimer())
            }
            sendMessage("§cTimer finished!", true)
            hasEnded = true
            cachedLines = emptyList()
        }
    }
}
