package io.github.chindeaone.collectiontracker.utils.render.screen.leaderboard

import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.leaderboardPositions
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseListScreen
import io.github.chindeaone.collectiontracker.utils.render.screen.core.Page
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseLeaderboardScreen(
    oldScreen: AbstractContainerScreen<*>?
): BaseListScreen(oldScreen) {

    protected data class Entry(var name: String, var position: String)

    protected val collectionEntries = mutableListOf<Entry>()
    protected val skillEntries = mutableListOf<Entry>()

    protected val entries: MutableList<Entry>
        get() = when (currentPage) {
            Page.COLLECTIONS -> collectionEntries
            Page.SKILLS -> skillEntries
        }

    override val entryCount: Int
        get() = entries.size

    protected val nameColumnX: Int
        get() = panelLeft() + (panelWidth * 0.25f).toInt()

    protected val valueColumnX: Int
        get() = panelLeft() + (panelWidth * 0.5f).toInt()

    protected val actionColumnX: Int
        get() = panelLeft() + (panelWidth * 0.7f).toInt()

    override fun loadData() {
        collectionEntries.clear()
        skillEntries.clear()

        leaderboardPositions.forEach { (name, position) ->
            val entry = Entry(name, position.toString())

            if (CollectionsManager.isValidCollection(name)) {
                collectionEntries += entry
            } else if (SkillUtils.isValidSkill(name)) {
                skillEntries += entry
            }
        }

        if (collectionEntries.isEmpty()) {
            collectionEntries += Entry("", "")
        }

        if (skillEntries.isEmpty()) {
            skillEntries += Entry("", "")
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

        val collectionPositions = filledCollections.mapNotNull { entry ->
            NumbersUtils.parseValue(entry.position)?.let { entry.name to it.toInt() }
        }

        val skillPositions = filledSkills.mapNotNull { entry ->
            NumbersUtils.parseValue(entry.position)?.let { entry.name to it.toInt() }
        }

        val positions = (collectionPositions + skillPositions).toMap()
        ConfigHelper.saveLeaderboardPositions(positions)

        when (currentPage) {
            Page.COLLECTIONS -> showSuccess("Collection leaderboard positions saved successfully!")
            Page.SKILLS -> showSuccess("Skill leaderboard positions saved successfully!")
        }
    }

    private fun hasDuplicates(names: List<String>): Boolean {
        val seen = HashSet<String>()
        return names.any { !seen.add(it.lowercase()) }
    }

    protected fun updateEntry(index: Int, name: String? = null, position: String? = null) {
        entries[index].apply {
            name?.let { this.name = it }
            position?.let { this.position = it }
        }
    }

    protected fun removeEntry(index: Int) {
        entries.removeAt(index)
        if (entries.isEmpty()) {
            entries += Entry("", "")
        }
    }

    protected fun addNewEntry() {
        entries += Entry("", "")
    }

    protected fun drawHeaders(context: GuiGraphicsExtractor) {
        val y = panelTop() + 40
        val headerName = if (currentPage == Page.COLLECTIONS) "Collection Name" else "Skill Name"

        context.centeredText(font, Component.literal(headerName), nameColumnX, y, Colors.WHITE.color)
        context.centeredText(font, Component.literal("Position"), valueColumnX, y, Colors.WHITE.color)
    }
}