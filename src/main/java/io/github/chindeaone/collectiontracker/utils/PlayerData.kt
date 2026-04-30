package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft

object PlayerData {

    val playerUUID: String
        get() = Minecraft.getInstance().player?.uuid.toString().replace("-", "")

    val playerName: String
        get() = Minecraft.getInstance().player?.gameProfile?.name ?: "Unknown"

    @Volatile
    private var profileIdInternal: String? = null

    val profileId: String?
        get() = profileIdInternal

    @JvmStatic
    fun updateProfileId(newId: String) {
        val trimmed = newId.trim()
        if (trimmed.isEmpty()) return
        if (profileIdInternal == null || !profileIdInternal.equals(trimmed, ignoreCase = true)) {
            profileIdInternal = trimmed
        }
    }
}