package io.github.chindeaone.collectiontracker.utils.rendering

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.serverapi.RepoUtils
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

object RenderUtils {

    private val fr: Font get() = Minecraft.getInstance().font
    private const val DUMMY_BG = -0x7fbfbfc0

    private var activeTitle: Component? = null
    private var titleExpireTime: Long = 0

    @JvmStatic
    fun drawOverlayFrame(context: GuiGraphics, pos: Position, drawContext: Runnable) {
        context.pose().pushMatrix()
        context.pose().translate(pos.x.toFloat(), pos.y.toFloat())
        context.pose().scale(pos.scale, pos.scale)

        drawContext.run()

        context.pose().popMatrix()
    }

    @JvmStatic
    fun drawDummyFrame(context: GuiGraphics, pos: Position, label: String) {
        drawOverlayFrame(context, pos) {
            context.fill(0, 0, pos.width, pos.height, DUMMY_BG)

            val overlayText = Component.literal(label)
                .withStyle(ChatFormatting.GREEN)

            val textScale = 0.8f
            val centerX = pos.width / 2f
            val yTop = (pos.height - fr.lineHeight * textScale) / 2f

            context.pose().pushMatrix()
            context.pose().scale(textScale, textScale)
            context.drawCenteredString(fr, overlayText, (centerX / textScale).toInt(), (yTop / textScale).toInt(), ColorUtils.WHITE)
            context.pose().popMatrix()
        }
    }

    @JvmStatic
    fun renderTrackingStringsWithColor(context: GuiGraphics, lines: List<String>, extraLines: List<String>, withColor: Boolean) {
        var y = 0

        val color: Int = if (withColor) (ColorUtils.collectionColors[CollectionTracker.collection]) ?: ColorUtils.GREEN  else ColorUtils.GREEN
        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }

        if (extraLines.isNotEmpty()) {
            y += fr.lineHeight
            for (line in extraLines) {
                drawHelper(line, context, y, color)
                y += fr.lineHeight
            }
        }
    }

    @JvmStatic
    fun renderSkillStringsWithTaming(context: GuiGraphics, lines: List<String>, tamingLines: List<String>, withTaming: Boolean) {
        var y = 0

        val color: Int = (ColorUtils.skillColors[SkillTracker.skillName]) ?: ColorUtils.GREEN
        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }

        if (withTaming) {
            y += fr.lineHeight
            val tamingColor: Int = (ColorUtils.skillColors["Taming"]) ?: ColorUtils.GREEN
            for (line in tamingLines) {
                drawHelper(line, context, y, tamingColor)
                y += fr.lineHeight
            }
        }
    }

    @JvmStatic
    fun renderStrings(context: GuiGraphics, lines: List<String>) {
        var y = 0

        for (line in lines) {
            context.drawString(fr, line, 0, y, ColorUtils.WHITE, true)
            y += fr.lineHeight
        }
    }

    @JvmStatic
    fun drawEditorHudText(context: GuiGraphics, activePosition: Position?) {
        val textScale = 0.8f

        val resizeText = Component.literal("Use mouse wheel to resize the overlay")
            .withStyle(ChatFormatting.GREEN)
        val resetText = Component.literal("Right click to reset the scale")
            .withStyle(ChatFormatting.GREEN)

        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), ColorUtils.WHITE, true)
        context.pose().popMatrix()

        val resetWidth = fr.width(resetText)
        val resetX = (context.guiWidth() / 2f) - (resetWidth * textScale / 2f)
        val resetY = textY + fr.lineHeight + 2f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resetText, (resetX / textScale).toInt(), (resetY / textScale).toInt(), ColorUtils.WHITE, true)
        context.pose().popMatrix()

        if (activePosition != null) {
            val x = ScaleUtils.mouseX + 12
            val y = ScaleUtils.mouseY - 12

            val scaleStr = String.format("%.2f", activePosition.scale)
            val positionText = Component.literal("X: ${activePosition.x} Y: ${activePosition.y} Scale: $scaleStr")
                .withStyle(ChatFormatting.YELLOW)

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    @JvmStatic
    fun drawEditorHudTitle(context: GuiGraphics, activePosition: Position?) {
        val textScale = 0.8f

        val resizeText = Component.literal("You can move the title position only vertically!")
            .withStyle(ChatFormatting.GREEN)

        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), ColorUtils.WHITE, true)
        context.pose().popMatrix()

        if (activePosition != null) {
            val x = ScaleUtils.mouseX + 12
            val y = ScaleUtils.mouseY - 12

            val positionText = Component.literal("Y: ${activePosition.y}")
                .withStyle(ChatFormatting.YELLOW)

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    private fun drawTooltipsHelper(context: GuiGraphics, positionText: Component, x: Int, y: Int) {
        val textScale = 0.8f

        val positionWidth = fr.width(positionText) * textScale
        val positionHeight = fr.lineHeight * textScale
        val positionY = when {
            y < 8 -> 4f
            y + fr.lineHeight > context.guiHeight() -> (context.guiHeight() - fr.lineHeight - 6).toFloat()
            else -> y.toFloat()
        }
        val positionX = if (x + positionWidth > context.guiWidth()) {
            (context.guiWidth() - positionWidth - 6)
        } else {
            x.toFloat()
        }

        drawTooltipBox(context, positionX, positionY, positionWidth, positionHeight)

        context.pose().pushMatrix()
        context.pose().translate(positionX, positionY)
        context.pose().scale(textScale, textScale)
        context.drawString(fr, positionText, 0, 0, ColorUtils.YELLOW, true)
        context.pose().popMatrix()
    }

    private fun drawHelper(line: String, context: GuiGraphics, y: Int, prefixColor: Int) {
        val splitIndex = line.lastIndexOf(": ")
        if (splitIndex != -1) {
            val prefix = line.substring(0, splitIndex)
            val numberPart = line.substring(splitIndex)

            context.drawString(fr, prefix, 0, y, prefixColor, true)

            val prefixWidth = fr.width(prefix)
            context.drawString(fr, numberPart,  prefixWidth, y, ColorUtils.CUSTOM_WHITE, true)
        } else {
            context.drawString(fr, line, 0, y, prefixColor, true)
        }
    }

    @JvmStatic
    fun showTitle(title: Component, durationMs: Int = getTitleDisplayTimer()) {
        activeTitle = title
        titleExpireTime = System.currentTimeMillis() + durationMs * 1000
    }

    @JvmStatic
    fun drawActiveTitle(context: GuiGraphics) {
        val title = activeTitle ?: return
        if (System.currentTimeMillis() < titleExpireTime) {
            renderTitle(context, title)
        } else activeTitle = null
    }

    private fun renderTitle(context: GuiGraphics, title: Component) {
        val screenWidth = context.guiWidth().toFloat()
        val screenHeight = context.guiHeight().toFloat()
        val pos = ConfigAccess.getTitlePosition()
        val scale = ConfigAccess.getTitleScale().scale * ScaleUtils.scale

        val y = if (pos.y == 0) ((screenHeight - (pos.height * scale))/ 2f) else pos.y.toFloat()
        val yOffset = (pos.height - fr.lineHeight) / 2f

        context.pose().pushMatrix()
        context.pose().translate(screenWidth / 2f, y)
        context.pose().scale(scale, scale)
        context.drawCenteredString(fr, title, 0, yOffset.toInt(), ColorUtils.WHITE)
        context.pose().popMatrix()
    }

    @JvmStatic
    fun renderChangelog(context: GuiGraphics, scrollOffset: Int) {
        val rawNotes = RepoUtils.latestNotes
        if (rawNotes.isEmpty()) return
        val footerIndex = rawNotes.indexOf("**Full Changelog**")
        val cleanNotes = if (footerIndex != -1) rawNotes.substring(0, footerIndex) else rawNotes

        val screenWidth = context.guiWidth()
        val screenHeight = context.guiHeight()

        val overlayWidth = screenWidth / 2
        val overlayHeight = (screenHeight * 0.75f).toInt()
        val startX = (screenWidth - overlayWidth) / 2
        val startY = (screenHeight - overlayHeight) / 2

        context.fill((startX - 10), startY - 10, startX + overlayWidth + 10, startY + overlayHeight + 10, -0x7f000000)

        // Render current version first
        SkyblockCollectionTracker.VERSION.let { version ->
            context.drawCenteredString(fr, "Version: $version", screenWidth / 2, startY - 20, ColorUtils.GREEN)
        }

        context.enableScissor(startX, startY, startX + overlayWidth, startY + overlayHeight)
        renderChangelogLines(context, cleanNotes, startX, startY - scrollOffset, overlayWidth, startY, overlayHeight)
        context.disableScissor()
    }

    private fun renderChangelogLines(context: GuiGraphics, text: String, startX: Int, startY: Int, overlayWidth: Int, limitStartY: Int, limitHeight: Int) {
        val lines = text.split(Regex("\r?\n"))
        var currentY = startY
        val referenceRegex = Regex("""\(#\d+\)""")

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.isEmpty() || trimmed == "---") {
                currentY += fr.lineHeight / 2
                continue
            }
            // Set header colors
            val color = when {
                line.contains("## What's New") -> ColorUtils.GREEN
                line.contains("## Improvements") -> ColorUtils.YELLOW
                line.contains("## Bug Fixes") -> ColorUtils.AQUA
                else -> ColorUtils.WHITE
            }
            // clear Markdown
            val cleanLine = trimmed.replace("## ", "")
                .replace("**", "")
                .replace("`", "")
                .replace(referenceRegex, "")

            val wrappedLines = fr.split(Component.literal(cleanLine), overlayWidth)
            for (wrapped in wrappedLines) {
                if (currentY + fr.lineHeight >= limitStartY && currentY <= limitStartY + limitHeight)
                    context.drawString(fr, wrapped, startX, currentY, color, true)
                currentY += fr.lineHeight
            }
        }
    }

    fun getChangelogHeight(screenWidth: Int): Int {
        val text = RepoUtils.latestNotes ?: return 0
        val overlayWidth = screenWidth / 2
        val footerIndex = text.indexOf("**Full Changelog**")
        val cleanNotes = if (footerIndex != -1) text.substring(0, footerIndex) else text

        val lines = cleanNotes.split(Regex("\r?\n"))
        var totalHeight = 0
        val referenceRegex = Regex("""\(#\d+\)""")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed == "---") {
                totalHeight += fr.lineHeight / 2
                continue
            }

            val cleanLine = trimmed.replace("## ", "")
                .replace("**", "")
                .replace("`", "")
                .replace(referenceRegex, "")

            val wrappedLines = fr.split(Component.literal(cleanLine), overlayWidth)
            totalHeight += wrappedLines.size * fr.lineHeight
        }
        return totalHeight
    }

    private fun drawTooltipBox(context: GuiGraphics, x: Float, y: Float, width: Float, height: Float, padding: Float = 4f, borderColor: Int = ColorUtils.GRAY) {
        val x1 = (x - padding).toInt()
        val y1 = (y - padding).toInt()
        val x2 = (x + width + padding).toInt()
        val y2 = (y + height + padding).toInt()

        context.fill(x1, y1, x2, y2, 0x90000000.toInt())

        context.fill(x1, y1, x2, y1 + 1, borderColor) // Top
        context.fill(x1, y2 - 1, x2, y2, borderColor) // Bottom
        context.fill(x1, y1, x1 + 1, y2, borderColor) // Left
        context.fill(x2 - 1, y1, x2, y2, borderColor) // Right
    }
}