package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigHelper.changeBazaarPrice
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaar
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setShowExtraStats
import io.github.chindeaone.collectiontracker.config.bazaarConfig
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.gemstoneVariant
import io.github.chindeaone.collectiontracker.config.showExtraStats
import io.github.chindeaone.collectiontracker.config.trackingPosition
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.parser.CollectionParser
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import kotlin.concurrent.Volatile
import kotlin.math.roundToInt

class CollectionOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    override val overlayLabel: String = "Collection Tracker"

    override val position: Position get() = trackingPosition

    override val isEnabled: Boolean get() = TrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled || !trackingDirty) return

        val lines = lines
        if (lines.isEmpty()) return

        RenderUtils.drawOverlayFrame(context, position) { RenderUtils.renderTrackingStringsWithColor(context, lines) }
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
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        TrackingRates.updateRates()

        val uptime = TrackingHandler.uptime
        val isChatOpened = MinecraftUtils.screen is ChatScreen
        val showExtra = showExtraStats

        val main = mutableListOf<String>()
        CollectionParser.updateTrackingLines(main)
        if (main.isNotEmpty()) {
            main.add("Uptime: $uptime")
            if (!showExtra && isChatOpened) CollectionParser.addToggleableSettingsLines(main)
        }

        val extra = mutableListOf<String>()
        if (showExtra) {
            CollectionParser.updateTrackingExtraLines(extra)
            CollectionParser.addToggleableSettingsLines(extra)
        }

        cachedLines = buildList {
            addAll(main)
            if (showExtra) {
                add("") // add separator line
                addAll(extra)
            }
        }
    }

    override fun handleLineAction(line: String) {
        when {
            line.contains("Prices") -> setBazaar(!bazaarConfig.useBazaar)
            line.contains("Extra") -> setShowExtraStats(!showExtraStats)
            line.contains(gemstoneVariant.toString()) -> cycleGemstoneVariant()
            line.contains("version") -> changeEnchantedType()
            line.contains("Instant") -> changeBazaarPriceType()
        }
    }

    override fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        if (!isEnabled) return false

        updateDimensions()

        val position = position

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
        val current = gemstoneVariant
        val nextOrdinal = (current.ordinal + 1) % variants.size
        ConfigHelper.setGemstoneVariant(variants[nextOrdinal])
    }

    private fun changeEnchantedType() {
        setBazaarType(if (bazaarType == Bazaar.BazaarType.ENCHANTED_VERSION) Bazaar.BazaarType.SUPER_ENCHANTED_VERSION else Bazaar.BazaarType.ENCHANTED_VERSION)
    }

    private fun changeBazaarPriceType() {
        changeBazaarPrice(if (bazaarPriceType == Bazaar.BazaarPriceType.INSTANT_BUY) Bazaar.BazaarPriceType.INSTANT_SELL else Bazaar.BazaarPriceType.INSTANT_BUY)
    }

    companion object {
        @Volatile
        var trackingDirty: Boolean = false
    }
}
