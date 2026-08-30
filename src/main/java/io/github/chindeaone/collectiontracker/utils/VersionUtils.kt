package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker

object VersionUtils {

    val mcVersions = mutableListOf<String>()

    fun parseVersions(json: JsonObject) {
        val obj = json.getAsJsonArray("versions")
        for (element in obj) {
            val version = element.asString
            mcVersions.add(version)
        }
    }

    fun checkIfVersionIsSupported(): Boolean {
        val currentVersion = SkyblockCollectionTracker.MC_VERSION
        return mcVersions.any { currentVersion.startsWith(it) }
    }
}