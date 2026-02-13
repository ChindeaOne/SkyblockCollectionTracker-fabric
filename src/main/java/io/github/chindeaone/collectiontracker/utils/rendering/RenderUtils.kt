package io.github.chindeaone.collectiontracker.utils.rendering

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.serverapi.RepoUtils
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import kotlin.text.split

object RenderUtils {

    private val fr: Font get() = Minecraft.getInstance().font
    private const val DUMMY_BG = -0x7fbfbfc0
    const val CUSTOM_WHITE: Int = 0xFFCCD7E0.toInt()
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val GREEN: Int = 0xFF55FF55.toInt()
    const val YELLOW: Int = 0xFFFFFF55.toInt()
    const val AQUA: Int = 0xFF55FFFF.toInt()

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
            context.drawCenteredString(fr, overlayText, (centerX / textScale).toInt(), (yTop / textScale).toInt(), WHITE)
            context.pose().popMatrix()
        }
    }

    @JvmStatic
    fun renderTrackingStringsWithColor(context: GuiGraphics, lines: List<String>, extraLines: List<String>, withColor: Boolean) {
        var y = 0

        val color: Int = if (withColor)  (ColorUtils.collectionColors[CollectionTracker.collection]) ?: GREEN  else GREEN
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

        val color: Int = (ColorUtils.skillColors[SkillTracker.skillName]) ?: GREEN
        for (line in lines) {
            drawHelper(line, context, y, color)
            y += fr.lineHeight
        }

        if (withTaming) {
            y += fr.lineHeight
            val tamingColor: Int = (ColorUtils.skillColors["Taming"]) ?: GREEN
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
            context.drawString(fr, line, 0, y, WHITE, true)
            y += fr.lineHeight
        }
    }

    @JvmStatic
    fun drawEditorHudText(context: GuiGraphics, activePosition: Position?) {
        val textScale = 0.8f

        val resizeText = Component.literal("Use mouse wheel to resize the overlay")
            .withStyle(ChatFormatting.GREEN)

        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), WHITE, true)
        context.pose().popMatrix()

        if (activePosition != null) {
            val positionText = Component.literal("Position: X=${activePosition.x}, Y=${activePosition.y}")
            val positionWidth = fr.width(positionText)
            val positionX = (context.guiWidth() / 2f) - (positionWidth * textScale / 2f)
            val positionY = textY + fr.lineHeight + 5f

            context.pose().pushMatrix()
            context.pose().scale(textScale, textScale)
            context.drawString(fr, positionText, (positionX / textScale).toInt(), (positionY / textScale).toInt(), YELLOW, true)
            context.pose().popMatrix()
        }
    }

    private fun drawHelper(line: String, context: GuiGraphics, y: Int, prefixColor: Int) {
        val splitIndex = line.lastIndexOf(": ")
        if (splitIndex != -1) {
            val prefix = line.substring(0, splitIndex)
            val numberPart = line.substring(splitIndex)

            context.drawString(fr, prefix, 0, y, prefixColor, true)

            val prefixWidth = fr.width(prefix)
            context.drawString(fr, numberPart,  prefixWidth, y, CUSTOM_WHITE, true)
        } else {
            context.drawString(fr, line, 0, y, prefixColor, true)
        }
    }

    @JvmStatic
    fun showTitle(title: Component, durationMs: Int) {
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

        context.pose().pushMatrix()
        context.pose().translate(screenWidth / 2f, screenHeight / 2f)
        context.pose().scale(ConfigAccess.getTitleScale().scale * ScaleUtils.scale, ConfigAccess.getTitleScale().scale * ScaleUtils.scale)

        context.drawCenteredString(fr, title, 0, -fr.lineHeight / 2, WHITE)
        context.pose().popMatrix()
    }

    @JvmStatic
    fun renderChangelog(context: GuiGraphics, scrollOffset: Int) {
        val rawNotes = RepoUtils.latestNotes
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
            context.drawCenteredString(fr, "Version: $version", screenWidth / 2, startY - 20, GREEN)
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
                line.contains("## What's New") -> GREEN
                line.contains("## Improvements") -> YELLOW
                line.contains("## Bug Fixes") -> AQUA
                else -> WHITE
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
}