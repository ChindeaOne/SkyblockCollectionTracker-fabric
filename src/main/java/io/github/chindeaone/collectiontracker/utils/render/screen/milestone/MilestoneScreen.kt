package io.github.chindeaone.collectiontracker.utils.render.screen.milestone

import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseButton
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseDropdown
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.Page
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class MilestoneScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseMilestoneScreen(oldScreen) {

    private data class MilestoneWidgets(val total: Button, val name: BaseDropdown, val value: EditBox, val remove: Button)
    private val entryWidgets = mutableListOf<MilestoneWidgets>()

    override val message: Component = Component.literal("ⓘ `Total` means the milestone is based on your total collection/skill amount!")

    override val screenTitle: Component
        get() = when (currentPage) {
            Page.COLLECTIONS -> Component.literal("Collection Milestones")
            Page.SKILLS -> Component.literal("Skill Milestones")
        }

    override fun rebuildEntryWidgets() {
        entryWidgets.clear()

        addRenderableWidget(
            BaseButton(actionColumnX - 8, panelTop() + 38, 16, 16, { Component.literal("+") }) {
                addNewEntry()
                rebuildWidgets()
            }
        )

        val dropdownEntries = when (currentPage) {
            Page.COLLECTIONS -> CollectionsManager.collectionList.sorted()
            Page.SKILLS -> SkillUtils.skillList.sorted()
        }

        entries.forEachIndexed { index, entry ->
            val y = contentTop + index * rowHeight - currentScrollOffset

            if (y + 10 < contentTop || y > contentBottom - 10) {
                return@forEachIndexed
            }

            val totalButton = BaseButton(
                totalColumnX - 8,
                y + 2,
                16,
                16,
                { if (entry.isTotal) Component.literal("✓").withColor(Colors.GREEN.color) else Component.empty() }
            ) {
                updateEntry(index, isTotal = !entries[index].isTotal)
            }

            val nameDropdown = BaseDropdown(
                nameColumnX - 55,
                y,
                110,
                20,
                entry.name,
                dropdownEntries
            ) {
                updateEntry(index, name = it)
            }

            val valueBox = createInputBox(entry.value, valueColumnX - 35, y, "Milestone Value").apply {
                setResponder { updateEntry(index, value = it) }
            }

            val removeButton = BaseButton(actionColumnX - 8, y + 2, 16, 16, { Component.literal("-") }) {
                removeEntry(index)
                rebuildWidgets()
            }

            entryWidgets += MilestoneWidgets(totalButton, nameDropdown, valueBox, removeButton)
        }

        entryWidgets.forEach { widgets ->
            addRenderableWidget(widgets.total)
            addRenderableWidget(widgets.name)
            addRenderableWidget(widgets.value)
            addRenderableWidget(widgets.remove)
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()

        for (widgets in entryWidgets) {
            if (widgets.name.clickDropdown(mouseX, mouseY)) {
                return true
            }
        }

        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        for (widgets in entryWidgets) {
            if (widgets.name.scrollDropdown(mouseX, mouseY, scrollY)) {
                return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)
        drawHeaders(context)
        entryWidgets.forEach { it.name.renderDropdown(context, mouseX, mouseY) }

        context.centeredText(MinecraftUtils.font, message, width / 2, panelBottom() + 2, Colors.GRAY.color)
    }
}