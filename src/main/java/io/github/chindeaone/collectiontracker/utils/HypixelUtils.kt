package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft

object HypixelUtils {

    private val HypixelServer get() = Hypixel.server

    val isInHypixel get() = HypixelServer && Minecraft.getInstance().player != null

    @JvmStatic
    val isOnSkyblock get() = isInHypixel && Hypixel.skyblock
}