package io.github.chindeaone.collectiontracker.utils.render

import io.github.chindeaone.collectiontracker.utils.MinecraftUtils

object ScaleUtils {

    private val window = MinecraftUtils.window
    private val mouseHandler = MinecraftUtils.mouseHandler

    val height get() = window.height
    val width get() = window.width
    val scale get() = window.guiScale
    val scaledHeight get() = window.guiScaledHeight
    val scaledWidth get() = window.guiScaledWidth
    val mouseX: Int get() = (mouseHandler.xpos() * scaledWidth / width).toInt()
    val mouseY: Int get() =(mouseHandler.ypos() * scaledHeight / height).toInt()
}