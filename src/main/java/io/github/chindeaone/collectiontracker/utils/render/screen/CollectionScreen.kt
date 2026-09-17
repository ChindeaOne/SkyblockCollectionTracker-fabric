package io.github.chindeaone.collectiontracker.utils.render.screen

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseButton
import io.github.chindeaone.collectiontracker.utils.rendering.screen.core.BaseListScreen
import io.github.chindeaone.collectiontracker.utils.toColor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

class CollectionScreen(
    private val collectionList: List<String>,
    private val onCancel: Runnable? = null
) : BaseListScreen(null) {

    private val map = mutableMapOf<String, EditBox>()
    private var confirmed = false

    override val entryCount: Int
        get() = collectionList.size

    override val screenTitle = Component.literal("Collections")

    override val message = Component.literal("ⓘ Couldn't reach Hypixel's API, so you have to set your collection values manually.")

    override fun initButtons() {
        addRenderableWidget(
            BaseButton(width / 2 - 40, panelBottom() - 30, 80, 20, { Component.literal("Confirm") }) {
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
    }

    override fun onClose() {
        if (!confirmed) {
            onCancel?.run()
        }
        super.onClose()
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

        context.centeredText(MinecraftUtils.font, message, width / 2, panelBottom() + 2, Colors.GRAY.color)
    }

    override fun rebuildEntryWidgets() {
        if (collectionList.isEmpty()) {
            ChatUtils.sendMessage("§cNo collections to set custom values for.")
            onClose()
            return
        }

        map.clear()

        collectionList.forEachIndexed { index, name ->
            val y = contentTop + index * rowHeight - currentScrollOffset

            if (y + 10 < contentTop || y > contentBottom - 10) {
                return@forEachIndexed
            }

            val displayName = StringUtils.formatCollectionName(name)

            val box = createInputBox(
                "",
                width / 2 - 25,
                y,
                displayName
            )

            map[name] = box
            addRenderableWidget(box)
        }
    }
}