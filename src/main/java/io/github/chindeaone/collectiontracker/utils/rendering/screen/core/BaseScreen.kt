package io.github.chindeaone.collectiontracker.utils.rendering.screen.core

import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
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

    private var initialized = false
    protected var currentPage = Page.COLLECTIONS

    private enum class StatusType(val color: Int) {
        ERROR(Colors.RED.color),
        SUCCESS(Colors.GREEN.color)
    }

    private var statusMessage: Pair<Component, StatusType>? = null
    private var statusTimestamp = 0L
    private val statusDuration = 2000L // 2 seconds

    override fun init() {
        if (!initialized){
            loadData()
            initialized = true
        }

        initContent()
        initButtons()
    }

    protected open fun loadData() {}
    protected open fun initContent() {}
    protected open fun saveData() {}

    protected fun panelLeft() = (width - panelWidth) / 2
    protected fun panelTop() = (height - panelHeight) / 2
    protected fun panelRight() = panelLeft() + panelWidth
    protected fun panelBottom() = panelTop() + panelHeight

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        context.fill(0, 0, width, height, ScreenColors.SCREEN_BG.color)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        drawPanel(context)

        super.extractRenderState(context, mouseX, mouseY, a)

        drawTitle(context)
        drawStatusMessage(context)
    }

    private fun drawPanel(context: GuiGraphicsExtractor) {
        val left = panelLeft()
        val top = panelTop()
        val right = panelRight()
        val bottom = panelBottom()

        // panel background
        context.fill(left, top, right, bottom, ScreenColors.PANEL_BG.color)

        val border = ScreenColors.PANEL_BORDER.color // outer border

        context.fill(left, top, right, top + 1, border)
        context.fill(left, bottom - 1, right, bottom, border)
        context.fill(left, top, left + 1, bottom, border)
        context.fill(right - 1, top, right, bottom, border)

        // header separator
        context.fill(left + 1, top + 35, right - 1, top + 36, ScreenColors.PANEL_HEADER.color)
    }

    private fun drawTitle(context: GuiGraphicsExtractor) {
        if (screenTitle == Component.empty()) return
        context.centeredText(font, screenTitle, width / 2, panelTop() + 13, Colors.WHITE.color)
    }

    protected open fun initButtons() {
        addRenderableWidget(
            BaseButton(width / 2 - 40, panelBottom() - 30, 80, 20, { Component.literal("Save") }) {
                saveData()
            }
        )

        val pageButtonWidth = 80
        val pageButtonHeight = 20
        val pageButtonY = panelTop() + 8

        val pageButtonX = when (currentPage) {
            Page.COLLECTIONS -> panelRight() - pageButtonWidth - 10
            Page.SKILLS -> panelLeft() + 10
        }

        val pageButtonLabel = when (currentPage) {
            Page.COLLECTIONS -> "Skill Page"
            Page.SKILLS -> "Collection Page"
        }

        addRenderableWidget(
            BaseButton(pageButtonX, pageButtonY, pageButtonWidth, pageButtonHeight, { Component.literal(pageButtonLabel) }) {
                switchPage()
            }
        )
    }

    protected fun switchPage() {
        currentPage = when (currentPage) {
            Page.COLLECTIONS -> Page.SKILLS
            Page.SKILLS -> Page.COLLECTIONS
        }

        rebuildWidgets()
    }

    protected open fun createInputBox(value: String, x: Int, y: Int, narrationText: String): EditBox =
        object: EditBox(MinecraftUtils.font, x, y, 70, 20, Component.literal(narrationText)) {
            override fun insertText(input: String) {
                super.insertText(input.filter { isAllowedInput(it) })
            }
        }.apply {
            this.value = value
            maxLength = 32
        }

    private fun isAllowedInput(char: Char): Boolean = char.isDigit() || char in ".,kmbKMB"

    protected fun showError(message: String) {
        statusMessage = Component.literal("§c$message") to StatusType.ERROR
        statusTimestamp = System.currentTimeMillis()
    }

    protected fun showSuccess(message: String) {
        statusMessage = Component.literal("§a$message") to StatusType.SUCCESS
        statusTimestamp = System.currentTimeMillis()
    }

    private fun drawStatusMessage(context: GuiGraphicsExtractor) {
        val (message, type) = statusMessage ?: return
        if (System.currentTimeMillis() - statusTimestamp > statusDuration) {
            statusMessage = null
            return
        }

        context.centeredText(font, message, width / 2, panelBottom() - 48, type.color)
    }

    override fun onClose() {
        OverlayManager.setGlobalRendering(true)
        MinecraftUtils.setScreen(oldScreen)
    }
}