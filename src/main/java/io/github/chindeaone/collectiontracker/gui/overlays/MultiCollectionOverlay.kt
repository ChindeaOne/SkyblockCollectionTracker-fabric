package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.CollectionTier
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarType
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigHelper.changeBazaarPrice
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaar
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.bazaarConfig
import io.github.chindeaone.collectiontracker.config.bazaarPriceType
import io.github.chindeaone.collectiontracker.config.bazaarType
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.collectionGemstoneTier
import io.github.chindeaone.collectiontracker.config.collectionTier
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.gemstoneTier
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
        newLines.add("Uptime: ${if (MultiTrackingHandler.isMultiPaused) "$uptime (§7Paused)" else uptime}")

        if (isChatOpened) CollectionParser.addToggleableSettingsLines(newLines)

        cachedLines = newLines
    }

    override fun handleLineAction(line: String) {
        val cleanLine = line.removeColor()

        if (cleanLine.startsWith("[+] ") || cleanLine.startsWith("[-] ")) {
            val content = cleanLine.substring(4)

            val collName = if (content.startsWith("Gemstone") && !cleanLine.contains("Tier")) {
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

        if (cleanLine.startsWith("Gemstone") && !cleanLine.contains("Tier")) {
            val collName = "gemstone"
            if (expandedCollections.contains(collName)) {
                expandedCollections.remove(collName)
            } else {
                expandedCollections.add(collName)
            }
            return
        }

        when {
            line.contains("Collection Tier") -> cycleCollectionTier()
            line.contains("Gemstone Tier") -> cycleCollectionGemstoneTier()
            line.contains("Stats") -> cycleStats()
            line.contains("Price Source") -> setBazaar(!bazaarConfig.useBazaar)
            line.contains(gemstoneTier.toString()) -> cycleGemstoneTier()
            line.contains("Price Tier") -> changeEnchantedType()
            line.contains("Bazaar Price") -> changeBazaarPriceType()
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

    private fun cycleCollectionTier() {
        val variants = CollectionTier.entries.toTypedArray()
        val nextOrdinal = (collectionTier.ordinal + 1) % variants.size
        ConfigHelper.setCollectionTier(variants[nextOrdinal])
    }

    private fun cycleCollectionGemstoneTier() {
        val tiers = GemstoneTier.entries.toTypedArray()
        val nextOrdinal = (collectionGemstoneTier.ordinal + 1) % tiers.size
        ConfigHelper.setCollectionGemstoneTier(tiers[nextOrdinal])
    }

    private fun cycleStats() {
        val options = MultiCollectionConfig.TrackingOptions.entries
        val currentStat = trackingOptions
        val nextIndex = (currentStat.ordinal + 1) % options.size
        ConfigHelper.setMultiTrackingOption(options[nextIndex])
    }

    private fun cycleGemstoneTier() {
        val variants = GemstoneTier.entries
        val current = gemstoneTier
        val nextIndex = (current.ordinal + 1) % variants.size
        ConfigHelper.setGemstoneTier(variants[nextIndex])
    }

    private fun changeEnchantedType() {
        setBazaarType(if (bazaarType == BazaarType.ENCHANTED_VERSION) BazaarType.SUPER_ENCHANTED_VERSION else BazaarType.ENCHANTED_VERSION)
    }

    private fun changeBazaarPriceType() {
        changeBazaarPrice(if (bazaarPriceType == BazaarPriceType.INSTANT_BUY) BazaarPriceType.INSTANT_SELL else BazaarPriceType.INSTANT_BUY)
    }

    companion object {
        @Volatile
        var trackingDirty: Boolean = false
    }
}
