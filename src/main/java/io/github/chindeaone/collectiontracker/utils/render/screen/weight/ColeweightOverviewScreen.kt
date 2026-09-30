package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

class ColeweightOverviewScreen(oldScreen: AbstractContainerScreen<*>?, playerName: String): BaseWeightOverviewScreen(oldScreen, playerName) {

    private val storage
        get() = ColeweightManager.storage

    override val weightName: String = "Coleweight"
    override val totalWeight: Float get() = storage.coleweight
    override val totalWeightColor: Int get() = ScreenColors.COLEWEIGHT_TEXT.color

    override val screenTitle: Component
        get() = Component.literal("Coleweight Profile").withColor(ColorUtils.COLEWEIGHT)

    override fun isProfileInit(): Boolean = ColeweightManager.loadedPlayer == playerName

    override val widgetBgColor get() = ScreenColors.COLEWEIGHT_WIDGET_BG.color

    override fun getCategories(): Pair<List<WeightCategory>, List<WeightCategory>> {
        val left = listOf(
            WeightCategory("Mining Experience", storage.experience["total"] ?: 0f, storage.experience),
            WeightCategory("Collection", storage.collection["total"] ?: 0f, storage.collection)
        )
        val right = listOf(
            WeightCategory("Powder", storage.powder["total"] ?: 0f, storage.powder),
            WeightCategory("Miscellaneous", storage.miscellaneous["total"] ?: 0f, storage.miscellaneous)
        )
        return left to right
    }

    override fun reopenScreen() {
        GuiManager.openColeweightScreen(playerName)
    }
}