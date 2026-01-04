package io.github.chindeaone.collectiontracker.util.rendering

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.commands.StartTracker
import io.github.chindeaone.collectiontracker.config.ModConfig
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass
import io.github.chindeaone.collectiontracker.util.CollectionColors
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

object RenderUtils {

    var config: ModConfig = SkyblockCollectionTracker.configManager.config!!
    var position: Position = config.overlay.overlaySingle.overlayPosition
    var commissionsPosition: Position = config.mining.commissionsOverlay.commissionsOverlayPosition

    var maxWidth: Int = 0
    var textHeight: Int = 0

    private val fr: Font get() = Minecraft.getInstance().font
    const val WHITE: Int = 0xFFFFFFFF.toInt()

    private fun getDimensions() {
        maxWidth = 0
        textHeight = 0

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

    fun drawCommissions(context: GuiGraphics) {
        if (!config.mining.commissionsOverlay.enableCommissionsOverlay) return

        context.pose().pushMatrix()
        context.pose().translate(commissionsPosition.x.toFloat(), commissionsPosition.y.toFloat())
        context.pose().scale(commissionsPosition.scale, commissionsPosition.scale)

        renderCommissions(context)

        context.pose().popMatrix()
    }

    private fun getCommissionsDimensions(): Pair<Int, Int> {
        val lines = TextUtils.updateCommissions() ?: return Pair(0, 0)
        var maxW = 0
        for (line in lines) {
            val w = fr.width(line)
            if (w > maxW) maxW = w
        }
        val h = fr.lineHeight * lines.size
        return Pair(maxW, h)
    }

    fun drawCommissionsDummy(context: GuiGraphics) {
        val (w, h) = getCommissionsDimensions()
        commissionsPosition.setDimensions(w, h)

        context.pose().pushMatrix()
        context.pose().translate(commissionsPosition.x.toFloat(), commissionsPosition.y.toFloat())
        context.pose().scale(commissionsPosition.scale, commissionsPosition.scale)

        context.fill(0, 0, commissionsPosition.width, commissionsPosition.height, -0x7fbfbfc0)

        val text = Component.literal("Move Commissions")
            .withStyle(ChatFormatting.AQUA)

        val centerX = commissionsPosition.width / 2.0f
        val centerY = (commissionsPosition.height - fr.lineHeight) / 2f

        context.drawCenteredString(fr, text, centerX.toInt(), centerY.toInt(), WHITE)

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
    }

    fun drawStaticText(context: GuiGraphics, activePosition: Position?) {
        val textScale = 0.8f

        val resizeText = Component.literal("Use the mouse wheel to resize the overlay").withStyle(ChatFormatting.GREEN)
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
            context.drawString(fr, positionText, (positionX / textScale).toInt(), (positionY / textScale).toInt(), WHITE, true)
            context.pose().popMatrix()
        }
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

    private fun renderCommissions(context: GuiGraphics) {
        val displayCommissionSet = TextUtils.updateCommissions() ?: return

        val x = 0
        var y = 0

        for (line in displayCommissionSet) {
            context.drawString(fr, line, x, y, WHITE, true)
            y += fr.lineHeight
        }
    }
}