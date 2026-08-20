package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft
import java.util.UUID

object PlayerData {

    private var cachedPlayerName: String = ""

    fun init() {
        val user = Minecraft.getInstance().user
        cachedPlayerName = user.name
    }

    val cachedName: String
        get() = cachedPlayerName

    @JvmStatic
    val playerUUID: String
        get() = profileId.toString().replace("-", "")

    @JvmStatic
    val playerName: String
        get() = Minecraft.getInstance().user.name

    val profileId: UUID
        get() = Minecraft.getInstance().user.profileId

    val accessToken: String
        get() = Minecraft.getInstance().user.accessToken
}