package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarPriceType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBazaarType
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getGemstoneVariant
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getMultiOverlayPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isOverlayTextColorEnabled
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigHelper.changeBazaarPrice
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaar
import io.github.chindeaone.collectiontracker.config.ConfigHelper.setBazaarType
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderMultiTrackingStringsWithColor
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.ChatScreen
import kotlin.concurrent.Volatile

class MultiCollectionOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private val expandedCollections: MutableList<String> = mutableListOf()

    private var lastUptime: String = ""
    private var lastIsChatOpened: Boolean = false
    private var lastBzType: Bazaar.BazaarType? = null
    private var lastBzPriceType: Bazaar.BazaarPriceType? = null
    private var lastGemVariant: Bazaar.GemstoneVariant? = null
    private var lastExpandedHash: Int = 0

    override val overlayLabel: String = "Multi-Collection Tracker"

    override val position: Position get() = getMultiOverlayPosition()

    override val isEnabled: Boolean get() = MultiTrackingHandler.isMultiTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled || !trackingDirty) return

        val mainLines = lines
        if (mainLines.isEmpty()) return

        drawOverlayFrame(context, position) { renderMultiTrackingStringsWithColor(context, mainLines, isOverlayTextColorEnabled()) }
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
            }
            return
        }

        val uptime = MultiTrackingHandler.multiUptime
        val isChatOpened = Minecraft.getInstance()./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/ is ChatScreen
        val bzType = getBazaarType()
        val bzPriceType = getBazaarPriceType()
        val gemVariant = getGemstoneVariant()
        val expandedHash = expandedCollections.hashCode()

        if (cachedLines.isNotEmpty() && uptime == lastUptime && isChatOpened == lastIsChatOpened
            && bzType == lastBzType && bzPriceType == lastBzPriceType && gemVariant == lastGemVariant
            && expandedHash == lastExpandedHash) return

        lastUptime = uptime
        lastIsChatOpened = isChatOpened
        lastBzType = bzType
        lastBzPriceType = bzPriceType
        lastGemVariant = gemVariant
        lastExpandedHash = expandedHash

        val newLines = mutableListOf<String>()
        TextUtils.updateMultiTrackingLines(newLines, expandedCollections, isChatOpened)
        newLines.add("Uptime: $uptime")

        if (isChatOpened) {
            TextUtils.addToggleableSettingsLines(newLines)
        }

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

        when (line) {
            "§e[Bazaar Prices]" -> setBazaar(true)
            "§e[NPC Prices]" -> setBazaar(false)
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

        val position = position
        val x = position.x
        val y = position.y
        val scale = position.scale

        val height = (position.height * scale).toInt()
        val width = (position.width * scale).toInt()

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
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
