package io.github.chindeaone.collectiontracker.utils.rendering

import net.minecraft.client.Minecraft

object ScaleUtils {

    private val mc = Minecraft.getInstance()
    private val mouse = mc.mouseHandler

    val height get() = mc.window.height
    val width get() = mc.window.width
    val scale get() = mc.window.guiScale
    val scaledHeight get() = mc.window.guiScaledHeight
    val scaledWidth get() = mc.window.guiScaledWidth
    val mouseX: Int get() {
        return (mouse.xpos() * scaledWidth / width).toInt()
    }
    val mouseY: Int get() {
        return (mouse.ypos() * scaledHeight / height).toInt()
    }
}