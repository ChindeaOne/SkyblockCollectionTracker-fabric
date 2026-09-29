package io.github.chindeaone.collectiontracker.utils.render.screen.weight

import com.mojang.authlib.GameProfile
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.GameProfileUtils
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import io.github.chindeaone.collectiontracker.utils.render.screen.core.LoadingWidget
import io.github.chindeaone.collectiontracker.utils.render.screen.core.PlayerMannequinWidget
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

abstract class BaseWeightScreen(
    oldScreen: AbstractContainerScreen<*>?,
    protected val playerName: String
): BaseScrollableScreen(oldScreen) {

    private data class WeightWidget(val title: String, val total: Float, val color: Int, val entries: Map<String, Float>, val weightType: String, val playerName: String, val x: Int, val y: Int)
    private val weightWidgets = mutableListOf<WeightWidget>()

    protected val columnWidth: Int
        get() = panelWidth / 2

    protected val widgetWidth = 120
    protected val widgetHeight = 40
    protected val verticalGap = 20

    protected val leftX: Int
        get() = centeredInColumn(0)

    protected val rightX: Int
        get() = centeredInColumn(1)

    protected abstract val widgetBgColor: Int

    private val mannequinX: Int
        get() = panelLeft + (panelWidth - mannequinWidth) / 2

    private val mannequinY: Int
        get() = panelTop + (panelHeight - mannequinHeight) / 2

    private val weightY: Int
        get() = mannequinY + mannequinHeight - 40

    private val mannequinWidth: Int
        get() = (panelWidth * 0.25f).toInt()

    private val mannequinHeight: Int
        get() = (mannequinWidth * 1.2f).toInt()

    private var profileLoaded = false
    private var gameProfileLoaded = false

    protected open fun isProfileInit(): Boolean = false

    protected abstract val weight: Float

    protected abstract val weightName: String

    override fun initContent() {
        super.initContent()
        profileLoaded = isProfileInit()

        if (!profileLoaded && !gameProfileLoaded) {
            showLoading()
            return
        }

        rebuildEntryWidgets()
        loadGameProfile()
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

    private fun rebuildEntryWidgets() {
        addLeftWidgets()
        addRightWidgets()
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
        renderWeightWidgets(context)
    }

    private fun renderWeightWidgets(context: GuiGraphicsExtractor) {
        weightWidgets.forEach { widget ->
            val percentage = widget.total / weight * 100f
            val component = Component.literal("%.2f (%.2f%%)".format(widget.total, percentage))

            context.fill(widget.x, widget.y, widget.x + widgetWidth, widget.y + widgetHeight, widgetBgColor)

            val centerX = widget.x + widgetWidth / 2

            context.centeredText(font, Component.literal(widget.title), centerX, widget.y + 6, Colors.WHITE.color)
            context.centeredText(font, component, centerX, widget.y + 6 + font.lineHeight, Colors.WHITE.color)
        }
    }

    protected open fun renderTotalWeight(context: GuiGraphicsExtractor) {
        val centerX = panelLeft + panelWidth / 2

        context.centeredText(font, Component.literal("$weightName: %.2f".format(weight)), centerX, weightY, Colors.WHITE.color)
    }

    protected open fun addLeftWidgets() {}
    protected open fun addRightWidgets() {}

    protected fun addWeightSection(title: String, total: Float, color: Int, entries: Map<String, Float>, weightType: String, playerName: String, x: Int, y: Int) {
        weightWidgets += WeightWidget(title, total, color, entries, weightType, playerName, x, y)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x
        val mouseY = event.y

        if (event.button() == 0) {
            weightWidgets.firstOrNull { widget ->
                mouseX >= widget.x && mouseX < widget.x + widgetWidth && mouseY >= widget.y && mouseY < widget.y + widgetHeight }?.let { widget ->
                    GuiManager.openDetailedWeightScreen(
                        widget.title,
                        widget.weightType,
                        playerName,
                        widget.color,
                        widget.entries
                    )

                return true
            }
        }

        return super.mouseClicked(event, doubleClick)
    }

    override fun initButtons() {}

    protected fun centeredInColumn(column: Int): Int {
        val columnLeft = panelLeft + column * columnWidth
        return columnLeft + (columnWidth - widgetWidth) / 2
    }

    override fun onClose() {
        weightWidgets.clear()
        super.onClose()
    }
}