package io.github.chindeaone.collectiontracker.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatUtils {

    private const val SCT = "§6SCT§r"
    private const val PREFIX = "§o§3[${SCT}§3] §r"

    fun sendMessage(message: String, prefix: Boolean = true) {
        val text = if (prefix) "$PREFIX$message" else message
        Minecraft.getInstance().player?.displayClientMessage(Component.literal(text), false)
    }
    fun sendMessage() {
        sendMessage("", prefix = false)
    }
}