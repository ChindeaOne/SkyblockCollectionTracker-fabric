package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.gui.GuiManager.getEditorInstance
import io.github.chindeaone.collectiontracker.gui.GuiManager.openEditor
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.roundToInt

/**
 * A general abstract class for all overlays
 */
abstract class AbstractOverlay {
    var isRenderingAllowed: Boolean = true

    abstract val overlayLabel: String

    abstract val position: Position

    open val isEnabled: Boolean get() = false

    fun shouldRender(): Boolean {
        val mc = Minecraft.getInstance()
        return !mc./*? if 26.2 {*/ /*gui.hud.isHidden() */ /*?} else {*/options.hideGui /*?}*/
                && !mc.debugEntries.isOverlayVisible
                && isEnabled
                && isRenderingAllowed
    }

    open fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return

        val lines = lines
        if (lines.isEmpty()) return

        RenderUtils.drawOverlayFrame(context, position) { RenderUtils.renderStrings(context, lines) }
    }

    open fun updateDimensions() {
        if (!HypixelUtils.isInSkyblock && !isEnabled) return

        val lines = lines
        if (lines.isEmpty()) {
            position.setDimensions(0, 0)
            return
        }

        val fr = Minecraft.getInstance().font
        var maxW = 0
        for (l in lines) maxW = maxOf(maxW, fr.width(l))
        val h = fr.lineHeight * lines.size

        position.setDimensions(maxW, h)
    }

    open val lines: List<String> get() = emptyList()

    open fun handleLineAction(line: String) {}

    fun handleMouseClick(mouseX: Double, mouseY: Double): Boolean {
        if (!isEnabled || !isHovered(mouseX, mouseY)) return false

        val lines = lines
        if (lines.isEmpty()) return false

        val position = position
        val x = position.x
        val y = position.y
        val scale = position.scale

        val fr = Minecraft.getInstance().font
        val height = (fr.lineHeight * lines.size * scale).toInt()
        val width = (position.width * scale).toInt()

        if (mouseX in x.toDouble()..(x + width).toDouble() && mouseY in y.toDouble()..(y + height).toDouble()) {
            val relativeY = mouseY - y
            val lineClicked = (relativeY / (fr.lineHeight * scale)).toInt()

            if (lineClicked in lines.indices) {
                handleLineAction(lines[lineClicked])
                return true
            }
        }
        return false
    }

    open fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        val pos = position

        val padding = 4

        val x = pos.x
        val y = pos.y
        val scale = pos.scale

        val width = (pos.width * scale).roundToInt()
        val height = (pos.height * scale).roundToInt()

        val x2 = (x + width).toDouble()
        val y1 = (y - padding * scale).toDouble()
        val y2 = (y + height + padding * scale).toDouble()

        return mouseX in x.toDouble()..x2 && mouseY in y1..y2
    }

    fun jumpToConfig() {
        val pos = position

        val editor: MoulConfigEditor<*> = getEditorInstance()
        val option = editor.getOptionFromField(pos.link) ?: return

        editor.search("")
        if (!editor.goToOption(option)) return
        openEditor(editor)
    }
}
