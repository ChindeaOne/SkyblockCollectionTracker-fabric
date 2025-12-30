package io.github.chindeaone.collectiontracker.util

import net.minecraft.client.Minecraft

object PlayerData {

    val playerUUID: String get() = Minecraft.getInstance().player?.uuid.toString().replace("-", "")

    val playerName: String get() = Minecraft.getInstance().player?.name.toString()
}