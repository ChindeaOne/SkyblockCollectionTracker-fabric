package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class FarmingweightOverviewScreen(oldScreen: AbstractContainerScreen<*>?, playerName: String): BaseWeightOverviewScreen(oldScreen, playerName) {

    private val storage
        get() = FarmingweightManager.storage

    override val weightName: String = "Farming Weight"
    override val totalWeight: Float get() = storage.weight
    override val totalWeightColor: Int get() = ScreenColors.FARMINGWEIGHT_TEXT.color

    override val screenTitle: Component
        get() = Component.literal("Farming Weight Profile").withColor(ColorUtils.FARMINGWEIGHT)

    override fun isProfileInit(): Boolean = FarmingweightManager.loadedPlayer == playerName

    override val widgetBgColor get() = ScreenColors.FARMINGWEIGHT_WIDGET_BG.color

    override fun getCategories(): Pair<List<WeightCategory>, List<WeightCategory>> {
        val left = listOf(
            WeightCategory("Crop Weight", storage.cropWeight["totalWeight"] ?: 0f, storage.cropWeight)
        )
        val right = listOf(
            WeightCategory("Bonus Weight", storage.bonusWeight["totalWeight"] ?: 0f, storage.bonusWeight)
        )
        return left to right
    }

    override fun reopenScreen() {
        GuiManager.openFarmingweightScreen(playerName)
    }
}