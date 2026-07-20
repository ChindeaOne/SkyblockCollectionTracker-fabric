package io.github.chindeaone.collectiontracker.api.tokenapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object TokenFetcher {

    private val logger: Logger = LogManager.getLogger(TokenFetcher::class.java)

    fun fetchToken(notify: Boolean): String? {
        return try {
            val serverId = ApiManager.serverId
            if (serverId == null) {
                logger.error("[SCT]: Mojang authentication was not completed.")
                return null
            }

            val headers = listOf(
                "X-UUID" to PlayerData.profileId.toString(),
                "X-NAME" to PlayerData.playerName,
                "X-SERVER-ID" to serverId
            )

            val response = ApiManager.request("token", headers)

            if (response.statusCode() != 200) {
                logger.error("[SCT]: Failed to fetch token, response code: {}", response.statusCode())
                if (notify) {
                    sendMessage("§cFailed to fetch token.", true)
                }

                null
            } else {
                val json = JsonParser.parseString(response.body()).asJsonObject
                logger.info("[SCT]: Successfully fetched token")
                if (notify) {
                    sendMessage("§aSuccessfully fetched a new token.", true)
                }

                json["token"].asString
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to fetch token", e)
            return null
        }
    }
}
