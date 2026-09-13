package io.github.chindeaone.collectiontracker.utils.rendering.screen.milestone

import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.milestones.Milestone
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseListScreen
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.Page
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseMilestoneScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseListScreen(oldScreen) {

    protected data class Entry(var name: String, var value: String, var isTotal: Boolean)

    protected val collectionEntries = mutableListOf<Entry>()
    protected val skillEntries = mutableListOf<Entry>()

    protected val entries: MutableList<Entry>
        get() = when (currentPage) {
            Page.COLLECTIONS -> collectionEntries
            Page.SKILLS -> skillEntries
        }

    override val entryCount: Int
        get() = entries.size

    protected val totalColumnX: Int
        get() = panelLeft() + (panelWidth * 0.15f).toInt()

    protected val nameColumnX: Int
        get() = panelLeft() + (panelWidth * 0.40f).toInt()

    protected val valueColumnX: Int
        get() = panelLeft() + (panelWidth * 0.65f).toInt()

    protected val actionColumnX: Int
        get() = panelLeft() + (panelWidth * 0.85f).toInt()

    override fun loadData() {
        collectionEntries.clear()
        skillEntries.clear()

        ConfigAccess.getMilestones().forEach { (name, milestone) ->
            val entry = Entry(
                name,
                StringUtils.formatValue(milestone.target),
                milestone.isTotal
            )

            if (CollectionsManager.isValidCollection(name)) {
                collectionEntries += entry
            } else if (SkillUtils.isValidSkill(name)) {
                skillEntries += entry
            }
        }

        if (collectionEntries.isEmpty()) {
            collectionEntries += Entry("", "", false)
        }

        if (skillEntries.isEmpty()) {
            skillEntries += Entry("", "", false)
        }
    }

    override fun saveData() {
        val filledCollections = collectionEntries.filter { it.name.isNotBlank() }
        val filledSkills = skillEntries.filter { it.name.isNotBlank() }

        if (hasDuplicates(filledCollections.map { it.name }) || hasDuplicates(filledSkills.map { it.name })) {
            showError("Can't have duplicate names!")
            return
        }

        val invalidCollections = filledCollections.filterNot { CollectionsManager.isValidCollection(it.name) }
        if (invalidCollections.isNotEmpty()) {
            showError("There's an invalid collection in the list!")
            return
        }

        val invalidSkills = filledSkills.filterNot { SkillUtils.isValidSkill(it.name) }
        if (invalidSkills.isNotEmpty()) {
            showError("There's an invalid skill in the list!")
            return
        }

        val currentMilestones = ConfigAccess.getMilestones()

        val collectionMilestones = filledCollections.mapNotNull { entry ->
            NumbersUtils.parseValue(entry.value)?.let { target ->
                val accumulated = currentMilestones[entry.name]?.accumulated ?: 0L
                entry.name to Milestone(
                    target = target,
                    isTotal = entry.isTotal,
                    accumulated = if (entry.isTotal) 0L else accumulated
                )
            }
        }

        val skillMilestones = filledSkills.mapNotNull { entry ->
            NumbersUtils.parseValue(entry.value)?.let { target ->
                val accumulated = currentMilestones[entry.name]?.accumulated ?: 0L
                entry.name to Milestone(
                    target = target,
                    isTotal = entry.isTotal,
                    accumulated = if (entry.isTotal) 0L else accumulated
                )
            }
        }

        val milestones = (collectionMilestones + skillMilestones).toMap()
        ConfigHelper.saveMilestones(milestones)

        when (currentPage) {
            Page.COLLECTIONS -> showSuccess("Collection milestones saved successfully!")
            Page.SKILLS -> showSuccess("Skill milestones saved successfully!")
        }
    }

    private fun hasDuplicates(names: List<String>): Boolean {
        val seen = HashSet<String>()
        return names.any { !seen.add(it.lowercase()) }
    }

    protected fun updateEntry(index: Int, name: String? = null, value: String? = null, isTotal: Boolean? = null) {
        entries[index].apply {
            name?.let { this.name = it }
            value?.let { this.value = it }
            isTotal?.let { this.isTotal = it }
        }
    }

    protected fun removeEntry(index: Int) {
        entries.removeAt(index)
        if (entries.isEmpty()) {
            entries += Entry("", "", false)
        }
    }

    protected fun addNewEntry() {
        entries += Entry("", "", false)
    }

    protected fun createEditBox(value: String, x: Int, y: Int): EditBox =
        EditBox(MinecraftUtils.font, x, y, 70, 20, Component.literal("Milestone Value")).apply {
            this.value = value
            maxLength = 32
        }

    protected fun drawHeaders(context: GuiGraphicsExtractor) {
        val y = panelTop() + 40
        val headerName = if (currentPage == Page.COLLECTIONS) "Collection Name" else "Skill Name"

        context.centeredText(font, Component.literal("Total"), totalColumnX, y, Colors.WHITE.color)
        context.centeredText(font, Component.literal(headerName), nameColumnX, y, Colors.WHITE.color)
        context.centeredText(font, Component.literal("Milestone Value"), valueColumnX, y, Colors.WHITE.color)
    }
}