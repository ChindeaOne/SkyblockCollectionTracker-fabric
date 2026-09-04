package io.github.chindeaone.collectiontracker.utils

import java.util.UUID

object PlayerData {

    private var cachedPlayerName: String = ""

    fun init() {
        cachedPlayerName = MinecraftUtils.name
    }

    val cachedName: String get() = cachedPlayerName

    val playerUUID: String get() = profileId.toString().replace("-", "")

    val playerName: String get() = MinecraftUtils.name

    val profileId: UUID get() = MinecraftUtils.profileId

    val accessToken: String get() = MinecraftUtils.accessToken
}