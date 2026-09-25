package io.github.chindeaone.collectiontracker.utils.render.screen.core

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import io.github.chindeaone.collectiontracker.utils.render.drawRoundedOutline
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

open class BaseScreen(
    private val oldScreen: AbstractContainerScreen<*>?,
): Screen(Component.empty()) {

    protected open val screenTitle: Component
        get() = Component.empty()

    protected open val panelWidth: Int
        get() = (width * 0.6f).toInt()

    protected open val panelHeight: Int
        get() = (height * 0.7f).toInt()

    protected val panelLeft: Int
        get() = (width - panelWidth) / 2

    protected val panelTop: Int
        get() = (height - panelHeight) / 2

    protected val panelRight: Int
        get() = panelLeft + panelWidth

    protected val panelBottom: Int
        get() = panelTop + panelHeight

    private var initialized = false

    override fun init() {
        if (!initialized){
            loadData()
            initialized = true
        }

        initContent()
    }

    protected open fun loadData() {}
    protected open fun initContent() {}
    protected open fun saveData() {}

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        context.fill(0, 0, width, height, ScreenColors.SCREEN_BG.color)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        drawPanel(context)

        super.extractRenderState(context, mouseX, mouseY, a)

        drawTitle(context)
    }

    private fun drawPanel(context: GuiGraphicsExtractor) {
        val left = panelLeft
        val top = panelTop
        val right = panelRight
        val bottom = panelBottom
        val radius = 4

        // panel background
        context.fill(left, top, right, bottom, ScreenColors.PANEL_BG.color)

        context.drawRoundedOutline(left, top, panelWidth, panelHeight, radius, ScreenColors.PANEL_BORDER.color)

        // header separator
        context.fill(left + 1, top + 35, right - 1, top + 36, ScreenColors.PANEL_HEADER.color)
    }

    private fun drawTitle(context: GuiGraphicsExtractor) {
        if (screenTitle == Component.empty()) return
        context.centeredText(font, screenTitle, width / 2, panelTop + 13, Colors.WHITE.color)
    }

    override fun onClose() {
        OverlayManager.setGlobalRendering(true)
        MinecraftUtils.setScreen(oldScreen)
    }
}