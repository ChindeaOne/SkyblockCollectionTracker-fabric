package io.github.chindeaone.collectiontracker.util.rendering

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.commands.StartTracker
import io.github.chindeaone.collectiontracker.config.categories.Overlay
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass
import io.github.chindeaone.collectiontracker.util.CollectionColors
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

object RenderUtils {

    var overlay: Overlay = SkyblockCollectionTracker.configManager.config!!.overlay
    var position: Position = overlay.overlaySingle.overlayPosition
    val positionList: Position = overlay.overlayList.overlayPosition

    var maxWidth: Int = 0
    var textHeight: Int = 0

    private val fr: Font get() = Minecraft.getInstance().font
    const val WHITE: Int = 0xFFFFFFFF.toInt()

    private fun getDimensions() {
        val overlayLines = TextUtils.getStrings()

        if (overlayLines.isEmpty()) {
            position.setDimensions(0, 0)
            return
        }

        for (line in overlayLines) {
            val lineWidth: Int = fr.width(line)
            if (lineWidth > maxWidth) maxWidth = lineWidth
        }

        textHeight = fr.lineHeight * (overlayLines.size + 1)
        position.setDimensions(maxWidth, textHeight)
    }

    fun drawRect(context: GuiGraphics) {
        if (TrackingHandlerClass.startTime == 0L) return

        context.pose().pushMatrix()
        context.pose().translate(position.x.toFloat(), position.y.toFloat())
        context.pose().scale(position.scale, position.scale)

        if (SkyblockCollectionTracker.configManager.config!!.overlay.overlayTextColor) {
            renderColors(context)
        } else {
            renderStrings(context)
        }

        context.pose().popMatrix()
    }

    fun drawRectDummy(
        context: GuiGraphics
    ) {
        getDimensions()

        context.pose().pushMatrix()

        context.pose().translate(position.x.toFloat(), position.y.toFloat())
        context.pose().scale(position.scale, position.scale)

        context.fill(
            0, 0, position.width, position.height, -0x7fbfbfc0
        )

        val overlayText = Component.literal("Move the overlay")
            .withStyle(ChatFormatting.GREEN)

        val textScale = 0.9f
        val centerX = position.width / 2.0f
        val yTop = (position.height - fr.lineHeight * textScale) / 2f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawCenteredString(fr, overlayText, (centerX / textScale).toInt(), (yTop / textScale).toInt(), WHITE)
        context.pose().popMatrix()

        context.pose().popMatrix()

        drawStaticText(context)
    }

    fun drawStaticText(context: GuiGraphics) {
        val textScale = 0.8f

        val resizeText = Component.literal("Use the mouse wheel to resize the overlay").withStyle(ChatFormatting.GREEN)
        val textWidth = fr.width(resizeText)
        val textX = (context.guiWidth() / 2f) - (textWidth * textScale / 2f)
        val textY = 10f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, resizeText, (textX / textScale).toInt(), (textY / textScale).toInt(), WHITE, true)
        context.pose().popMatrix()

        val positionText = Component.literal("Position: X=${position.x}, Y=${position.y}")
        val positionWidth = fr.width(positionText)
        val positionX = (context.guiWidth() / 2f) - (positionWidth * textScale / 2f)
        val positionY = textY + fr.lineHeight + 5f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawString(fr, positionText, (positionX / textScale).toInt(), (positionY / textScale).toInt(), WHITE, true)
        context.pose().popMatrix()
    }

    private fun renderStrings(
        context: GuiGraphics
    ) {
        val overlayLines = TextUtils.getStrings()
        if (overlayLines.isEmpty()) return

        val x = 0
        var y = 0

        for (line in overlayLines) {
            val splitIndex = line.lastIndexOf(": ")
            if (splitIndex != -1) {
                val prefix = line.substring(0, splitIndex + 2)
                val numberPart = line.substring(splitIndex + 2)

                context.drawString(fr, prefix, x, y, 0xFF55FF55.toInt(), true)

                val prefixWidth = fr.width(prefix)
                context.drawString(fr, numberPart, (x + prefixWidth), y, WHITE, true)
            } else {
                context.drawString(fr, line, x, y, WHITE, true)
            }

            y += fr.lineHeight
        }

        val uptimeString = TextUtils.uptimeString()
        val splitIndex = uptimeString.lastIndexOf(": ")
        if (splitIndex != -1) {
            val prefix = uptimeString.substring(0, splitIndex + 2)
            val numberPart = uptimeString.substring(splitIndex + 2)

            context.drawString(fr, prefix, x, y, 0xFF55FF55.toInt(), true)

            val prefixWidth = fr.width(prefix)
            context.drawString(fr, numberPart, (x + prefixWidth), y, WHITE, true)
        } else {
            context.drawString(fr, uptimeString, x, y, WHITE, true)
        }
    }

    private fun renderColors(
        context: GuiGraphics
    ) {

        val overlayLines = TextUtils.getStrings()
        if (overlayLines.isEmpty()) return

        val x = 0
        var y = 0

        val color = StartTracker.collection.let { CollectionColors.colors[it] }

        for (line in overlayLines) {
            val splitIndex = line.lastIndexOf(": ")
            if (splitIndex != -1) {
                val prefix = line.substring(0, splitIndex + 2)
                val numberPart = line.substring(splitIndex + 2)

                if (color != null) {
                    context.drawString(fr, prefix, x, y, color, true)
                }

                val prefixWidth = fr.width(prefix)
                context.drawString(fr, numberPart, (x + prefixWidth), y, WHITE, true)
            } else {
                if (color != null) {
                    context.drawString(fr, line, x, y, color, true)
                }
            }

            y += fr.lineHeight
        }
        if (color != null) {
            val splitIndex = TextUtils.uptimeString().lastIndexOf(": ")
            if (splitIndex != -1) {
                val prefix = TextUtils.uptimeString().substring(0, splitIndex + 2)
                val numberPart = TextUtils.uptimeString().substring(splitIndex + 2)

                context.drawString(fr, prefix, x, y, color, true)

                val prefixWidth = fr.width(prefix)
                context.drawString(fr, numberPart, (x + prefixWidth), y, WHITE, true)
            } else {
                context.drawString(fr, TextUtils.uptimeString(), x, y, color, true)
            }
        }
    }

    fun drawRectDummyList(
        context: GuiGraphics,
    ) {
        getDimensions()

        context.pose().pushMatrix()
        context.pose().translate(positionList.x.toFloat(), positionList.y.toFloat())
        context.pose().scale(positionList.scale, positionList.scale)

        context.fill(
            0, 0, positionList.width, positionList.height, -0x7fbfbfc0
        )

        val overlayText = Component.literal("Move the overlay list")
            .withStyle(ChatFormatting.GREEN)

        val textScale = 0.9f
        val centerX = positionList.width / 2.0f
        val yTop = (positionList.height - fr.lineHeight * textScale) / 2f

        context.pose().pushMatrix()
        context.pose().scale(textScale, textScale)
        context.drawCenteredString(fr, overlayText, (centerX / textScale).toInt(), (yTop / textScale).toInt(), WHITE)
        context.pose().popMatrix()

        context.pose().popMatrix()

        drawStaticText(context)
    }
}