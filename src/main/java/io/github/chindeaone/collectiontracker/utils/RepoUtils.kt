package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.categories.About

object RepoUtils {

    @JvmStatic
    var latestVersion: String? = null
        private set

    @JvmStatic
    var latestReleaseTag: String? = null
        private set

    @JvmStatic
    var latestBetaTag: String? = null
        private set

    @JvmStatic
    var latestNotes: String? = null
        private set

    private var latestReleaseNotes: String? = null
    private var latestBetaNotes: String? = null

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

        val chosenTag = if (ConfigAccess.getUpdateStream() == About.UpdateStream.BETA) {
            latestBetaTag
        } else {
            latestReleaseTag
        }

        val chosenNotes = if (ConfigAccess.getUpdateStream() == About.UpdateStream.BETA) {
            latestBetaNotes
        } else {
            latestReleaseNotes
        }

        if (chosenTag == null) {
            latestVersion = null
            latestNotes = null
            return
        }

        // If already on that same version -> no update
        if (currentVersion == chosenTag) {
            latestVersion = null
            latestNotes = chosenNotes
            return
        }

        // Prevent downgrades
        val baseCompare = compareBaseVersion(chosenTag)

        if (baseCompare > 0) {
            // Target has higher major/minor/beta -> update
            latestVersion = chosenTag
            latestNotes = chosenNotes
        } else if (baseCompare == 0) {
            latestVersion = chosenTag
            latestNotes = chosenNotes
        } else {
            // Target is older -> don't update
            latestVersion = null
            latestNotes = null
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