package io.github.chindeaone.collectiontracker.utils.render

import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableTamingTracking
import io.github.chindeaone.collectiontracker.config.overlayTextColor
import io.github.chindeaone.collectiontracker.config.titleDisplayTimer
import io.github.chindeaone.collectiontracker.config.titlePosition
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils.font
import io.github.chindeaone.collectiontracker.utils.RepoUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object RenderUtils {

    private data class QueuedTitle(val title: Component, val duration: Long)
    private val titleQueue = ArrayDeque<QueuedTitle>()

    fun drawOverlayFrame(context: GuiGraphicsExtractor, pos: Position, drawContext: Runnable) {
        context.pushPopMatrix {
            context.translate(pos.x, pos.y)
            context.scale(pos.scale, pos.scale)

            drawContext.run()
        }
    }

    fun drawDummyFrame(context: GuiGraphicsExtractor, pos: Position, label: String) {
        val yPadding = 4
        val totalBoxHeight = pos.height + yPadding * 2
        val radius = (totalBoxHeight / 4).coerceAtMost(6)

        drawOverlayFrame(context, pos) {
            context.drawRoundedRect(pos.width, totalBoxHeight, radius)

            val overlayText = Component.literal(label).withColor(Colors.GREEN.color)
            val textScale = 0.8f

            val textHeight = font.lineHeight * textScale
            val centerYInBox = (totalBoxHeight - textHeight) / 2f

            val xPos = (pos.width / 2f) / textScale
            val yPos = (centerYInBox - yPadding * textScale) / textScale

            context.pushPopMatrix {
                context.scale(textScale, textScale)
                context.centeredText(font, overlayText, xPos.toInt(), yPos.toInt(), Colors.WHITE.color)
            }
        }
    }

    fun renderTrackingStringsWithColor(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0

        val withColor = overlayTextColor
        val color: Int = if (overlayTextColor) (ColorUtils.collectionColors[CollectionTracker.collection]) ?: Colors.GREEN.color else Colors.GREEN.color

        if (withColor) {
            drawLayeredOutline(context, lines, color)
        }

        for (line in lines) {
            drawHelper(line, context, y, color)
            y += font.lineHeight
        }
    }

    fun renderMultiTrackingStringsWithColor(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0

        val withColor = overlayTextColor
        val outlineColor: Int = if (CollectionTracker.collectionList.size == 1) ColorUtils.collectionColors["gemstone"] ?: Colors.BLACK.color else Colors.BLACK.color

        if (withColor) {
            drawLayeredOutline(context, lines, outlineColor)
        }

        for (line in lines) {
            var color: Int = Colors.GREEN.color
            if (withColor) {
                val splitIndex = line.indexOf(": ")
                if (splitIndex != -1) {
                    val collectionName = line.substringBefore(": ")
                        .replace("§e[+]§r ", "")
                        .replace("§e[-]§r ", "")
                        .lowercase()
                        .trimEnd()
                        .let { name ->
                            listOf(" $/h (bazaar)", " $/h (npc)", " $ made (bazaar)", " $ made (npc)", " coll/h", " motes/h", " motes made", " (session)")
                                .find(name::endsWith)
                                ?.let(name::removeSuffix)
                                ?: name
                        }

                    when {
                        collectionName.startsWith(" ") -> { // all gemstones when it's expanded
                            val firstWord = collectionName.trimStart().substringBefore(' ')
                            color = ColorUtils.collectionColors[firstWord] ?: Colors.GREEN.color
                        }
                        collectionName.contains("gemstone") || CollectionTracker.collectionList.size == 1 -> {
                            color = ColorUtils.collectionColors["gemstone"] ?: Colors.GREEN.color
                        }
                        else -> {
                            color = ColorUtils.collectionColors[collectionName] ?: Colors.GREEN.color
                        }
                    }
                }
            }

            drawHelper(line, context, y, color)
            y += font.lineHeight
        }
    }

    fun renderSkillStringsWithTaming(context: GuiGraphicsExtractor, lines: List<String>, tamingLines: List<String>) {
        var y = 0

        val withColor = overlayTextColor
        val outlineColor: Int = (ColorUtils.skillColors[SkillTracker.skillName]) ?: Colors.GREEN.color

        if (withColor) {
            drawLayeredOutline(context, lines + "" + tamingLines, outlineColor)
        }

        val color: Int = (ColorUtils.skillColors[SkillTracker.skillName]) ?: Colors.GREEN.color
        for (line in lines) {
            drawHelper(line, context, y, color)
            y += font.lineHeight
        }

        if (!enableTamingTracking || SkillTracker.skillName == "Taming") return

        val tamingColor: Int = (ColorUtils.skillColors["Taming"]) ?: Colors.GREEN.color

        y += font.lineHeight
        for (line in tamingLines) {
            drawHelper(line, context, y, tamingColor)
            y += font.lineHeight
        }
    }

    fun renderColeweightStrings(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0
        val color = ColorUtils.COLEWEIGHT

        for (line in lines) {
            drawHelper(line, context, y, color)
            y += font.lineHeight
        }
    }

    fun renderStrings(context: GuiGraphicsExtractor, lines: List<String>) {
        var y = 0

        for (line in lines) {
            context.text(font, line, 0, y, Colors.WHITE.color, true)
            y += font.lineHeight
        }
    }

    fun renderCooldownCircle(context: GuiGraphicsExtractor, ability: String) {
        val cx = (ScaleUtils.scaledWidth / 2f - 1f).roundToInt()
        val cy = (ScaleUtils.scaledHeight / 2f - 1f).roundToInt()

        val (cooldown, duration, maxCooldown, maxDuration) = getAbilityTimes(ability)

        when {
            cooldown <= 0.0 -> drawArc(context, cx, cy, -90f, 360f, Colors.GREEN.color)

            duration > 0.0 -> {
                val progress = (duration / maxDuration).coerceIn(0.0, 1.0)

                val sweep = (360 * progress).toFloat()
                val start = -90f + (360f - sweep)

                drawArc(context, cx, cy, start, sweep, Colors.GREEN.color)
            }

            else -> {
                val progress = (1.0 - cooldown / maxCooldown).coerceIn(0.0, 1.0)

                drawArc(context, cx, cy, -90f, (-360 * progress).toFloat(), Colors.RED.color)
            }
        }
    }

    fun renderCooldownBar(context: GuiGraphicsExtractor, ability: String) {
        val centerX = (ScaleUtils.scaledWidth / 2f - 1f).roundToInt()
        val centerY = (ScaleUtils.scaledHeight / 2f - 1f + 8f).roundToInt()

        val (cooldown, duration, maxCooldown, maxDuration) = getAbilityTimes(ability)

        when {
            cooldown <= 0.0 -> drawBar(context, centerX, centerY, 1f, Colors.GREEN.color)

            duration > 0.0 -> {
                val progress = (duration / maxDuration).coerceIn(0.0, 1.0).toFloat()
                drawBar(context, centerX, centerY, progress, Colors.GREEN.color)
            }

            else -> {
                val progress = (1.0 - cooldown / maxCooldown).coerceIn(0.0, 1.0).toFloat()
                drawBar(context, centerX, centerY, progress, Colors.RED.color)
            }
        }
    }

    private fun drawArc(context: GuiGraphicsExtractor, cx: Int, cy: Int, direction: Float, sweepAngle: Float, color: Int) {
        if (sweepAngle == 0f) return

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

    private val arcPixels: List<Pair<Int, Int>> =
        buildList {
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

    private fun drawBar(context: GuiGraphicsExtractor, centerX: Int, centerY: Int, progress: Float, color: Int) {
        val left = centerX - 5
        val top = centerY - 1
        val right = centerX + 5
        val bottom = centerY + 1

        val clampedProgress = progress.coerceIn(0f, 1f)
        val progressWidth = ((right - left) * clampedProgress).roundToInt()

        context.fill(left - 1, top - 1, right + 1, bottom + 1, Colors.DARK_GRAY.color)

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

    fun renderMilestoneStrings(context: GuiGraphicsExtractor, lines: List<String>, isCollection: Boolean = true) {
        var y = 0

        drawLayeredOutline(context, lines, Colors.GOLD.color)

        for (line in lines) {
            val milestoneName = line.substringBefore(": ").trim()
            val color = if (isCollection) ColorUtils.collectionColors[milestoneName.lowercase()] ?: Colors.GREEN.color else ColorUtils.skillColors[milestoneName] ?: Colors.GREEN.color

            drawHelper(line, context, y, color)
            y += font.lineHeight
        }
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

        val lines = font.split(positionText, 1000)
        val maxTextWidth = lines.maxOfOrNull { font.width(it) } ?: 0

        val positionWidth = (maxTextWidth * textScale).toInt()
        val maxHeight = ((lines.size * font.lineHeight + (lines.size - 1) * space) * textScale).toInt()

        val positionX = x.coerceIn(8, ScaleUtils.scaledWidth - positionWidth - padding * 2 - 8)
        val positionY = y.coerceIn(8, ScaleUtils.scaledHeight - maxHeight - padding * 2 - 8)

        context.drawTooltipBox(positionX, positionY, positionWidth, maxHeight)

        context.pushPopMatrix {
            context.translate(positionX, positionY)
            context.scale(textScale, textScale)
            lines.forEachIndexed { index, line ->
                val yOffset = index * (font.lineHeight + space)
                context.text(font, line, 0, yOffset, Colors.YELLOW.color, true)
            }
        }
    }

    private fun drawHelper(line: String, context: GuiGraphicsExtractor, y: Int, prefixColor: Int) {
        val splitIndex = line.indexOf(": ")
        if (splitIndex != -1) {
            val prefix = line.substring(0, splitIndex)
            val numberPart = line.substring(splitIndex)

            context.text(font, prefix, 0, y, prefixColor, true)

            val prefixWidth = font.width(prefix)
            context.text(font, numberPart,  prefixWidth, y, ColorUtils.CUSTOM_WHITE, true)
        } else {
            context.text(font, line, 0, y, prefixColor, true)
        }
    }

    fun showTitle(title: Component, duration: Long = titleDisplayTimer) {
        if (titleQueue.isEmpty()) {
            titleQueue.add(QueuedTitle(title, System.currentTimeMillis() + duration))
        } else {
            titleQueue.add(QueuedTitle(title, titleQueue.last().duration + duration))
        }
    }

    fun drawActiveTitle(context: GuiGraphicsExtractor) {
        val title = titleQueue.firstOrNull() ?: return
        if (System.currentTimeMillis() < title.duration) {
            renderTitle(context, title.title)
        } else titleQueue.removeFirst()
    }

    private fun renderTitle(context: GuiGraphicsExtractor, title: Component) {
        val screenWidth = context.guiWidth().toFloat()
        val screenHeight = context.guiHeight().toFloat()
        val pos = titlePosition
        val scale = pos.scale * ScaleUtils.scale

        val y = if (pos.y == 0) ((screenHeight - (pos.height * scale))/ 2f) else pos.y.toFloat()
        val yOffset = (pos.height - font.lineHeight) / 2f

        context.pushPopMatrix {
            context.translate(screenWidth / 2f, y)
            context.scale(scale, scale)
            context.centeredText(font, title, 0, yOffset.toInt(), Colors.WHITE.color)
        }
    }

    fun renderChangelogLines(context: GuiGraphicsExtractor, text: String, startX: Int, startY: Int, overlayWidth: Int, limitStartY: Int, limitHeight: Int) {
        val lines = text.split(Regex("\r?\n"))
        var currentY = startY
        val referenceRegex = Regex("""\(#\d+\)""")

        for (line in lines) {
            val trimmed = line.trimEnd()

            if (trimmed.isEmpty() || trimmed == "---") {
                currentY += font.lineHeight / 2
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

            val wrappedLines = font.split(Component.literal(cleanLine), overlayWidth)
            for (wrapped in wrappedLines) {
                if (currentY + font.lineHeight >= limitStartY && currentY <= limitStartY + limitHeight)
                    context.text(font, wrapped, startX, currentY, color, true)
                currentY += font.lineHeight
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
                totalHeight += font.lineHeight / 2
                continue
            }

            val cleanLine = trimmed.replace("## ", "")
                .replace("**", "")
                .replace("`", "")
                .replace(referenceRegex, "")

            val wrappedLines = font.split(Component.literal(cleanLine), overlayWidth)
            totalHeight += wrappedLines.size * font.lineHeight
        }
        return totalHeight
    }

    private fun drawLayeredOutline(context: GuiGraphicsExtractor, lines: List<String>, color: Int) {
        val maxTextWidth = lines.maxOfOrNull { font.width(it) } ?: 0
        val totalTextHeight = lines.size * font.lineHeight

        val padding = 8
        val width= maxTextWidth + padding * 2
        val height = totalTextHeight + padding * 2
        val x = -padding
        val y = -padding

        val radius = (height / 12).coerceAtLeast(1)
        val baseR = radius.coerceAtMost(width / 2).coerceAtMost(height / 2)

        if (baseR >= 3) {
            context.drawRoundedOutline(x, y, width, height, baseR, Colors.DARK_GRAY.color) // outer layer
            context.drawRoundedOutline(x + 1, y + 1, width - 2, height - 2, baseR - 1, color) // middle layer
            context.drawRoundedOutline(x + 2, y + 2, width - 4, height - 4, baseR - 2, Colors.DARK_GRAY.color) // inner layer
        } else {
            context.drawRoundedOutline(x, y, width, height, baseR, Colors.DARK_GRAY.color)
        }
    }
}