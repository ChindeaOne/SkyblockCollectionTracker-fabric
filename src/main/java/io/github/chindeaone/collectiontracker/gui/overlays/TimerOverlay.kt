package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.coleweightTimerPosition
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.showTimerTitle
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import io.github.chindeaone.collectiontracker.utils.chat.TimeNotifierUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.showTitle
import net.minecraft.network.chat.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimerOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var coleweightTimerEnd: EpochMark? = null
    private var remainingTime: Duration = Duration.ZERO
    var isPaused = false
    var hasEnded = true

    override val overlayLabel: String = "Timer Overlay"

    override val position: Position get() = coleweightTimerPosition

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

    fun setTimer(duration: Duration) {
        if (duration.inWholeSeconds == 0L) {
            sendMessage("§cTimer cancelled!")
            hasEnded = true
            isPaused = false
            cachedLines = emptyList()
            return
        }
        coleweightTimerEnd = duration.fromNow()
        isPaused = false
        hasEnded = false

        sendMessage("§aTimer set for ${duration.formatCompact()}!")
        TimeNotifierUtils.resetTimerNotifier()
    }

    fun pauseTimer() {
        if (hasEnded) {
            sendMessage("§cTimer has already ended!")
            return
        }
        val timer = coleweightTimerEnd ?: return

        if (!isPaused && timer.isInFuture) {
            remainingTime = timer.timeUntil()
            isPaused = true
            sendMessage("§eTimer paused!")
        } else {
            coleweightTimerEnd = remainingTime.fromNow()
            isPaused = false
            sendMessage("§aTimer resumed!")
        }
    }

    private fun updateLinesIfNeeded() {
        if (hasEnded || !isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 2L != 0L) return

        val timerEnd = coleweightTimerEnd ?: return
        if (!isPaused && timerEnd.isPassed) {
            if (showTimerTitle) {
                showTitle(Component.literal("§6[§3§kd§6] §b§lTimer Finished! §6[§3§kd§6]"))
            }
            sendMessage("§cTimer finished!")
            hasEnded = true
            cachedLines = emptyList()
            return
        }

        val remaining = if (isPaused) remainingTime else timerEnd.timeUntil()
        if (remaining > 0.seconds) {
            val pauseTarget = if (isPaused) "§7 (Paused)" else ""
            val timeFormat = remaining.formatCompact()
            cachedLines = listOf("§bTimer: §e$timeFormat$pauseTarget")
        }
    }

    fun getRemainingTimeInSeconds(): Duration = if (isPaused) remainingTime else (coleweightTimerEnd?.timeUntil() ?: Duration.ZERO)
}
