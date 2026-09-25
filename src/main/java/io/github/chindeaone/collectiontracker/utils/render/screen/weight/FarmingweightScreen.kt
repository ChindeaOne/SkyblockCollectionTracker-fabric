package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class FarmingweightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    playerName: String
): BaseWeightScreen(oldScreen, playerName) {

    override val contentHeight: Int = 28

    override val screenTitle: Component
        get() = Component.literal("Farmingweight Profile").withColor(ColorUtils.FARMINGWEIGHT)

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
            leftX,
            firstY
        )
    }

    override fun addRightWidgets() {
        addWeightSection(
            "Bonus Weight",
            storage.bonusWeight["totalWeight"] ?: 0f,
            rightX,
            firstY
        )
    }
}