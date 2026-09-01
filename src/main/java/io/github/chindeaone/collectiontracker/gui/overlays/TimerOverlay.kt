package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getColeweightTimerPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowTimerTitle
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.HypixelUtils.isInSkyblock
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderStrings
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import kotlin.math.max

class TimerOverlay : AbstractOverlay() {
    private val position = getColeweightTimerPosition()
    private val timerLines: MutableList<String> = ArrayList<String>()
    private var coleweightTimerEnd: Long = 0
    private var remainingTime: Long = 0
    private var isPaused = false
    private var hasEnded = true

    override fun overlayLabel(): String {
        return "Timer Overlay"
    }

    override fun position(): Position {
        return position
    }

    override fun isEnabled(): Boolean {
        return !hasEnded && isInSkyblock
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return

        val lines = getTimerLines()
        if (lines.isEmpty()) return

        drawOverlayFrame(context, position) { renderStrings(context, lines) }
    }

    override fun updateDimensions() {
        if (!isEnabled) return
        val lines = getTimerLines()
        if (lines.isEmpty()) return

        val fr = Minecraft.getInstance().font
        var maxW = 0
        for (l in lines) maxW = max(maxW, fr.width(l))
        val h = fr.lineHeight * lines.size

        position.setDimensions(maxW, h)
    }

    fun setTimer(duration: Long) {
        if (duration == 0L) {
            sendMessage("§cTimer cancelled!", true)
            hasEnded = true
            isPaused = false
            return
        }
        coleweightTimerEnd = System.currentTimeMillis() + duration * 1000L
        isPaused = false
        hasEnded = false

        sendMessage("§aTimer set for ${StringUtils.formatTimeIntoText(duration)}!", true)
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

    private fun getTimerLines(): MutableList<String> {
        timerLines.clear()
        if (hasEnded) return timerLines

        val now = System.currentTimeMillis()
        val remaining = (if (isPaused) remainingTime else coleweightTimerEnd - now) / 1000

        if (remaining > 0) {
            val pauseTarget = if (isPaused) "§7 (Paused)" else ""
            val timeFormat = StringUtils.formatCompactTime(remaining)
            timerLines.add("§bTimer: §e$timeFormat$pauseTarget")
        } else {
            if (isShowTimerTitle()) {
                val title = "§6[§3§kd§6] §b§lTimer Finished! §6[§3§kd§6]"
                showTitle(Component.literal(title), getTitleDisplayTimer())
            }
            sendMessage("§cTimer finished!", true)
            hasEnded = true
        }

        return timerLines
    }
}
