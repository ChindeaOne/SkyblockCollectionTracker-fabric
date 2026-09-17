package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableTempBuffTracker
import io.github.chindeaone.collectiontracker.config.showTempBuffExpiredTitle
import io.github.chindeaone.collectiontracker.config.tempBuffPosition
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.fiestaFlaskEndTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.filetEndTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.powderPumpkinEndTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.pristinePotatoEndTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.refinedCacaoEndTime
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.showTitle
import net.minecraft.network.chat.Component

class TemporaryBuffsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val activeStates: MutableMap<String, Boolean> = mutableMapOf()

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

        processBuff(newLines, "§6Refined Dark Cacao Truffle", refinedCacaoEndTime)
        processBuff(newLines, "§9Filet O' Fortune", filetEndTime)
        processBuff(newLines, "§5Chilled Pristine Potato", pristinePotatoEndTime)
        processBuff(newLines, "§aPowder Pie", powderPumpkinEndTime)
        processBuff(newLines, "§6Fiesta Flask", fiestaFlaskEndTime)

        cachedLines = newLines
    }

    private fun processBuff(lines: MutableList<String>, displayName: String, expireTime: Long) {
        val now = System.currentTimeMillis()

        val isActive = expireTime > now
        val wasActive = activeStates.getOrDefault(displayName, false)

        if (wasActive && !isActive && showTempBuffExpiredTitle) {
            showTitle(Component.literal("$displayName §cExpired!"))
        }
        activeStates[displayName] = isActive

        if (isActive) {
            val diff = expireTime - now
            val formattedTime = StringUtils.formatCompactTime(diff / 1000)
            lines.add("$displayName §e$formattedTime")
        }
    }
}
