package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class DummyOverlay(
    private val oldScreen: AbstractContainerScreen<*>?
) : Screen(Component.literal("Dummy Overlay")) {

    private var dragging: AbstractOverlay? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    override fun onClose() {
        OverlayManager.setGlobalRendering(true)
        MinecraftUtils.setScreen(oldScreen)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!OverlayManager.isInEditorMode()) {
            return
        }

        extractMenuBackground(context)

        oldScreen?.extractRenderState(context, mouseX, mouseY, partialTicks)

        var hovered: AbstractOverlay? = null
        // Draw all dummies
        for (overlay in OverlayManager.all()) {
            if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel) continue
            overlay.updateDimensions()
            if (overlay.position.width == 0 && overlay.position.height == 0) continue

            RenderUtils.drawDummyFrame(context, overlay.position, overlay.overlayLabel)

            if (overlay.isHovered(mouseX.toDouble(), mouseY.toDouble())) {
                hovered = overlay
            }
        }
        // Update dragging positions
        dragging?.let {
            it.updateDimensions()
            it.position.setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY)
        }

        RenderUtils.drawEditorHudText(context, hovered?.position)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!OverlayManager.isInEditorMode()) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        if (verticalAmount == 0.0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        for (overlay in OverlayManager.all()) {
            if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel) continue

            val pos = overlay.position
            if (overlay.isHovered(mouseX, mouseY)) {
                val scaleChange = 0.05f
                val next = pos.scale + (if (verticalAmount > 0) scaleChange else -scaleChange)
                pos.setScaling(clamp(next))
                return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
        val mx = event.x
        val my = event.y

        for (overlay in OverlayManager.all()) {
            when (event.button()) {
                0 -> {
                    if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel) continue

                    if (overlay.isHovered(mx, my)) {
                        dragging = overlay
                        dragOffsetX = (mx - overlay.position.x).toInt()
                        dragOffsetY = (my - overlay.position.y).toInt()
                        return true
                    }
                }
                1 -> {
                    if (!overlay.isEnabled || overlay.overlayLabel == "Global Title") continue

                    if (overlay.isHovered(mx, my)) {
                        OverlayManager.setGlobalRendering(true)
                        overlay.jumpToConfig()
                        return true
                    }
                }
                2 -> {
                    if (!overlay.isEnabled || "Global Title" == overlay.overlayLabel) continue

                    if (overlay.isHovered(mx, my)) {
                        overlay.position.setScaling(1.0f)
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

    private fun clamp(v: Float): Float {
        return v.coerceIn(0.1f, 10.0f)
    }
}
