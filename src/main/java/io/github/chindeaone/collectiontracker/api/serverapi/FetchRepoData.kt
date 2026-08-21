package io.github.chindeaone.collectiontracker.api.serverapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.RepoUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchRepoData {

    private val logger: Logger = LogManager.getLogger(FetchRepoData::class.java)

    fun checkGithubReleases(): Boolean {
        return try {
            val headers = listOf(
                "X-MINECRAFT-VERSION" to SkyblockCollectionTracker.MC_VERSION
            )

            val response = ApiManager.request("github", headers)

            val status = response.statusCode()
            if (status != 200) {
                logger.error("[SCT]: Failed to fetch GitHub releases, response code: {}", status)
                return false
            }

            val jsonResponse = JsonParser.parseString(response.body()).asJsonObject

            RepoUtils.parseData(jsonResponse)
            logger.info("[SCT]: Successfully fetched GitHub releases")
            true
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching GitHub releases: ", e)
            false
        }
    }
}