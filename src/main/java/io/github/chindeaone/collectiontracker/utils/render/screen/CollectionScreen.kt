package io.github.chindeaone.collectiontracker.utils.render.screen

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseButton
import io.github.chindeaone.collectiontracker.utils.render.screen.core.BaseScrollableScreen
import io.github.chindeaone.collectiontracker.utils.toColor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

class CollectionScreen(private val collectionList: List<String>, private val onCancel: Runnable? = null) : BaseScrollableScreen(null) {

    private val map = mutableMapOf<String, EditBox>()
    private var confirmed = false

    private val rowHeight = 28

    override val contentHeight: Int
        get() = collectionList.size * rowHeight

    override val screenTitle = Component.literal("Collections")

    private val message = Component.literal("ⓘ Couldn't reach Hypixel's API, so you have to set your collection values manually.")

    override fun initContent() {
        addRenderableWidget(
            BaseButton(width / 2 - 40, panelBottom - 30, 80, 20, { Component.literal("Confirm") }) {
                val values = map.mapValues { NumbersUtils.parseValue(it.value.value) ?: 0L }

                if (collectionList.size == 1 && !collectionList.contains("gemstone")) {
                    TrackingRates.setCollection(values.values.first())
                } else {
                    MultiTrackingRates.setCollections(values)
                }

                ChatUtils.sendMessage("§eCustom collection values set:")

                values.forEach { (name, value) ->
                    val displayName = StringUtils.formatCollectionName(name).toColor()
                    val formattedValue = NumbersUtils.formatNumber(value)
                    val component = Component.literal(" §7- §f")
                        .append(displayName)
                        .append(": §a$formattedValue")

                    ChatUtils.sendComponent(component, false)
                }

                confirmed = true
                onClose()
            }
        )
        rebuildEntryWidgets()
    }

    private fun rebuildEntryWidgets() {
        if (collectionList.isEmpty()) {
            ChatUtils.sendMessage("§cNo collections to set custom values for.")
            onClose()
            return
        }

        map.clear()

        collectionList.forEachIndexed { index, name ->
            val y = contentTop + index * rowHeight - scrollOffset

            if (y + 10 < contentTop || y > contentBottom - 10) {
                return@forEachIndexed
            }

            val displayName = StringUtils.formatCollectionName(name)

            val box = object: EditBox(MinecraftUtils.font, width / 2 - 25, y, 70, 20, Component.literal(displayName)) {
                override fun insertText(input: String) {
                    super.insertText(input.filter { it.isDigit() || it in ".,kmbKMB" })
                }
            }.apply {
                this.value = ""
                maxLength = 32
            }

            map[name] = box
            addRenderableWidget(box)
        }
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)

        map.forEach { (name, box) ->
            val displayName = StringUtils.formatCollectionName(name)
            context.text(
                MinecraftUtils.font,
                displayName,
                box.x - MinecraftUtils.font.width(displayName) - 10,
                box.y + 5,
                ColorUtils.collectionColors[name] ?: Colors.WHITE.color
            )
        }

        context.centeredText(MinecraftUtils.font, message, width / 2, panelBottom + 2, Colors.GRAY.color)
    }

    override fun onClose() {
        if (!confirmed) {
            onCancel?.run()
        }
        super.onClose()
    }
}