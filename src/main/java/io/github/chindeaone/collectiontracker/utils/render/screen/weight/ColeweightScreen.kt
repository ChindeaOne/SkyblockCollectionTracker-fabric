package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class ColeweightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    playerName: String
): BaseWeightScreen(oldScreen, playerName) {

    override val contentHeight: Int = 28

    override val screenTitle: Component
        get() = Component.literal("Coleweight Profile").withColor(ColorUtils.COLEWEIGHT)

    override fun isProfileInit(): Boolean = ColeweightManager.loadedPlayer == playerName

    private val firstY: Int
        get() = contentTop + verticalGap

    private val secondY: Int
        get() = firstY + widgetHeight + 2 * verticalGap

    private val storage
        get() = ColeweightManager.storage

    override val weight: Float
        get() = storage.coleweight

    override fun addLeftWidgets() {
        addWeightSection(
            "Mining Experience",
            storage.experience["total"] ?: 0f,
            leftX,
            firstY
        )

        addWeightSection(
            "Collection",
            storage.collection["total"] ?: 0f,
            leftX,
            secondY
        )
    }

    override fun addRightWidgets() {
        addWeightSection(
            "Powder",
            storage.powder["total"] ?: 0f,
            rightX,
            firstY
        )

        addWeightSection(
            "Miscellaneous",
            storage.miscellaneous["total"] ?: 0f,
            rightX,
            secondY
        )
    }
}