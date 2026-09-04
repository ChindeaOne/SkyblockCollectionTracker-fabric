package io.github.chindeaone.collectiontracker.utils

object HypixelUtils {

    private val HypixelServer get() = Hypixel.server

    val isInHypixel get() = HypixelServer && MinecraftUtils.player != null

    @JvmStatic
    val isInSkyblock get() = isInHypixel && Hypixel.skyblock
}