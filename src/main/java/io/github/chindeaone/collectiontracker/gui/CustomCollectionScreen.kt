package io.github.chindeaone.collectiontracker.gui

import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatCollectionName
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class CustomCollectionScreen(
    private val collectionList: List<String>,
    private val onCancel: Runnable? = null
) : Screen(Component.literal("Enter custom collection value")) {

    private val map = mutableMapOf<String, EditBox>()
    private var confirmed = false

    override fun init() {
        if (collectionList.isEmpty()) {
            ChatUtils.sendMessage("§cNo collections to set custom values for.")
            onClose()
            return
        }

        val height = collectionList.size * 20
        val startY = this.height / 2 - height / 2 - 20

        collectionList.forEachIndexed { index, s ->
            val yPos = startY + (index * 20)
            val displayName = formatCollectionName(s)
            val box = CollectionEditBox(
                font,
                width / 2 - 25,
                yPos,
                100,
                20,
                Component.literal(displayName)
            )
            map[s] = box
            addRenderableWidget(box)
        }

        val buttonY = startY + height + 10
        addRenderableWidget(
            Button.builder(Component.literal("Confirm")) { _ ->
                val values = map.mapValues { parseCustomValue(it.value.value) }

                if (collectionList.size == 1 && !collectionList.contains("gemstone")) {
                    TrackingRates.setCollection(values.values.first())
                } else {
                    MultiTrackingRates.setCollections(values)
                }
                ChatUtils.sendMessage("§eCustom collection values set:")

                values.forEach { (name, value) ->
                    val displayName = formatCollectionName(name)
                    val formattedValue = NumbersUtils.formatNumber(value)
                    ChatUtils.sendMessage(" §7- §f$displayName: §a$formattedValue", false)
                }
                confirmed = true
                onClose()
            }.bounds(width / 2 - 100, buttonY, 200, 20).build()
        )
    }

    override fun onClose() {
        if (!confirmed) {
            onCancel?.run()
        }
        super.onClose()
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        extractMenuBackground(context)

        context.centeredText(
            font,
            title,
            width / 2,
            20,
            0xFFFFFFFF.toInt()
        )

        map.forEach { (name, box) ->
            val displayName = formatCollectionName(name)
            context.text(
                font,
                displayName,
                box.x - font.width(name) - 10,
                box.y + 5,
                0xFFFFFFFF.toInt()
            )
        }

        super.extractRenderState(context, mouseX, mouseY, delta)
    }

    override fun shouldCloseOnEsc(): Boolean = true

    private fun parseCustomValue(value: String): Long {
        val input = value.trim().lowercase()
        if (input.isEmpty()) return 0L

        val multiplier = when {
            input.endsWith("k") -> 1_000L
            input.endsWith("m") -> 1_000_000L
            input.endsWith("b") -> 1_000_000_000L
            else -> 1L
        }

        val number = if (multiplier > 1L)
            input.dropLast(1)
        else {
            input
        }

        return try {
            (number.toDouble() * multiplier).toLong()
        } catch (_: NumberFormatException) {
            0L
        }
    }

    class CollectionEditBox(
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        message: Component
    ) : EditBox(font, x, y, width, height, message) {

        override fun insertText(input: String) {
            val filtered = input.filter {
                it.isDigit() || it == '.' || it == ',' || it.lowercaseChar() in listOf('k', 'm', 'b')
            }

            super.insertText(filtered)
        }
    }
}