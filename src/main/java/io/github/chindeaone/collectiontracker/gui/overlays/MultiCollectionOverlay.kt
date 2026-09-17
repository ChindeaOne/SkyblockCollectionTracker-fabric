package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigHelper.changeBazaarPrice
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaar
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.bazaarConfig
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.gemstoneVariant
import io.github.chindeaone.collectiontracker.config.multiOverlayPosition
import io.github.chindeaone.collectiontracker.config.trackingOptions
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.parser.CollectionParser
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.renderMultiTrackingStringsWithColor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import kotlin.concurrent.Volatile

class MultiCollectionOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val expandedCollections: MutableList<String> = mutableListOf()

    override val overlayLabel: String = "Multi-Collection Tracker"

    override val position: Position get() = multiOverlayPosition

    override val isEnabled: Boolean get() = MultiTrackingHandler.isMultiTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled || !trackingDirty) return

        val mainLines = lines
        if (mainLines.isEmpty()) return

        drawOverlayFrame(context, position) { renderMultiTrackingStringsWithColor(context, mainLines) }
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

        MultiTrackingRates.updateRates()

        val uptime = MultiTrackingHandler.multiUptime
        val isChatOpened = MinecraftUtils.screen is ChatScreen

        val newLines = mutableListOf<String>()
        CollectionParser.updateMultiTrackingLines(newLines, expandedCollections, isChatOpened)
        newLines.add("Uptime: $uptime")

        if (isChatOpened) CollectionParser.addToggleableSettingsLines(newLines)

        cachedLines = newLines
    }

    override fun handleLineAction(line: String) {
        val cleanLine = line.removeColor()

        if (cleanLine.startsWith("[+] ") || cleanLine.startsWith("[-] ")) {
            val content = cleanLine.substring(4)

            val collName = if (content.startsWith("Gemstone")) {
                "gemstone"
            } else {
                content.substringBefore(":").trim { it <= ' ' }.lowercase().replace(' ', '_')
            }

            if (expandedCollections.contains(collName)) {
                expandedCollections.remove(collName)
            } else {
                expandedCollections.add(collName)
            }
            return
        }

        if (cleanLine.startsWith("Gemstone")) {
            val collName = "gemstone"
            if (expandedCollections.contains(collName)) {
                expandedCollections.remove(collName)
            } else {
                expandedCollections.add(collName)
            }
            return
        }

        when {
            line.contains("Stats") -> cycleStats()
            line.contains("Prices") -> setBazaar(!bazaarConfig.useBazaar)
            line.contains(gemstoneVariant.toString()) -> cycleGemstoneVariant()
            line.contains("version") -> changeEnchantedType()
            line.contains("Instant") -> changeBazaarPriceType()
        }
    }

    override fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        if (!isEnabled) return false

        updateDimensions()

        val position = position
        val x = position.x
        val y = position.y
        val scale = position.scale

        val height = (position.height * scale).toInt()
        val width = (position.width * scale).toInt()

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }

    private fun cycleStats() {
        val options = MultiCollectionConfig.TrackingOptions.entries
        val currentStat = trackingOptions
        val nextIndex = (currentStat.ordinal + 1) % options.size
        ConfigHelper.setMultiTrackingOption(options[nextIndex])
    }

    private fun cycleGemstoneVariant() {
        val variants = Bazaar.GemstoneVariant.entries
        val current = gemstoneVariant
        val nextIndex = (current.ordinal + 1) % variants.size
        ConfigHelper.setGemstoneVariant(variants[nextIndex])
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
