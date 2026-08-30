package io.github.chindeaone.collectiontracker.api.serverapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.RepoUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object FetchRepoData {

    private val logger: Logger = LogManager.getLogger(FetchRepoData::class.java)

    fun checkGithubReleases(): CompletableFuture<Void> {
        return ApiManager.requestAsync("github", listOf("X-MINECRAFT-VERSION" to SkyblockCollectionTracker.MC_VERSION))
            .thenAccept { response ->
                val status = response.statusCode()
                if (status != 200) {
                    logger.error("[SCT]: Failed to fetch GitHub releases, response code: {}", status)
                    return@thenAccept
                }

                val jsonResponse = JsonParser.parseString(response.body()).asJsonObject

                RepoUtils.parseData(jsonResponse)
                logger.info("[SCT]: Successfully fetched GitHub releases")
            }
            .exceptionally { e ->
                logger.error("[SCT]: Error fetching GitHub releases: ", e)
                null
            }
    }
}