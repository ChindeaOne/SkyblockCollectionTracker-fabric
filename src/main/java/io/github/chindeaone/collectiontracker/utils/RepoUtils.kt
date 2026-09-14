package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.categories.About

object RepoUtils {

    var latestVersion: String? = null

    @Volatile
    var latestReleaseTag: String? = null

    @Volatile
    var latestBetaTag: String? = null

    @Volatile
    var latestNotes: String? = null

    @Volatile
    var latestReleaseNotes: String? = null

    @Volatile
    var latestBetaNotes: String? = null

    private val currentVersion = SkyblockCollectionTracker.VERSION

    fun parseData(json: JsonObject) {
        latestReleaseTag = getNullableString(json, "latest_tag")
        latestBetaTag = getNullableString(json, "latest_beta_tag")
        latestReleaseNotes = getNullableString(json, "latest_release_notes")
        latestBetaNotes = getNullableString(json, "latest_beta_notes")
    }

    fun checkLatestVersion() {
        latestReleaseTag = normalizeTags(latestReleaseTag)
        latestBetaTag = normalizeTags(latestBetaTag)

        val isBeta = ConfigAccess.getUpdateStream() == About.UpdateStream.BETA

        val chosenTag = if (isBeta) latestBetaTag else latestReleaseTag
        val chosenNotes = if (isBeta) latestBetaNotes else latestReleaseNotes

        latestNotes = chosenNotes
        latestVersion = null

        if (chosenTag == null || currentVersion == chosenTag) {
            return
        }

        if (compareBaseVersion(chosenTag) >= 0) {
            latestVersion = chosenTag
        }
    }

    private fun normalizeTags(tag: String?): String? {
        tag ?: return null

        var normalizedTag = tag
        if (tag.startsWith("v")) normalizedTag = tag.substring(1)

        // Remove metadata if present
        val plusIndex = normalizedTag.indexOf('+')
        if (plusIndex != -1) {
            normalizedTag = normalizedTag.substring(0, plusIndex)
        }

        return normalizedTag
    }

    private fun compareBaseVersion(v1: String?): Int {
        v1 ?: return 0

        val a = v1.substringBefore('-').split('.')
        val b = currentVersion.substringBefore('-').split('.')

        for (i in 0 until 3) {
            val n1 = a.getOrNull(i)?.toIntOrNull() ?: 0
            val n2 = b.getOrNull(i)?.toIntOrNull() ?: 0
            if (n1 != n2) return n1.compareTo(n2)
        }
        return 0 // same major.minor.beta
    }

    private fun getNullableString(objectJson: JsonObject, key: String): String? {
        return if (objectJson.has(key) && !objectJson.get(key).isJsonNull) {
            objectJson.get(key).asString
        } else {
            null
        }
    }
}