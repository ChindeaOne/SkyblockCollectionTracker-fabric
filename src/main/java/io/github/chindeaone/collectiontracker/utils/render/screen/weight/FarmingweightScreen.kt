package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class FarmingweightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    playerName: String
): BaseWeightScreen(oldScreen, playerName) {

    override val contentHeight: Int = 28

    override val screenTitle: Component
        get() = Component.literal("Farming Weight Profile").withColor(ColorUtils.FARMINGWEIGHT)

    override val weightName: String
        get() = "Farming Weight"

    override val widgetBgColor: Int
        get() = ScreenColors.FARMINGWEIGHT_WIDGET_BG.color

    override fun isProfileInit(): Boolean = FarmingweightManager.loadedPlayer == playerName

    private val firstY: Int
        get() = contentTop + verticalGap

    private val storage
        get() = FarmingweightManager.storage

    override val weight: Float
        get() = storage.weight

    override fun addLeftWidgets() {
        addWeightSection(
            "Crop Weight",
            storage.cropWeight["totalWeight"] ?: 0f,
            ScreenColors.FARMINGWEIGHT_WIDGET_BG.color,
            storage.cropWeight,
            "Farming Weight",
            playerName,
            leftX,
            firstY
        )
    }

    override fun addRightWidgets() {
        addWeightSection(
            "Bonus Weight",
            storage.bonusWeight["totalWeight"] ?: 0f,
            ScreenColors.FARMINGWEIGHT_WIDGET_BG.color,
            storage.bonusWeight,
            "Farming Weight",
            playerName,
            rightX,
            firstY
        )
    }
}