package io.github.chindeaone.collectiontracker.api.hypixelapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

object SkillApiFetcher {

    private val logger: Logger = LogManager.getLogger(SkillApiFetcher::class.java)

    fun fetchSkillsData(): CompletableFuture<Void> {
        return ApiManager.requestAsync("skills", headers())
            .thenAccept { response ->
                when (response.statusCode()) {
                    200 -> processSkillsResponse(response)
                    404 -> {
                        ChatUtils.sendMessage("§eSkill API disabled in game or Hypixel API is down. Setting skill values manually...", true)
                        logger.warn("[SCT]: Skill API disabled in game.")
                    }
                    else -> logger.error("[SCT]: Failed to fetch skill data. HTTP ${response.statusCode()}")
                }
            }
            .exceptionally { e ->
                logger.error("[SCT]: Error while receiving the skill data", e)
                null
            }
    }
    
    private fun processSkillsResponse(response: HttpResponse<String>) {
        val skills = Gson().fromJson<Map<String, Double>>(
            response.body(),
            object : TypeToken<Map<String, Double>>() {}.type
        )

        SkillUtils.updateFromApi(skills)
        logger.info("[SCT]: Successfully received the skill data.")
    }

    private fun headers() = mapOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to PlayerData.playerUUID,
    )
}