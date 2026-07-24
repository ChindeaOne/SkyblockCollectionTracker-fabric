package io.github.chindeaone.collectiontracker.updater

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.ApiManager.HTTP_CLIENT
import io.github.chindeaone.collectiontracker.api.ApiManager.agent
import io.github.chindeaone.collectiontracker.api.serverapi.RepoUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

object ModrinthData {
    private val logger: Logger = LogManager.getLogger(ModrinthData::class.java)
    private const val MODRINTH_API = "https://api.modrinth.com/v2/project/sct/version"

    fun getModLink(): String? {
        val mcVersion = SkyblockCollectionTracker.MC_VERSION
        val modVersion = RepoUtils.latestVersion ?: return null

        return try {

            val queryParams = listOf(
                "loaders=fabric",
                "game_versions=$mcVersion",
                "version=$modVersion"
            ).joinToString("&")

            val url = "$MODRINTH_API?$queryParams"

            val request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", agent)
                .header("Accept", "application/json")
                .GET()
                .build()

            val response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

            if (response.statusCode() != 200) {
                logger.warn("[SCT]: Modrinth API returned status code ${response.statusCode()}")
                return null
            }

            val parsedJson = JsonParser.parseString(response.body())

            if (!parsedJson.isJsonArray) {
                logger.warn("[SCT]: Modrinth API response is not a JSON array")
                return null
            }

            val jsonArray = parsedJson.asJsonArray

            if (jsonArray.size() == 0) {
                logger.warn("[SCT]: No versions found on Modrinth for mc=$mcVersion, mod=$modVersion")
                return null
            }

            val versionObj = jsonArray[0].asJsonObject

            if (!versionObj.has("version_number") || versionObj["version_number"].isJsonNull) {
                logger.warn("[SCT]: version_number missing in Modrinth version object")
                return null
            }

            val versionNumber = versionObj.get("version_number").asString
            val versionPageUrl = "https://modrinth.com/mod/sct/version/$versionNumber"

            logger.info("[SCT]: Found mod version page: $versionPageUrl")
            versionPageUrl

        } catch (e: Exception) {
            logger.error("[SCT]: Failed to fetch mod link from Modrinth", e)
            null
        }
    }
}