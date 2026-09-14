package io.github.chindeaone.collectiontracker.utils.rendering.screen

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.RepoUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseScrollableScreen
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class ChangelogScreen(
    oldScreen: AbstractContainerScreen<*>?
) : BaseScrollableScreen(oldScreen) {

    override val contentHeight: Int
        get() = RenderUtils.getChangelogHeight(ScaleUtils.scaledWidth)

    override val screenTitle
        get() = Component.literal("Changelog - Version ${SkyblockCollectionTracker.VERSION}").withColor(Colors.BLUE.color)
            .append(Component.literal(" $versionLabel").withColor(versionColor))

    private val isReleaseVersion
        get() = SkyblockCollectionTracker.VERSION.endsWith("0")

    private val versionLabel
        get() = if (isReleaseVersion) "(Release)" else "(Beta)"

    private val versionColor
        get() = if (isReleaseVersion) Colors.GREEN.color else Colors.GOLD.color

    override fun initButtons() {}

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)

        val rawNotes = RepoUtils.latestNotes ?: return
        if (rawNotes.isEmpty()) return

        val footerIndex = rawNotes.indexOf("**Full Changelog**")
        val cleanNotes = if (footerIndex != -1) {
            rawNotes.substring(0, footerIndex)
        } else {
            rawNotes
        }

        val screenWidth = context.guiWidth()
        val overlayWidth = screenWidth / 2

        val startX = (screenWidth - overlayWidth) / 2

        context.enableScissor(startX, contentTop, startX + overlayWidth, contentBottom)

        RenderUtils.renderChangelogLines(
            context,
            cleanNotes,
            startX,
            contentTop  - currentScrollOffset,
            overlayWidth,
            contentTop,
            contentBottom - contentTop
        )

        context.disableScissor()
    }
}