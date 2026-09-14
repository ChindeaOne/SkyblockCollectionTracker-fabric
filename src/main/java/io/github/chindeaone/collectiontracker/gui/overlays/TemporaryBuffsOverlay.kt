package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTempBuffPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowTempBuffExpiredTitle
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isTempBuffTrackerEnabled
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.fiestaFlaskTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.filetTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.powderPumpkinTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.pristinePotatoTime
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser.refinedCacaoTime
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import net.minecraft.network.chat.Component

class TemporaryBuffsOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val activeStates: MutableMap<String, Boolean> = mutableMapOf()

    override val overlayLabel: String = "Temporary Buffs"

    override val position: Position get() = getTempBuffPosition()

    override val isEnabled: Boolean get() = isTempBuffTrackerEnabled()

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

        val now = System.currentTimeMillis()
        val newLines = mutableListOf<String>()

        processBuff(newLines, "§6Refined Dark Cacao Truffle", refinedCacaoTime, now)
        processBuff(newLines, "§9Filet O' Fortune", filetTime, now)
        processBuff(newLines, "§5Chilled Pristine Potato", pristinePotatoTime, now)
        processBuff(newLines, "§aPowder Pie", powderPumpkinTime, now)
        processBuff(newLines, "§6Fiesta Flask", fiestaFlaskTime, now)

        cachedLines = newLines
    }

    private fun processBuff(lines: MutableList<String>, displayName: String, expireTime: Long, now: Long) {
        val isActive = expireTime > now
        val wasActive = activeStates.getOrDefault(displayName, false)

        if (wasActive && !isActive && isShowTempBuffExpiredTitle()) {
            showTitle(Component.literal("$displayName §cExpired!"), getTitleDisplayTimer())
        }
        activeStates[displayName] = isActive

        if (isActive) {
            val diff = expireTime - now
            val formattedTime = StringUtils.formatCompactTime(diff / 1000)
            lines.add("$displayName §e$formattedTime")
        }
    }
}
