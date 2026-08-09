package io.github.chindeaone.collectiontracker.commands

import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.util.Mth

import java.util.*
import kotlin.math.ceil

object CollectionList {

    private const val PAGE_SIZE = 15 // Max collections per page

    private data class Page(
        val category: String,
        val color: Int,
        val collections: List<String>
    )

    fun sendCollectionList(page: Int) {
        val categoryColors = getCategoryColors()

        // Ordered categories
        val categories = CollectionsManager.collections.entries

        val pages = ArrayList<Page>()
        for (entry in categories) {
            val category = entry.key
            val color = categoryColors[category]!!

            val allCollections = ArrayList(entry.value)
            if (allCollections.isEmpty()) {
                pages.add(Page(category, color, emptyList()))
                continue
            }

            for (i in allCollections.indices step PAGE_SIZE) {
                val end = (i + PAGE_SIZE).coerceAtMost(allCollections.size)
                val sub = allCollections.subList(i, end)
                pages.add(Page(category, color, sub))
            }
        }
        if (pages.isEmpty()) return

        val totalPages = pages.size
        val page = Mth.clamp(page, 1, totalPages)

        val current = pages[page - 1]

        ChatUtils.sendCategoryPage(current.category, current.color, current.collections, page, totalPages)
    }

    private fun getCategoryColors(): MutableMap<String, Int> {
        val categoryColors = mutableMapOf<String, Int>()
        categoryColors["Farming"] = Colors.GREEN.color
        categoryColors["Mining"] = Colors.GOLD.color
        categoryColors["Combat"] = Colors.RED.color
        categoryColors["Foraging"] = Colors.DARK_GREEN.color
        categoryColors["Fishing"] = Colors.AQUA.color
        categoryColors["Rift"] = Colors.DARK_PURPLE.color
        categoryColors["Miscellaneous"] = Colors.DARK_GRAY.color
        return categoryColors
    }

    fun getPageForCategory(categoryInput: String): Int? {
        val collectionsMap = CollectionsManager.collections

        var pageIndex = 1

        for (entry in collectionsMap.entries) {
            val category = entry.key
            val allCollections = ArrayList(entry.value)

            val pagesForThisCategory = 1.coerceAtLeast(ceil(allCollections.size / PAGE_SIZE.toDouble()).toInt())

            if (category.equals(categoryInput, ignoreCase = true)) {
                return pageIndex // first page of this category
            }

            pageIndex += pagesForThisCategory
        }

        return null
    }
}