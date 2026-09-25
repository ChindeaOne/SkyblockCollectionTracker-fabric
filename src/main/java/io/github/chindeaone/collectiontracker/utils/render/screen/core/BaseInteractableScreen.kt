package io.github.chindeaone.collectiontracker.utils.render.screen.core

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseInteractableScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseScreen(oldScreen) {

    private enum class StatusType(val color: Int) {
        ERROR(Colors.RED.color),
        SUCCESS(Colors.GREEN.color)
    }

    private var statusMessage: Pair<Component, StatusType>? = null
    private var statusTimestamp = 0L
    private val statusDuration = 2000L // 2 seconds

    protected var currentPage = Page.COLLECTIONS

    override fun init() {
        super.init()
        initButtons()
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)
        drawStatusMessage(context)
    }

    protected open fun initButtons() {
        addRenderableWidget(
            BaseButton(width / 2 - 40, panelBottom - 30, 80, 20, { Component.literal("Save") }) {
                saveData()
            }
        )

        val pageButtonWidth = 80
        val pageButtonHeight = 20
        val pageButtonY = panelTop + 8

        val pageButtonX = when (currentPage) {
            Page.COLLECTIONS -> panelRight - pageButtonWidth - 10
            Page.SKILLS -> panelLeft + 10
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

        context.centeredText(font, message, width / 2, panelBottom - 48, type.color)
    }
}