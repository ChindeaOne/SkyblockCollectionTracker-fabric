package io.github.chindeaone.collectiontracker.utils.render

import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.sqrt

fun GuiGraphicsExtractor.pushPopMatrix(block: () -> Unit) {
    this.pose().pushMatrix()
    block()
    this.pose().popMatrix()
}

fun GuiGraphicsExtractor.translate(x: Number, y: Number) {
    this.pose().translate(x.toFloat(), y.toFloat())
}

fun GuiGraphicsExtractor.scale(x: Number, y: Number) {
    this.pose().scale(x.toFloat(), y.toFloat())
}

fun GuiGraphicsExtractor.scissor(x0: Int, y0: Int, x1: Int, y1: Int, block: () -> Unit) {
    this.enableScissor(x0, y0, x1 - x0, y1 - y0)
    block()
    this.disableScissor()
}

fun GuiGraphicsExtractor.drawTooltipBox(x: Int, y: Int, width: Int, height: Int) {
    val padding = 4
    val x1 = (x - padding)
    val y1 = (y - padding)
    val x2 = (x + width + padding)
    val y2 = (y + height + padding)

    val color = Colors.GRAY.color

    fill(x1, y1, x2, y2, ColorUtils.TOOLTIP_BG)

    fill(x1, y1, x2, y1 + 1, color) // Top
    fill(x1, y2 - 1, x2, y2, color) // Bottom
    fill(x1, y1, x1 + 1, y2, color) // Left
    fill(x2 - 1, y1, x2, y2, color) // Right
}

fun GuiGraphicsExtractor.drawRoundedOutline(x: Int, y: Int, width: Int, height: Int, radius: Int, color: Int) {
    if (width <= 0 || height <= 0) return

    val r = radius.coerceIn(0, 3)
        .coerceAtMost(width / 2)
        .coerceAtMost(height / 2)

    if (r <= 0) {
        fill(x, y, x + width, y + 1, color)
        fill(x, y + height - 1, x + width, y + height, color)
        fill(x, y + 1, x + 1, y + height - 1, color)
        fill(x + width - 1, y + 1, x + width, y + height - 1, color)
        return
    }

    fill(x + r, y, x + width - r, y + 1, color)
    fill(x + r, y + height - 1, x + width - r, y + height, color)
    fill(x, y + r, x + 1, y + height - r, color)
    fill(x + width - 1, y + r, x + width, y + height - r, color)

    for (i in 1 until r) {
        val offset = r - i

        fill(x + i, y + offset, x + i + 1, y + offset + 1, color)
        fill(x + width - i - 1, y + offset, x + width - i, y + offset + 1, color)

        fill(x + i, y + height - offset - 1, x + i + 1, y + height - offset, color)
        fill(x + width - i - 1, y + height - offset - 1, x + width - i, y + height - offset, color)
    }
}

fun GuiGraphicsExtractor.drawRoundedRect(width: Int, height: Int, radius: Int) {
    val x = 0
    val y = -4
    val color = ColorUtils.DUMMY_BG

    if (radius <= 0) {
        fill(x, y, x + width, y + height, color)
        return
    }

    val r = radius.coerceAtMost(width / 2).coerceAtMost(height / 2)
    val alpha = (color shr 24 and 0xFF)
    val rgb = color and 0xFFFFFF

    // main
    fill(x + r, y, x + width - r, y + r, color)
    fill(x, y + r, x + width, y + height - r, color)
    fill(x + r, y + height - r, x + width - r, y + height, color)

    // corners with AA
    for (cx in 0 until r) {
        for (cy in 0 until r) {
            val dx = (r - cx - 0.5)
            val dy = (r - cy - 0.5)
            val dist = sqrt(dx * dx + dy * dy)

            val currAlpha = when {
                dist < r - 1.0 -> alpha // fully opaque
                dist < r -> ((r - dist) * alpha).toInt()
                else -> 0
            }

            if (currAlpha > 0) {
                val newColor = (currAlpha shl 24) or rgb

                fill(x + cx, y + cy, x + cx + 1, y + cy + 1, newColor) // top left
                fill(x + width - cx - 1, y + cy, x + width - cx, y + cy + 1, newColor) // top right
                fill(x + cx, y + height - cy - 1, x + cx + 1, y + height - cy, newColor) // bottom left
                fill(x + width - cx - 1, y + height - cy - 1, x + width - cx, y + height - cy, newColor) // bottom right
            }
        }
    }
}