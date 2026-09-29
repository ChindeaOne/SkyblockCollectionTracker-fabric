package io.github.chindeaone.collectiontracker.utils

import com.mojang.authlib.GameProfile

object GameProfileUtils {

    private val profiles = mutableMapOf<String, GameProfile>()

    fun getProfile(playerName: String): GameProfile? =
        profiles[playerName]

    fun addProfile(playerName: String, profile: GameProfile) {
        profiles[playerName] = profile
    }
}