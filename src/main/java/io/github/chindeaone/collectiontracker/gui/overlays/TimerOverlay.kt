package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightTimerPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowTimerTitle
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.TimeUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import net.minecraft.network.chat.Component

class TimerOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var coleweightTimerEnd: Long = 0
    private var remainingTime: Long = 0
    var isPaused = false
    var hasEnded = true

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

        sendMessage("§aTimer set for ${StringUtils.formatCompactTime(duration)}!", true)
        TimeUtils.resetTimerNotifier()
    }

    fun pauseTimer() {
        if (hasEnded) {
            sendMessage("§cTimer has already ended!", true)
            return
        }
        if (!isPaused && coleweightTimerEnd > System.currentTimeMillis()) {
            remainingTime = coleweightTimerEnd - System.currentTimeMillis()
            isPaused = true
            sendMessage("§eTimer paused!", true)
        } else {
            coleweightTimerEnd = System.currentTimeMillis() + remainingTime
            isPaused = false
            sendMessage("§aTimer resumed!", true)
        }
    }

    private fun updateLinesIfNeeded() {
        if (hasEnded || !isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val now = System.currentTimeMillis()
        val remaining = (if (isPaused) remainingTime else coleweightTimerEnd - now) / 1000

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

    fun getRemainingTimeInSeconds(): Long = (if (isPaused) remainingTime else (coleweightTimerEnd - System.currentTimeMillis())) / 1000
}
