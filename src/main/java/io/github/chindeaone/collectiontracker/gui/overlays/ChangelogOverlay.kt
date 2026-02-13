package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.mixins.AccessorGuiContainer
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.NotNull

class ChangelogOverlay(
    private val oldScreen: AbstractContainerScreen<*>?
) : Screen(Component.literal("Changelog")) {

    private var scrollAmount = 0.0

    override fun onClose(){
        OverlayManager.setGlobalRendering(true)
        Minecraft.getInstance().setScreen(oldScreen)
    }

    override fun render(@NotNull context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderMenuBackground(context)

        if (oldScreen != null) (oldScreen as AccessorGuiContainer)
            .invokeDrawGuiContainerBackgroundLayer_sct(context, partialTicks, -1, -1)

        RenderUtils.renderChangelog(context, scrollAmount.toInt())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val window = Minecraft.getInstance().window
        val screenWidth = window.guiScaledWidth
        val screenHeight = window.guiScaledHeight

        val totalHeight = RenderUtils.getChangelogHeight(screenWidth)
        val overlayHeight = (screenHeight * 0.75f).toInt()
        val maxScroll = (totalHeight - overlayHeight).coerceAtLeast(0)

        scrollAmount = (scrollAmount - verticalAmount * 10).coerceIn(0.0, maxScroll.toDouble())

        return true
    }
}