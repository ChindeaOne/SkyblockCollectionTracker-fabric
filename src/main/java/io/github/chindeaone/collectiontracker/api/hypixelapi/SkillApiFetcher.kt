package io.github.chindeaone.collectiontracker.api.hypixelapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.http.HttpResponse

object SkillApiFetcher {

    private val logger: Logger = LogManager.getLogger(SkillApiFetcher::class.java)

    @JvmStatic
    fun fetchSkillsData() {
        try {            
            val headers = listOf(
                "Authorization" to "Bearer ${TokenManager.token}",
                "X-UUID" to PlayerData.playerUUID,
            )

            val response = ApiManager.request("skills", headers)

            when(val status = response.statusCode()) {
                401 -> {
                    logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
                    TokenManager.fetchAndStoreToken()

                    val headersWithNewToken = listOf(
                        "Authorization" to "Bearer ${TokenManager.token}",
                        "X-UUID" to PlayerData.playerUUID,
                    )
                    val responseWithNewToken = ApiManager.request("skills", headersWithNewToken)
                    val statusWithNewToken = responseWithNewToken.statusCode()
                    
                    if (statusWithNewToken == 200) {
                        processSkillsResponse(responseWithNewToken)
                    } else {
                        logger.error("[SCT]: Failed to fetch skill data after token refresh. HTTP {}", statusWithNewToken)
                    }
                }

                200 -> {
                    processSkillsResponse(response)
                }

                else -> {
                    logger.error("[SCT]: Failed to fetch skill data. HTTP {}", status)
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error while receiving the skill data", e)
        }
    }
    
    private fun processSkillsResponse(response: HttpResponse<String>) {
        val skills = Gson().fromJson<Map<String, Double>>(
            response.body(),
            object : TypeToken<Map<String, Double>>() {}.type
        )
            
        if (skills.isEmpty()) {
            ChatUtils.sendMessage("§c[SCT] Skill API disabled. Please enable it in the settings.", true)
            logger.warn("[SCT]: Skill API disabled for player.")
            SkillTrackingHandler.stopTracking()
            return
        }

        SkillUtils.updateFromApi(skills)
        logger.info("[SCT]: Successfully received the skill data.")
    }
}