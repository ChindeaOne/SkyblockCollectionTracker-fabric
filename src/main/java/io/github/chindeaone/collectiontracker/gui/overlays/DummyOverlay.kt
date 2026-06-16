package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.NotNull

class DummyOverlay(private val oldScreen: AbstractContainerScreen<*>?) : Screen(Component.literal("Dummy Overlay")) {

    private var dragging: AbstractOverlay? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    override fun onClose() {
        OverlayManager.setGlobalRendering(true)
        Minecraft.getInstance().setScreen(oldScreen)
    }

    override fun extractRenderState(@NotNull context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!OverlayManager.isInEditorMode()) {
            return
        }

        extractMenuBackground(context)

        oldScreen?.extractRenderState(context, mouseX, mouseY, partialTicks)

        var hovered: AbstractOverlay? = null
        // Draw all dummies
        for (overlay in OverlayManager.all()) {
            if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel()) continue
            overlay.updateDimensions()

            RenderUtils.drawDummyFrame(context, overlay.position(), overlay.overlayLabel())

            if (isMouseOver(mouseX, mouseY, overlay.position())) {
                hovered = overlay
            }
        }
        // Update dragging positions
        dragging?.let {
            it.updateDimensions()
            it.position().setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY)
        }

        RenderUtils.drawEditorHudText(context, hovered?.position())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!OverlayManager.isInEditorMode()) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        if (verticalAmount == 0.0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        for (overlay in OverlayManager.all()) {
            if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel()) continue

            val pos = overlay.position()
            if (isMouseOver(mx, my, pos)) {
                val scaleChange = 0.05f
                val next = pos.scale + (if (verticalAmount > 0) scaleChange else -scaleChange)
                pos.setScaling(clamp(next))
                return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()

        for (overlay in OverlayManager.all()) {
            when (event.button()) {
                0 -> {
                    if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel()) continue

                    if (isMouseOver(mx, my, overlay.position())) {
                        dragging = overlay
                        dragOffsetX = mx - overlay.position().x
                        dragOffsetY = my - overlay.position().y
                        return true
                    }
                }
                1 -> if (overlay.overlayLabel() != "Global Title" && isMouseOver(mx, my, overlay.position())) {
                    OverlayManager.setGlobalRendering(true)
                    overlay.jumpToConfig()
                    return true
                }
                2 -> {
                    if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel()) continue

                    if (isMouseOver(mx, my, overlay.position())) {
                        overlay.position().setScaling(1.0f)
                        return true
                    }
                }
            }
        }

        return super.mouseClicked(event, doubled)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        dragging = null
        return super.mouseReleased(event)
    }

    private fun isMouseOver(mouseX: Int, mouseY: Int, pos: Position): Boolean {
        val yPadding = 4
        val x = pos.x
        val y = pos.y
        val w = pos.width
        val h = pos.height
        val s = pos.scale

        val x2 = x + (w * s).toInt()
        val y1 = (y - yPadding * s).toInt()
        val y2 = (y + (h + yPadding) * s).toInt()

        return mouseX in x..x2 && mouseY in y1..y2
    }

    private fun clamp(v: Float): Float {
        return v.coerceIn(0.1f, 10.0f)
    }
}
