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
import kotlin.math.sqrt

object RenderUtils {

    private val fr: Font get() = Minecraft.getInstance().font
    private const val DUMMY_BG = -0x7fbfbfc0

    private data class QueuedTitle(val title: Component, val duration: Long)
    private val titleQueue = ArrayDeque<QueuedTitle>()

    @JvmStatic
    fun drawOverlayFrame(context: GuiGraphics, pos: Position, drawContext: Runnable) {
        context.pose().pushMatrix()
        context.pose().translate(pos.x.toFloat(), pos.y.toFloat())
        context.pose().scale(pos.scale, pos.scale)

        drawContext.run()

        context.pose().popMatrix()
    }

    fun drawDummyFrame(context: GuiGraphics, pos: Position, label: String) {
        val yPadding = 4
        val totalBoxHeight = pos.height + yPadding * 2
        val radius = (totalBoxHeight / 4).coerceAtMost(6)

        drawOverlayFrame(context, pos) {
            drawRoundedRect(context, 0, -yPadding, pos.width, totalBoxHeight, radius, DUMMY_BG)

            val overlayText = Component.literal(label).withStyle(ChatFormatting.GREEN)
            val textScale = 0.8f

            val textHeight = fr.lineHeight * textScale
            val centerYInBox = (totalBoxHeight - textHeight) / 2f

            val xPos = (pos.width / 2f) / textScale
            val yPos = (centerYInBox - yPadding * textScale) / textScale

            context.pose().pushMatrix()
            context.pose().scale(textScale, textScale)
            context.drawCenteredString(fr, overlayText, xPos.toInt(), yPos.toInt(), ColorUtils.WHITE)
            context.pose().popMatrix()
        }
    }

    @JvmStatic
    fun renderTrackingStringsWithColor(context: GuiGraphics, lines: List<String>, extraLines: List<String>, withColor: Boolean) {
        var y = 0

        val allLines = mutableListOf<String>()
        allLines.addAll(lines)
        if (extraLines.isNotEmpty()) {
            allLines.add("")
            allLines.addAll(extraLines)
        }

        val maxTextWidth = allLines.maxOfOrNull { fr.width(it) } ?: 0
        val totalTextHeight = allLines.size * fr.lineHeight

        val padding = 8
        val overlayW = maxTextWidth + padding * 2
        val overlayH = totalTextHeight + padding * 2

        val radius = (overlayH / 12).coerceAtLeast(1)

        val color: Int = if (withColor) (ColorUtils.collectionColors[CollectionTracker.collection]) ?: ColorUtils.GREEN  else ColorUtils.GREEN

        if (color != ColorUtils.GREEN) {
            val outlineShade = ColorUtils.DARK_GRAY

            val startX = -padding
            val startY = -padding
            val baseR = radius.coerceAtMost(overlayW / 2).coerceAtMost(overlayH / 2)

            if (baseR >= 3) {
                drawOverlayOutline(context, startX, startY, overlayW, overlayH, baseR, outlineShade)
                drawOverlayOutline(
                    context,
                    startX + 1,
                    startY + 1,
                    overlayW - 2,
                    overlayH - 2,
                    (baseR - 1).coerceAtLeast(1),
                    color
                )
                drawOverlayOutline(
                    context,
                    startX + 2,
                    startY + 2,
                    overlayW - 4,
                    overlayH - 4,
                    (baseR - 2).coerceAtLeast(1),
                    outlineShade
                )
            } else {
                drawOverlayOutline(context, startX, startY, overlayW, overlayH, baseR, outlineShade)
            }
        }

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
    fun renderMultiTrackingStringsWithColor(context: GuiGraphics, lines: List<String>, withColor: Boolean) {
        var y = 0

        for (line in lines) {
            var color: Int = ColorUtils.GREEN
            if (withColor) {
                val splitIndex = line.indexOf(": ")
                if (splitIndex != -1) {
                    val prefix = line.substring(0, splitIndex)
                    // Check prefixes
                    val delimiters = arrayOf(" collection", " $/h", " $ made", " Coll/h", " Motes")
                    var foundCollName = prefix
                        .replace("§e[+]§r ", "")
                        .replace("§e[-]§r ", "")

                    for (delim in delimiters) {
                        if (foundCollName.contains(delim)) {
                            foundCollName = foundCollName.substring(0, foundCollName.indexOf(delim))
                            break
                        }
                    }

                    if (foundCollName.contains("Next Position") || foundCollName.contains("Till Next Position") || foundCollName.contains("ETA")) {
                        color = ColorUtils.collectionColors["gemstone"]!!
                    } else if (foundCollName.contains(" ")) {
                        val firstWord = foundCollName.split(" ")[0].lowercase()
                        val gemstoneTypes = arrayOf("ruby", "sapphire", "topaz", "amethyst", "jade", "jasper", "amber", "opal", "aquamarine", "peridot", "citrine", "onyx")
                        color = if (gemstoneTypes.contains(firstWord)) {
                            ColorUtils.collectionColors[firstWord.trim()] ?: ColorUtils.GREEN
                        } else {
                            ColorUtils.collectionColors[firstWord.trim()] ?: ColorUtils.GREEN
                        }
                    } else {
                        color = ColorUtils.collectionColors[foundCollName.lowercase().trim()] ?: ColorUtils.GREEN
                    }
                }
            }

            drawHelper(line, context, y, color)
            y += fr.lineHeight
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
    fun renderColeweightStrings(context: GuiGraphics, lines: List<String>) {
        var y = 0
        val color = ColorUtils.SILVER_BLUE

        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
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

    fun drawEditorHudText(context: GuiGraphics, activePosition: Position?) {
        val textScale = 0.75f

        val resizeText = Component.literal("Use mouse wheel to resize the overlay").withStyle(ChatFormatting.YELLOW)
        val resetText = Component.literal("Middle click to reset the scale").withStyle(ChatFormatting.YELLOW)

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

            val positionText = Component.literal("OVERLAY POSITION").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(" (top-left corner)").withStyle(ChatFormatting.DARK_GRAY))
                .append("\n")
                .append(Component.literal("X: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("${activePosition.x}").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("  Y: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("${activePosition.y}").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("  Scale: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(scaleStr).withStyle(ChatFormatting.AQUA)))

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    fun drawEditorHudTitle(context: GuiGraphics, pos: Position?) {
        val textScale = 0.75f
        val resizeText = Component.literal("You can move the title position only vertically!").withStyle(ChatFormatting.GREEN)

        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), ColorUtils.WHITE, true)
        context.pose().popMatrix()

        if (pos != null) {
            val x = ScaleUtils.mouseX + 12
            val y = ScaleUtils.mouseY - 12

            val positionText = Component.literal("TITLE POSITION").withStyle(ChatFormatting.BLUE)
                .append("\n")
                .append(Component.literal("Vertical Y: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("${pos.y}").withStyle(ChatFormatting.YELLOW))

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    private fun drawTooltipsHelper(context: GuiGraphics, positionText: Component, x: Int, y: Int) {
        val textScale = 0.75f
        val padding = 2
        val space = 2

        val lines = fr.split(positionText, 1000)
        val maxTextWidth = lines.maxOfOrNull { fr.width(it) } ?: 0

        val positionWidth = maxTextWidth * textScale
        val maxHeight = (lines.size * fr.lineHeight + (lines.size - 1) * space) * textScale

        val positionY = (y.toFloat()).coerceIn(8f, context.guiHeight() - maxHeight - padding * 2 - 8f)
        val positionX = (x.toFloat()).coerceIn(8f, context.guiWidth() - positionWidth - padding * 2 - 8f)

        drawTooltipBox(context, positionX, positionY, positionWidth, maxHeight)

        context.pose().pushMatrix()
        context.pose().translate(positionX, positionY)
        context.pose().scale(textScale, textScale)
        lines.forEachIndexed { index, line ->
            val yOffset = index * (fr.lineHeight + space)
            context.drawString(fr, line, 0, yOffset, ColorUtils.YELLOW, true)
        }
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
    fun showTitle(title: Component, duration: Int = getTitleDisplayTimer()) {
        if (titleQueue.isEmpty()) {
            titleQueue.add(QueuedTitle(title, System.currentTimeMillis() + duration * 1000L))
        } else {
            titleQueue.add(QueuedTitle(title, titleQueue.last().duration + duration * 1000L))
        }
    }

    @JvmStatic
    fun drawActiveTitle(context: GuiGraphics) {
        val title = titleQueue.firstOrNull() ?: return
        if (System.currentTimeMillis() < title.duration) {
            renderTitle(context, title.title)
        } else titleQueue.removeFirst()
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

        drawRoundedRect(context, startX - 10, startY - 10, overlayWidth + 20, overlayHeight + 20, 8, -0x6f000000)

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
            val trimmed = line.trimEnd()

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

    private fun drawRoundedRect(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
        if (radius <= 0) {
            context.fill(x, y, x + width, y + height, color)
            return
        }

        val r = radius.coerceAtMost(width / 2).coerceAtMost(height / 2)
        val alpha = (color shr 24 and 0xFF)
        val rgb = color and 0xFFFFFF

        // main
        context.fill(x + r, y, x + width - r, y + r, color)
        context.fill(x, y + r, x + width, y + height - r, color)
        context.fill(x + r, y + height - r, x + width - r, y + height, color)

        // corners with AA
        for (cx in 0 until r) {
            for (cy in 0 until r) {
                val dx = (r - cx - 0.5)
                val dy = (r - cy - 0.5)
                val dist = sqrt(dx * dx + dy * dy)

                val currAlpha = when {
                    dist < r - 1.0 -> alpha // fully opaque
                    dist < r -> ((r - dist) * alpha).toInt()
                    else -> 0
                }

                if (currAlpha > 0) {
                    val newColor = (currAlpha shl 24) or rgb

                    context.fill(x + cx, y + cy, x + cx + 1, y + cy + 1, newColor) // top left
                    context.fill(x + width - cx - 1, y + cy, x + width - cx, y + cy + 1, newColor) // top right
                    context.fill(x + cx, y + height - cy - 1, x + cx + 1, y + height - cy, newColor) // bottom left
                    context.fill(x + width - cx - 1, y + height - cy - 1, x + width - cx, y + height - cy, newColor) // bottom right
                }
            }
        }
    }

    private fun drawOverlayOutline(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
        val r = radius.coerceAtMost(width / 2).coerceAtMost(height / 2)

        context.fill(x + r, y, x + width - r, y + 1, color)
        context.fill(x + r, y + height - 1, x + width - r, y + height, color)
        context.fill(x, y + r, x + 1, y + height - r, color)
        context.fill(x + width - 1, y + r, x + width, y + height - r, color)

        val innerR = r - 1
        for (cx in 0 until r) {
            for (cy in 0 until r) {
                val dx = (r - cx - 0.5)
                val dy = (r - cy - 0.5)
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < r && dist >= innerR) {
                    context.fill(x + cx, y + cy, x + cx + 1, y + cy + 1, color) // top left
                    context.fill(x + width - cx - 1, y + cy, x + width - cx, y + cy + 1, color) // top right
                    context.fill(x + cx, y + height - cy - 1, x + cx + 1, y + height - cy, color) // bottom left
                    context.fill(x + width - cx - 1, y + height - cy - 1, x + width - cx, y + height - cy, color) // bottom right
                }
            }
        }
    }
}