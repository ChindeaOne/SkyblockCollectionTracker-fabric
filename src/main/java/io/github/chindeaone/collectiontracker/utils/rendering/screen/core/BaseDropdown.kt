package io.github.chindeaone.collectiontracker.utils.rendering.screen.core

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

open class BaseDropdown(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    initialValue: String,
    private val entries: List<String>,
    private val onSelected: (String) -> Unit
) : AbstractWidget(x, y, width, height, Component.empty()) {

    private var expanded = false
    private var scrollOffset = 0
    private var isUpdatingValue = false

    private val entryHeight = 20
    private val visibleEntries = 6

    private val dropdownHeight: Int
        get() = minOf(filteredEntries.size, visibleEntries) * entryHeight

    private val maxScrollOffset: Int
        get() = (filteredEntries.size - visibleEntries).coerceAtLeast(0)

    private val editBox = EditBox(MinecraftUtils.font, x, y, width, height, Component.empty()).apply {
        value = initialValue
        maxLength = 64
    }

    var value: String
        get() = editBox.value
        private set(newValue) {
            isUpdatingValue = true
            editBox.value = newValue
            isUpdatingValue = false
        }

    protected val filteredEntries: List<String>
        get() {
            if (value.isEmpty()) return entries
            return entries.filter { matchesEntry(it, value) }
        }

    protected open fun matchesEntry(entry: String, query: String): Boolean {
        return entry.contains(query, ignoreCase = true)
    }

    init {
        editBox.setResponder {
            if (!isUpdatingValue) {
                scrollOffset = 0
                expanded = true
                onSelected(it)
            }
        }
    }

    override fun extractWidgetRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        editBox.extractRenderState(context, mouseX, mouseY, a)
    }

    fun renderDropdown(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (!expanded || filteredEntries.isEmpty()) return

        val displayed = filteredEntries.drop(scrollOffset).take(visibleEntries)
        displayed.forEachIndexed { index, entry ->
            val entryY = y + height + index * entryHeight
            val hovered = mouseX >= x && mouseX < x + width && mouseY >= entryY && mouseY < entryY + entryHeight

            context.fill(x, entryY, x + width, entryY + entryHeight, if (hovered) ScreenColors.DROPDOWN_HOVER.color else ScreenColors.DROPDOWN.color)
            context.text(MinecraftUtils.font, Component.literal(entry), x + 5, entryY + (entryHeight - MinecraftUtils.font.lineHeight) / 2, Colors.WHITE.color)
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (!expanded) return false
        return editBox.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (!expanded) return false
        return editBox.charTyped(event)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (!isActive) return false

        val mouseX = event.x()
        val mouseY = event.y()

        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            expanded = true
            isFocused = true
            editBox.isFocused = true
            return editBox.mouseClicked(event, doubleClick)
        }

        expanded = false
        isFocused = false
        editBox.isFocused = false
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!expanded || filteredEntries.isEmpty()) return false

        val dropdownTop = y + height
        val dropdownBottom = dropdownTop + dropdownHeight

        val insideDropdown = mouseX >= x && mouseX < x + width && mouseY >= dropdownTop && mouseY < dropdownBottom
        val insideField = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height

        if (!insideDropdown && !insideField) return false
        if (filteredEntries.size <= visibleEntries) return true

        scrollOffset = (scrollOffset - scrollY.toInt()).coerceIn(0, maxScrollOffset)
        return true
    }

    fun clickDropdown(mouseX: Double, mouseY: Double): Boolean {
        if (!expanded || filteredEntries.isEmpty()) return false

        val dropdownTop = y + height
        val dropdownBottom = dropdownTop + dropdownHeight

        if (mouseX < x || mouseX >= x + width || mouseY < dropdownTop || mouseY >= dropdownBottom) return false

        val index = scrollOffset + ((mouseY - dropdownTop) / entryHeight).toInt()
        if (index !in filteredEntries.indices) return false

        select(filteredEntries[index])
        return true
    }

    fun scrollDropdown(mouseX: Double, mouseY: Double, scrollY: Double): Boolean {
        return mouseScrolled(mouseX, mouseY, 0.0, scrollY)
    }

    private fun select(entry: String) {
        value = entry
        scrollOffset = 0

        expanded = false
        editBox.isFocused = false
        isFocused = false

        onSelected(entry)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }
}