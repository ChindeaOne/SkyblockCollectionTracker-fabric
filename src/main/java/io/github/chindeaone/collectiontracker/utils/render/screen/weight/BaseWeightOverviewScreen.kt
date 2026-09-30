package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import com.mojang.authlib.GameProfile
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.chindeaone.collectiontracker.utils.GameProfileUtils
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import io.github.chindeaone.collectiontracker.utils.render.screen.core.LoadingWidget
import io.github.chindeaone.collectiontracker.utils.render.screen.core.PlayerMannequinWidget
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component

abstract class BaseWeightOverviewScreen(oldScreen: AbstractContainerScreen<*>?, protected val playerName: String): BaseScrollableScreen(oldScreen) {

    data class WeightCategory(val title: String, val total: Float, val entries: Map<String, Float>)

    protected abstract val weightName: String
    protected abstract val totalWeight: Float
    protected abstract val totalWeightColor: Int
    protected abstract val widgetBgColor: Int
    protected abstract fun isProfileInit(): Boolean
    protected abstract fun getCategories(): Pair<List<WeightCategory>, List<WeightCategory>>

    override val contentHeight: Int = 28

    protected val columnWidth: Int
        get() = panelWidth / 2

    protected val widgetWidth = 120
    protected val widgetHeight = 30
    protected val verticalGap = 20

    private val mannequinWidth: Int
        get() = (panelWidth * 0.25f).toInt()

    private val mannequinHeight: Int
        get() = (mannequinWidth * 1.2f).toInt()

    private val mannequinX: Int
        get() = panelLeft + (panelWidth - mannequinWidth) / 2

    private val mannequinY: Int
        get() = panelTop + (panelHeight - mannequinHeight) / 2

    private val weightY: Int
        get() = mannequinY + mannequinHeight - 40

    private var profileLoaded = false
    private var gameProfileLoaded = false

    override fun initContent() {
        profileLoaded = isProfileInit()

        if (!profileLoaded && !gameProfileLoaded) {
            showLoading()
            return
        }

        buildCategoryCards()
        loadGameProfile()
    }

    private fun buildCategoryCards() {
        val (leftCategories, rightCategories) = getCategories()

        leftCategories.forEachIndexed { i, cat ->
            val y = contentTop + verticalGap + i * (widgetHeight + verticalGap) - scrollOffset
            if (y + widgetHeight >= contentTop && y <= contentBottom) {
                addRenderableWidget(
                    WeightWidget(centeredInColumn(0), y, widgetWidth, widgetHeight, cat.title, cat.total, totalWeight, widgetBgColor) {
                        openDetailedView(cat)
                    })
            }
        }

        rightCategories.forEachIndexed { i, cat ->
            val y = contentTop + verticalGap + i * (widgetHeight + verticalGap) - scrollOffset
            if (y + widgetHeight >= contentTop && y <= contentBottom) {
                addRenderableWidget(
                    WeightWidget(centeredInColumn(1), y, widgetWidth, widgetHeight, cat.title, cat.total, totalWeight, widgetBgColor) {
                        openDetailedView(cat)
                    })
            }
        }
    }

    protected abstract fun reopenScreen()

    protected open fun openDetailedView(category: WeightCategory) {
        if (category.title == "Mining Experience") return

        GuiManager.openWeightBreakdownScreen(
            weightName = category.title,
            widgetBgColor = widgetBgColor,
            weightEntries = category.entries,
            onBack = { reopenScreen() }
        )
    }

    private fun loadGameProfile() {
        GameProfileUtils.getProfile(playerName)?.let {
            displayGameProfile(it)
            return
        }

        ApiManager.fetchGameProfile(playerName).thenAccept { profile ->
            if (profile == null) return@thenAccept

            GameProfileUtils.addProfile(playerName, profile)
            minecraft.execute {
                displayGameProfile(profile)
            }
        }
    }

    private fun displayGameProfile(profile: GameProfile) {
        gameProfileLoaded = true

        val nameTag = Component.literal(profile.name)
        val player = getPlayerDisplay(profile, nameTag)

        player.arrangeElements()
        player.visitWidgets(this::addRenderableWidget)
    }

    private fun getPlayerDisplay(profile: GameProfile, nameTag: Component): LinearLayout {
        val playerWidget = PlayerMannequinWidget(profile, nameTag, mannequinWidth, mannequinHeight)

        return LinearLayout.vertical().apply {
            x = mannequinX
            y = mannequinY
            addChild(playerWidget)
        }
    }

    override fun tick() {
        super.tick()

        if (!profileLoaded && isProfileInit()) {
            profileLoaded = true
            rebuildWidgets()
        }
    }

    protected fun showLoading() {
        if (isProfileInit()) return

        val loading = LoadingWidget()
        FrameLayout.centerInRectangle(loading, 0, 0, width, height)

        addRenderableWidget(loading)
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)

        if (!isProfileInit()) return

        renderTotalWeight(context)
    }

    protected open fun renderTotalWeight(context: GuiGraphicsExtractor) {
        val centerX = panelLeft + panelWidth / 2
        context.centeredText(font, Component.literal("$weightName: %.2f".format(totalWeight)), centerX, weightY, totalWeightColor)
    }

    protected fun centeredInColumn(column: Int): Int {
        val columnLeft = panelLeft + column * columnWidth
        return columnLeft + (columnWidth - widgetWidth) / 2
    }
}