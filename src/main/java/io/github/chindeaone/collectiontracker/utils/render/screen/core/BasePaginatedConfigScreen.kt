package io.github.chindeaone.collectiontracker.utils.render.screen.core

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BasePaginatedConfigScreen(oldScreen: AbstractContainerScreen<*>?): BaseScrollableScreen(oldScreen) {

    private enum class StatusType(val color: Int) {
        ERROR(Colors.RED.color),
        SUCCESS(Colors.GREEN.color)
    }

    private var statusMessage: Pair<Component, StatusType>? = null
    private var statusTimestamp = 0L
    private val statusDuration = 2000L

    var currentPage = Page.COLLECTIONS
        protected set

    override fun init() {
        initPageButtons()
        initActionButtons()
    }

    protected open fun initPageButtons() {
        val buttonWidth = 80
        val buttonHeight = 20
        val buttonY = panelTop + 8

        val buttonX = when (currentPage) {
            Page.COLLECTIONS -> panelRight - buttonWidth - 10
            Page.SKILLS -> panelLeft + 10
        }

        val label = when (currentPage) {
            Page.COLLECTIONS -> "Skill Page"
            Page.SKILLS -> "Collection Page"
        }

        addRenderableWidget(BaseButton(buttonX, buttonY, buttonWidth, buttonHeight, { Component.literal(label) }) {
            switchPage()
        })
    }

    protected open fun initActionButtons() {
        addRenderableWidget(BaseButton(width / 2 - 40, panelBottom - 30, 80, 20, { Component.literal("Save") }) {
            saveData()
        })
    }

    protected fun switchPage() {
        currentPage = when (currentPage) {
            Page.COLLECTIONS -> Page.SKILLS
            Page.SKILLS -> Page.COLLECTIONS
        }
        resetScroll()
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

    fun showSuccess(message: String) {
        statusMessage = Component.literal(message) to StatusType.SUCCESS
        statusTimestamp = System.currentTimeMillis()
    }

    fun showError(message: String) {
        statusMessage = Component.literal(message) to StatusType.ERROR
        statusTimestamp = System.currentTimeMillis()
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)
        drawStatusMessage(context)
    }

    private fun drawStatusMessage(context: GuiGraphicsExtractor) {
        val (message, type) = statusMessage ?: return
        if (System.currentTimeMillis() - statusTimestamp > statusDuration) {
            statusMessage = null
            return
        }

        context.centeredText(font, message, width / 2, panelBottom - 48, type.color)
    }

    enum class Page {
        COLLECTIONS, SKILLS
    }
}