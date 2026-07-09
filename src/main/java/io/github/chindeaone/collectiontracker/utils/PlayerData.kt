package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft

object PlayerData {

    @JvmStatic
    val playerUUID: String
        get() = Minecraft.getInstance().player?.uuid.toString().replace("-", "")

    @JvmStatic
    val playerName: String
        get() = Minecraft.getInstance().player?.gameProfile?.name ?: "Unknown"
}