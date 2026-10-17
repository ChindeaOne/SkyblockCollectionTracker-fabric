package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableTempBuffTracker
import io.github.chindeaone.collectiontracker.config.showTempBuffExpiredTitle
import io.github.chindeaone.collectiontracker.config.tempBuffPosition
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser.Consumables
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.showTitle
import net.minecraft.network.chat.Component
import java.util.EnumMap

class ConsumablesOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val activeStates = EnumMap<Consumables, Boolean>(Consumables::class.java)

    override val overlayLabel: String = "Consumables"

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

        Consumables.entries.forEach { buff ->
            processBuff(newLines, buff)
        }

        cachedLines = newLines
    }

    private fun processBuff(lines: MutableList<String>, buff: Consumables) {
        val expireTime = ConsumableParser.getEndTime(buff) ?: return
        val isExpired = expireTime.isPassed
        val isActive = !isExpired

        val wasActive = activeStates.getOrDefault(buff, false)

        if (wasActive && isExpired && showTempBuffExpiredTitle) {
            showTitle(Component.literal("${buff.overlayName} §cExpired!"))
        }

        activeStates[buff] = isActive

        if (isActive) {
            val remaining = expireTime.timeUntil()
            val formattedTime = remaining.formatCompact()

            lines.add("${buff.overlayName} §e$formattedTime")
        }
    }
}
