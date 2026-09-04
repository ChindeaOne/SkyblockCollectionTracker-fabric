package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class ChangelogOverlay(
    private val oldScreen: AbstractContainerScreen<*>?
) : Screen(Component.literal("Changelog")) {

    private var scrollAmount = 0.0

    override fun onClose(){
        OverlayManager.setGlobalRendering(true)
        MinecraftUtils.setScreen(oldScreen)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        extractMenuBackground(context)

        oldScreen?.extractRenderState(context, mouseX, mouseY, partialTicks)

        RenderUtils.renderChangelog(context, scrollAmount.toInt())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val screenWidth = ScaleUtils.scaledWidth
        val screenHeight = ScaleUtils.scaledHeight

        val totalHeight = RenderUtils.getChangelogHeight(screenWidth)
        val overlayHeight = (screenHeight * 0.75f).toInt()
        val maxScroll = (totalHeight - overlayHeight).coerceAtLeast(0)

        scrollAmount = (scrollAmount - verticalAmount * 10).coerceIn(0.0, maxScroll.toDouble())

        return true
    }
}