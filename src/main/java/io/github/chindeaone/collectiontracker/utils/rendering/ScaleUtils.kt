package io.github.chindeaone.collectiontracker.utils.rendering

import net.minecraft.client.Minecraft

object ScaleUtils {

    private val mc = Minecraft.getInstance()

    val height get() = mc.window.height
    val width get() = mc.window.width
    val scale get() = mc.window.guiScale
}