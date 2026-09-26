package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableTempBuffTracker
import io.github.chindeaone.collectiontracker.config.showTempBuffExpiredTitle
import io.github.chindeaone.collectiontracker.config.tempBuffPosition
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.TemporaryBuff
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.showTitle
import net.minecraft.network.chat.Component
import java.util.EnumMap

class TemporaryBuffsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val activeStates = EnumMap<TemporaryBuff, Boolean>(TemporaryBuff::class.java)

    override val overlayLabel: String = "Temporary Buffs"

    override val position: Position get() = tempBuffPosition

    override val isEnabled: Boolean get() = enableTempBuffTracker

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
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val newLines = mutableListOf<String>()

        TemporaryBuff.entries.forEach { buff ->
            processBuff(newLines, buff)
        }

        cachedLines = newLines
    }

    private fun processBuff(lines: MutableList<String>, buff: TemporaryBuff) {
        val now = System.currentTimeMillis()
        val expireTime = TemporaryBuffsParser.getEndTime(buff)

        val isActive = expireTime > now
        val wasActive = activeStates.getOrDefault(buff, false)

        if (wasActive && !isActive && showTempBuffExpiredTitle) {
            showTitle(Component.literal("${buff.overlayName} §cExpired!"))
        }

        activeStates[buff] = isActive

        if (isActive) {
            val diff = expireTime - now
            val formattedTime = StringUtils.formatCompactTime(diff / 1000)

            lines.add("${buff.overlayName} §e$formattedTime")
        }
    }
}
