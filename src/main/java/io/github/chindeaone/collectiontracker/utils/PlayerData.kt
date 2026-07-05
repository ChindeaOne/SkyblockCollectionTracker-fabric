package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft

object PlayerData {

    val playerUUID: String
        get() = Minecraft.getInstance().player?.uuid.toString().replace("-", "")

    val playerName: String
        get() = Minecraft.getInstance().player?.gameProfile?.name ?: "Unknown"
}