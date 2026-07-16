package io.github.chindeaone.collectiontracker.api.serverapi

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.categories.About
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object RepoUtils {

    private val logger: Logger = LogManager.getLogger(RepoUtils::class.java)

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

    @JvmStatic
    fun checkGithubReleases(): Boolean {
        return try {
            val headers = listOf(
                "MC_VERSION" to SkyblockCollectionTracker.MC_VERSION
            )

            val response = ApiManager.request("github", headers)

            val status = response.statusCode()
            if (status != 200) {
                logger.error("[SCT]: Failed to fetch GitHub releases, response code: {}", status)
                return false
            }

            val jsonResponse = JsonParser.parseString(response.body()).asJsonObject
            logger.info("[SCT]: Successfully fetched GitHub releases")

            latestReleaseTag = getNullableString(jsonResponse, "latest_tag")
            latestBetaTag = getNullableString(jsonResponse, "latest_beta_tag")
            latestReleaseNotes = getNullableString(jsonResponse, "latest_release_notes")
            latestBetaNotes = getNullableString(jsonResponse, "latest_beta_notes")

            true
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching GitHub releases: ", e)
            false
        }
    }

    @JvmStatic
    fun checkLatestVersion() {
        latestReleaseTag = normalizeTags(latestReleaseTag)
        latestBetaTag = normalizeTags(latestBetaTag)

        val chosenTag = if (ConfigAccess.getUpdateType() == About.UpdateType.BETA) {
            latestBetaTag
        } else {
            latestReleaseTag
        }

        val chosenNotes = if (ConfigAccess.getUpdateType() == About.UpdateType.BETA) {
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

        // Remove 'v' prefix if present
        if (tag.startsWith("v")) {
            normalizedTag = tag.substring(1)
        }

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