package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class ColeweightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    playerName: String
): BaseWeightScreen(oldScreen, playerName) {

    override val contentHeight: Int = 28

    override val screenTitle: Component
        get() = Component.literal("Coleweight Profile").withColor(ColorUtils.COLEWEIGHT)

    override val weightName: String
        get() = "Coleweight"

    override val widgetBgColor: Int
        get() = ScreenColors.COLEWEIGHT_WIDGET_BG.color

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
            ScreenColors.COLEWEIGHT_WIDGET_BG.color,
            storage.experience,
            "Coleweight",
            playerName,
            leftX,
            firstY
        )

        addWeightSection(
            "Collection",
            storage.collection["total"] ?: 0f,
            ScreenColors.COLEWEIGHT_WIDGET_BG.color,
            storage.collection,
            "Coleweight",
            playerName,
            leftX,
            secondY
        )
    }

    override fun addRightWidgets() {
        addWeightSection(
            "Powder",
            storage.powder["total"] ?: 0f,
            ScreenColors.COLEWEIGHT_WIDGET_BG.color,
            storage.powder,
            "Coleweight",
            playerName,
            rightX,
            firstY
        )

        addWeightSection(
            "Miscellaneous",
            storage.miscellaneous["total"] ?: 0f,
            ScreenColors.COLEWEIGHT_WIDGET_BG.color,
            storage.miscellaneous,
            "Coleweight",
            playerName,
            rightX,
            secondY
        )
    }
}