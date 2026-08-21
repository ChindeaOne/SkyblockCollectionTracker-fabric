package io.github.chindeaone.collectiontracker.utils.rendering

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.RepoUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object RenderUtils {

    private val fr: Font get() = Minecraft.getInstance().font

    private data class QueuedTitle(val title: Component, val duration: Long)
    private val titleQueue = ArrayDeque<QueuedTitle>()

    @JvmStatic
    fun drawOverlayFrame(context: GuiGraphicsExtractor, pos: Position, drawContext: Runnable) {
        context.pose().pushMatrix()
        context.pose().translate(pos.x.toFloat(), pos.y.toFloat())
        context.pose().scale(pos.scale, pos.scale)

        drawContext.run()

        context.pose().popMatrix()
    }

    fun drawDummyFrame(context: GuiGraphicsExtractor, pos: Position, label: String) {
        val yPadding = 4
        val totalBoxHeight = pos.height + yPadding * 2
        val radius = (totalBoxHeight / 4).coerceAtMost(6)

        drawOverlayFrame(context, pos) {
            drawRoundedRect(context, 0, -yPadding, pos.width, totalBoxHeight, radius, ColorUtils.DUMMY_BG)

            val overlayText = Component.literal(label).withColor(Colors.GREEN.color)
            val textScale = 0.8f

            val textHeight = fr.lineHeight * textScale
            val centerYInBox = (totalBoxHeight - textHeight) / 2f

            val xPos = (pos.width / 2f) / textScale
            val yPos = (centerYInBox - yPadding * textScale) / textScale

            context.pose().pushMatrix()
            context.pose().scale(textScale, textScale)
            context.centeredText(fr, overlayText, xPos.toInt(), yPos.toInt(), Colors.WHITE.color)
            context.pose().popMatrix()
        }
    }

    @JvmStatic
    fun renderTrackingStringsWithColor(context: GuiGraphicsExtractor, lines: List<String>, extraLines: List<String>, withColor: Boolean) {
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

        val color: Int = if (withColor) (ColorUtils.collectionColors[CollectionTracker.collection]) ?: Colors.GREEN.color else Colors.GREEN.color

        if (color != Colors.GREEN.color) {
            val outlineShade = Colors.DARK_GRAY.color

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
    fun renderMultiTrackingStringsWithColor(context: GuiGraphicsExtractor, lines: List<String>, withColor: Boolean) {
        var y = 0

        for (line in lines) {
            var color: Int = Colors.GREEN.color
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

                    if (foundCollName.contains("Next Position") ||
                        foundCollName.contains("Till Next Position") ||
                        foundCollName.contains("ETA") ||
                        foundCollName.contains("Passed") ||
                        foundCollName.contains("Difference") ||
                        foundCollName.contains("Gemstones") ||
                        foundCollName.contains("Custom")) {
                        color = ColorUtils.collectionColors["gemstone"] ?: Colors.GREEN.color
                    } else if (foundCollName.contains(" ")) { // all gemstones when it's expanded
                        val firstWord = foundCollName.trim().substringBefore(' ').lowercase()
                        color = ColorUtils.collectionColors[firstWord.trim()] ?: Colors.GREEN.color
                    } else {
                        color = ColorUtils.collectionColors[foundCollName.lowercase().trim()] ?: Colors.GREEN.color
                    }
                }
            }

            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }
    }

    @JvmStatic
    fun renderSkillStringsWithTaming(context: GuiGraphicsExtractor, lines: List<String>, tamingLines: List<String>, withTaming: Boolean) {
        var y = 0

        val color: Int = (ColorUtils.skillColors[SkillTracker.skillName]) ?: Colors.GREEN.color
        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }

        if (withTaming) {
            y += fr.lineHeight
            val tamingColor: Int = (ColorUtils.skillColors["Taming"]) ?: Colors.GREEN.color
            for (line in tamingLines) {
                drawHelper(line, context, y, tamingColor)
                y += fr.lineHeight
            }
        }
    }

    @JvmStatic
    fun renderColeweightStrings(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0
        val color = ColorUtils.SILVER_BLUE

        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }
    }

    @JvmStatic
    fun renderStrings(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0

        for (line in lines) {
            context.text(fr, line, 0, y, Colors.WHITE.color, true)
            y += fr.lineHeight
        }
    }

    @JvmStatic
    fun renderCooldownCircle(context: GuiGraphicsExtractor, ability: String) {
        val centerX = ScaleUtils.scaledWidth / 2f - 1f
        val centerY = ScaleUtils.scaledHeight / 2f - 1f

        val (cooldown, duration, maxCooldown, maxDuration) = getAbilityTimes(ability)

        when {
            cooldown <= 0.0 -> {
                drawArc(context, centerX, centerY, -90f, 360f, Colors.GREEN.color)
            }

            duration > 0.0 -> {
                val progress = (duration / maxDuration).coerceIn(0.0, 1.0)
                val sweep = (360 * progress).toFloat()
                val start = -90f + (360f - sweep)

                drawArc(context, centerX, centerY, start, sweep, Colors.GREEN.color)
            }

            else -> {
                val progress = (1.0 - cooldown / maxCooldown).coerceIn(0.0, 1.0)

                drawArc(context, centerX, centerY, -90f, (-360 * progress).toFloat(), Colors.RED.color)
            }
        }
    }

    @JvmStatic
    fun renderCooldownBar(context: GuiGraphicsExtractor, ability: String) {
        val centerX = ScaleUtils.scaledWidth / 2f - 1f
        val centerY = ScaleUtils.scaledHeight / 2f - 1f + 8f

        val (cooldown, duration, maxCooldown, maxDuration) = getAbilityTimes(ability)

        when {
            cooldown <= 0.0 -> {
                drawBar(context, centerX, centerY, 11f, 2f, 1f, Colors.GREEN.color)
            }

            duration > 0.0 -> {
                val progress = (duration / maxDuration).coerceIn(0.0, 1.0).toFloat()
                drawBar(context, centerX, centerY, 11f, 2f, progress, Colors.GREEN.color)
            }

            else -> {
                val progress = (1.0 - cooldown / maxCooldown).coerceIn(0.0, 1.0).toFloat()
                drawBar(context, centerX, centerY, 11f, 2f, progress, Colors.RED.color)
            }
        }
    }

    private fun drawArc(context: GuiGraphicsExtractor, centerX: Float, centerY: Float, direction: Float, sweepAngle: Float, color: Int) {
        if (sweepAngle == 0f) return

        val cx = centerX.roundToInt()
        val cy = centerY.roundToInt()
        val count = arcPixels.size
        val startIdx = (normalizeAngle(direction + 90f) / 360f * count).toInt()
        val steps = (abs(sweepAngle) / 360f * count).toInt().coerceAtLeast(1)
        val dir = if (sweepAngle >= 0f) 1 else -1

        var idx = startIdx
        repeat(steps + 1) {
            val (ox, oy) = arcPixels[idx]
            context.fill(cx + ox, cy + oy, cx + ox + 1, cy + oy + 1, color)

            idx += dir
            if (idx < 0) idx = count - 1
            else if (idx >= count) idx = 0
        }
    }

    private fun normalizeAngle(angle: Float): Float = ((angle % 360f) + 360f) % 360f

    private val arcPixels: List<Pair<Int, Int>> = buildList {
        var last: Pair<Int, Int>? = null

        for (deg in 0..360) {
            val angle = Math.toRadians((deg - 90).toDouble())
            val point = (6f * cos(angle).toFloat()).roundToInt() to (6f * sin(angle).toFloat()).roundToInt()

            if (point != last) {
                add(point)
                last = point
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun drawBar(context: GuiGraphicsExtractor, centerX: Float, centerY: Float, width: Float, height: Float, progress: Float, color: Int) {
        val halfWidth = width / 2f
        val halfHeight = height / 2f

        val left = (centerX - halfWidth).roundToInt()
        val top = (centerY - halfHeight).roundToInt()
        val bottom = (centerY + halfHeight).roundToInt()

        val progressWidth = (width * progress).roundToInt()

        if (progressWidth > 0) {
            context.fill(left, top, left + progressWidth, bottom, color)
        }
    }

    data class AbilityTimes(
        val cooldown: Double,
        val duration: Double,
        val maxCooldown: Double,
        val maxDuration: Double
    )

    private fun getAbilityTimes(ability: String) = when (ability) {
        "axe" -> AbilityTimes(
            ChatListener.finalAxeCooldown,
            ChatListener.finalAxeDuration,
            ChatListener.maxAxeCooldown,
            ChatListener.maxAxeDuration
        )

        "pickaxe" -> AbilityTimes(
            ChatListener.finalCooldown,
            ChatListener.finalDuration,
            ChatListener.maxCooldown,
            ChatListener.maxDuration
        )

        else -> AbilityTimes(0.0, 0.0, 0.0, 0.0)
    }

    fun drawEditorHudText(context: GuiGraphicsExtractor, activePosition: Position?) {
        if (activePosition != null) {
            val x = ScaleUtils.mouseX + 12
            val y = ScaleUtils.mouseY - 12

            val scaleStr = String.format("%.2f\n\n", activePosition.scale)

            val positionText = Component.literal("Position Editor\n").withColor(Colors.BLUE.color)
                .append(Component.literal(" X: ").withColor(Colors.GRAY.color))
                .append(Component.literal("${activePosition.x}").withColor(Colors.YELLOW.color))
                .append(Component.literal("  Y: ").withColor(Colors.GRAY.color))
                .append(Component.literal("${activePosition.y}").withColor(Colors.YELLOW.color))
                .append(Component.literal("  Scale: ").withColor(Colors.GRAY.color))
                .append(Component.literal(scaleStr).withColor(Colors.AQUA.color))
                .append(Component.literal("Use mouse wheel to resize the overlay\n").withColor(Colors.YELLOW.color))
                .append(Component.literal("Use middle click to reset the scale\n").withColor(Colors.YELLOW.color))
                .append(Component.literal("Right-click to open the config").withColor(Colors.GOLD.color))

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    fun drawEditorHudTitle(context: GuiGraphicsExtractor, pos: Position?) {
        val textScale = 0.75f
        val resizeText = Component.literal("").withColor(Colors.GREEN.color)

        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.text(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), Colors.WHITE.color, true)
        context.pose().popMatrix()

        if (pos != null) {
            val x = ScaleUtils.mouseX + 12
            val y = ScaleUtils.mouseY - 12

            val positionText = Component.literal("Position Editor\n").withColor(Colors.BLUE.color)
                .append(Component.literal(" Y: ").withColor(Colors.GRAY.color))
                .append(Component.literal("${pos.y}\n\n").withColor(Colors.YELLOW.color))
                .append(Component.literal("You can only move the title vertically").withColor(Colors.YELLOW.color))

            drawTooltipsHelper(context, positionText, x, y)
        }
    }

    private fun drawTooltipsHelper(context: GuiGraphicsExtractor, positionText: Component, x: Int, y: Int) {
        val textScale = 0.85f
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
            context.text(fr, line, 0, yOffset, Colors.YELLOW.color, true)
        }
        context.pose().popMatrix()
    }

    private fun drawHelper(line: String, context: GuiGraphicsExtractor, y: Int, prefixColor: Int) {
        val splitIndex = line.lastIndexOf(": ")
        if (splitIndex != -1) {
            val prefix = line.substring(0, splitIndex)
            val numberPart = line.substring(splitIndex)

            context.text(fr, prefix, 0, y, prefixColor, true)

            val prefixWidth = fr.width(prefix)
            context.text(fr, numberPart,  prefixWidth, y, ColorUtils.CUSTOM_WHITE, true)
        } else {
            context.text(fr, line, 0, y, prefixColor, true)
        }
    }

    @JvmStatic
    fun showTitle(title: Component, duration: Long = getTitleDisplayTimer()) {
        if (titleQueue.isEmpty()) {
            titleQueue.add(QueuedTitle(title, System.currentTimeMillis() + duration))
        } else {
            titleQueue.add(QueuedTitle(title, titleQueue.last().duration + duration))
        }
    }

    @JvmStatic
    fun drawActiveTitle(context: GuiGraphicsExtractor) {
        val title = titleQueue.firstOrNull() ?: return
        if (System.currentTimeMillis() < title.duration) {
            renderTitle(context, title.title)
        } else titleQueue.removeFirst()
    }

    private fun renderTitle(context: GuiGraphicsExtractor, title: Component) {
        val screenWidth = context.guiWidth().toFloat()
        val screenHeight = context.guiHeight().toFloat()
        val pos = ConfigAccess.getTitlePosition()
        val scale = ConfigAccess.getTitleScale().scale * ScaleUtils.scale

        val y = if (pos.y == 0) ((screenHeight - (pos.height * scale))/ 2f) else pos.y.toFloat()
        val yOffset = (pos.height - fr.lineHeight) / 2f

        context.pose().pushMatrix()
        context.pose().translate(screenWidth / 2f, y)
        context.pose().scale(scale, scale)
        context.centeredText(fr, title, 0, yOffset.toInt(), Colors.WHITE.color)
        context.pose().popMatrix()
    }

    @JvmStatic
    fun renderChangelog(context: GuiGraphicsExtractor, scrollOffset: Int) {
        val rawNotes = RepoUtils.latestNotes ?: return
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
            context.centeredText(fr, "Version: $version", screenWidth / 2, startY - 20, Colors.GREEN.color)
        }

        context.enableScissor(startX, startY, startX + overlayWidth, startY + overlayHeight)
        renderChangelogLines(context, cleanNotes, startX, startY - scrollOffset, overlayWidth, startY, overlayHeight)
        context.disableScissor()
    }

    private fun renderChangelogLines(context: GuiGraphicsExtractor, text: String, startX: Int, startY: Int, overlayWidth: Int, limitStartY: Int, limitHeight: Int) {
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
                line.contains("## What's New") -> Colors.GREEN.color
                line.contains("## Improvements") -> Colors.YELLOW.color
                line.contains("## Bug Fixes") -> Colors.AQUA.color
                else -> Colors.WHITE.color
            }
            // clear Markdown
            val cleanLine = trimmed.replace("## ", "")
                .replace("**", "")
                .replace("`", "")
                .replace(referenceRegex, "")

            val wrappedLines = fr.split(Component.literal(cleanLine), overlayWidth)
            for (wrapped in wrappedLines) {
                if (currentY + fr.lineHeight >= limitStartY && currentY <= limitStartY + limitHeight)
                    context.text(fr, wrapped, startX, currentY, color, true)
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

    private fun drawTooltipBox(context: GuiGraphicsExtractor, x: Float, y: Float, width: Float, height: Float, padding: Float = 4f, borderColor: Int = Colors.GRAY.color) {
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

    private fun drawRoundedRect(context: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
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

    private fun drawOverlayOutline(context: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
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