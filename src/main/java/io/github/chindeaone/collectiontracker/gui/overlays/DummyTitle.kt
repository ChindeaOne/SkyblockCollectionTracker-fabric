package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

class DummyTitle(
    private val oldScreen: AbstractContainerScreen<*>?
) : Screen(Component.literal("Title Dummy")) {

    private var dragging = false
    private var dragOffsetY = 0

    override fun onClose(){
        OverlayManager.setGlobalRendering(true)
        Minecraft.getInstance()./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(oldScreen)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        extractMenuBackground(context)

        oldScreen?.extractRenderState(context, mouseX, mouseY, partialTicks)

        val pos = ConfigAccess.getTitlePosition()
        val totalScale = ConfigAccess.getTitleScale().scale * ScaleUtils.scale
        if (pos.x == 0 && pos.y == 0) {
            val sw = (pos.width * totalScale).roundToInt()
            val sh = (pos.height * totalScale).roundToInt()

            val centerX = (ScaleUtils.scaledWidth - sw) / 2
            val centerY = (ScaleUtils.scaledHeight - sh) / 2
            ConfigHelper.setTitlePosition(centerX, centerY)
        }
        val currentPos = ConfigAccess.getTitlePosition()
        val displayPos = calculateDisplayPosition(currentPos, totalScale)

        RenderUtils.drawDummyFrame(context, displayPos, "Title Overlay")

        val hovered = isMouseOver(mouseX, mouseY, displayPos)

        RenderUtils.drawEditorHudTitle(context, if (hovered) displayPos else null)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()
        val pos = ConfigAccess.getTitlePosition()
        val totalScale = ConfigAccess.getTitleScale().scale * ScaleUtils.scale
        val displayPos = calculateDisplayPosition(pos, totalScale)

        if (isMouseOver(mx, my, displayPos)) {
            if (event.button() == 0) {
                dragging = true
                dragOffsetY = my - pos.y
                return true
            }
        }
        return super.mouseClicked(event, doubled)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragY: Double, e: Double): Boolean {
        if (dragging) {
            val pos = ConfigAccess.getTitlePosition()
            ConfigHelper.setTitlePosition(pos.x, event.y.toInt() - dragOffsetY)
            return true
        }
        return false
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        dragging = false
        return super.mouseReleased(event)
    }

    private fun calculateDisplayPosition(pos: Position, totalScale: Float): Position {
        val sw = (pos.width * totalScale).roundToInt()
        val centeredX = (ScaleUtils.scaledWidth - sw) / 2

        val displayPos = Position(centeredX, pos.y)
        displayPos.setScaling(totalScale)
        displayPos.setDimensions(pos.width, pos.height)
        return displayPos
    }

    private fun isMouseOver(mouseX: Int, mouseY: Int, pos: Position): Boolean {
        val yPadding = 4
        val s = pos.scale
        val sw = (pos.width * s).roundToInt()

        val y1 = (pos.y - yPadding * s).roundToInt()
        val y2 = (pos.y + (pos.height + yPadding) * s).roundToInt()

        return mouseX >= pos.x && mouseX <= pos.x + sw && mouseY >= y1 && mouseY <= y2
    }
}