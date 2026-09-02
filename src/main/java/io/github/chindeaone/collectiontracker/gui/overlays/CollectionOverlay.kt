package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarPriceType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getGemstoneVariant
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTrackingPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isOverlayTextColorEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowExtraStats
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigHelper.changeBazaarPrice
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaar
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setShowExtraStats
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.utils.parser.CollectionParser
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import kotlin.concurrent.Volatile
import kotlin.math.roundToInt

class CollectionOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var cachedMainLines: List<String> = emptyList()
    private var cachedExtraLines: List<String> = emptyList()

    private var lastUptime: String = ""
    private var lastIsChatOpened: Boolean = false
    private var lastShowExtraStats: Boolean = false
    private var lastBzType: Bazaar.BazaarType? = null
    private var lastBzPriceType: Bazaar.BazaarPriceType? = null
    private var lastGemVariant: Bazaar.GemstoneVariant? = null

    override val overlayLabel: String = "Collection Tracker"

    override val position: Position get() = getTrackingPosition()

    override val isEnabled: Boolean get() = TrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled || !trackingDirty) return

        updateLinesIfNeeded()

        if (cachedMainLines.isEmpty()) return

        RenderUtils.drawOverlayFrame(context, position) {
            RenderUtils.renderTrackingStringsWithColor(
                context,
                cachedMainLines,
                cachedExtraLines,
                isOverlayTextColorEnabled()
            )
        }
    }

    override fun updateDimensions() {
        if (!isEnabled || !trackingDirty) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    private fun updateLinesIfNeeded() {
        if (!isEnabled || !trackingDirty) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
                cachedMainLines = emptyList()
                cachedExtraLines = emptyList()
            }
            return
        }

        val uptime = TrackingHandler.uptime
        val isChatOpened = Minecraft.getInstance()./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/ is ChatScreen
        val showExtra = isShowExtraStats()
        val bzType = getBazaarType()
        val bzPriceType = getBazaarPriceType()
        val gemVariant = getGemstoneVariant()

        if (cachedLines.isNotEmpty() && uptime == lastUptime && isChatOpened == lastIsChatOpened
            && showExtra == lastShowExtraStats && bzType == lastBzType && bzPriceType == lastBzPriceType
            && gemVariant == lastGemVariant) return

        lastUptime = uptime
        lastIsChatOpened = isChatOpened
        lastShowExtraStats = showExtra
        lastBzType = bzType
        lastBzPriceType = bzPriceType
        lastGemVariant = gemVariant

        val main = mutableListOf<String>()
        CollectionParser.updateTrackingLines(main)
        if (main.isNotEmpty()) {
            main.add("Uptime: $uptime")
            if (!showExtra && isChatOpened) {
                CollectionParser.addToggleableSettingsLines(main)
            }
        }

        val extra = mutableListOf<String>()
        if (showExtra) {
            CollectionParser.updateTrackingExtraLines(extra)
            if (isChatOpened) {
                CollectionParser.addToggleableSettingsLines(extra)
            }
        }

        cachedMainLines = main
        cachedExtraLines = extra

        val combined = mutableListOf<String>()
        combined.addAll(main)
        if (showExtra && extra.isNotEmpty()) {
            combined.add("") // add separator line
            combined.addAll(extra)
        }

        cachedLines = combined
    }

    override fun handleLineAction(line: String) {
        when (line) {
            "§e[Bazaar Prices]" -> setBazaar(true)
            "§e[NPC Prices]" -> setBazaar(false)
            "§e[Extra Stats]" -> setShowExtraStats(!isShowExtraStats())
        }
        if (line.contains(getGemstoneVariant().toString())) {
            cycleGemstoneVariant()
        }
        if (line.contains("version")) {
            changeEnchantedType()
        }
        if (line.contains("Instant")) {
            changeBazaarPriceType()
        }
    }

    override fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        if (!isEnabled) return false

        updateDimensions()

        val position = this.position

        val padding = 8

        val x = position.x
        val y = position.y
        val scale = position.scale

        val width = ((position.width + padding * 2) * scale).roundToInt()
        val height = ((position.height + padding * 2) * scale).roundToInt()

        val x1 = (x - padding * scale).toDouble()
        val y1 = (y - padding * scale).toDouble()
        val x2 = x1 + width
        val y2 = y1 + height

        return mouseX in x1..x2 && mouseY >= y1 && mouseY <= y2
    }

    private fun cycleGemstoneVariant() {
        val variants: Array<Bazaar.GemstoneVariant> = Bazaar.GemstoneVariant.entries.toTypedArray()
        val current = getGemstoneVariant()
        val nextOrdinal = (current.ordinal + 1) % variants.size
        ConfigHelper.setGemstoneVariant(variants[nextOrdinal])
    }

    private fun changeEnchantedType() {
        setBazaarType(if (getBazaarType() == Bazaar.BazaarType.ENCHANTED_VERSION) Bazaar.BazaarType.SUPER_ENCHANTED_VERSION else Bazaar.BazaarType.ENCHANTED_VERSION)
    }

    private fun changeBazaarPriceType() {
        changeBazaarPrice(if (getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY) Bazaar.BazaarPriceType.INSTANT_SELL else Bazaar.BazaarPriceType.INSTANT_BUY)
    }

    companion object {
        @Volatile
        var trackingDirty: Boolean = false
    }
}
