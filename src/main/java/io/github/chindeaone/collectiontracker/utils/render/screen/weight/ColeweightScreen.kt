package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.ScreenColors
import net.minecraft.client.gui.GuiGraphicsExtractor
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
        get() = firstY + sectionHeight + 2 * verticalGap

    private val storage
        get() = ColeweightManager.storage

    override fun renderWeightSections(context: GuiGraphicsExtractor) {
        for (section in weightSections) {
            val percentage = section.total / storage.coleweight * 100f

            val component = Component.literal("%.2f (%.2f%%)".format(section.total, percentage))

            context.fill(section.x, section.y, section.x + sectionWidth, section.y + sectionHeight, ScreenColors.BUTTON_HOVER.color)

            val centerX = section.x + sectionWidth / 2
            context.centeredText(font, Component.literal(section.title), centerX, section.y + 6, Colors.WHITE.color)
            context.centeredText(font, component, centerX, section.y + 6 + font.lineHeight, Colors.WHITE.color)
        }
    }

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