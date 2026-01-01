package io.github.chindeaone.collectiontracker.util.rendering

import net.minecraft.client.Minecraft

object ScaleUtils {

    private val mc = Minecraft.getInstance()

    val height get() = mc.window.height
    val width get() = mc.window.width
}