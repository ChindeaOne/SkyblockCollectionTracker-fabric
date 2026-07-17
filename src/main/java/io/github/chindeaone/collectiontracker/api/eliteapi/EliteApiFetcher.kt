package io.github.chindeaone.collectiontracker.api.eliteapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.net.http.HttpResponse

object EliteApiFetcher {

    private val logger = LogManager.getLogger(EliteApiFetcher::class.java)

    @Volatile
    var hasFarmingweightLb = false
        private set

    @Volatile
    var hasFarmingweightTopColors = false
        private set

    fun fetchFarmingweightDataAsync(
        playerName: String,
        uuid: String,
        onComplete: Runnable?
    ) {
        ApiManager.requestAsync("farmingweight", authHeaders(uuid, playerName)).thenAccept { response ->
            if (response.statusCode() == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
                TokenManager.fetchAndStoreToken()

                handleFarmingweightResponse(authenticatedGet("farming", authHeaders(uuid, playerName)), playerName) { body ->
                    FarmingweightManager.updateFarmingweight(body)
                    logger.info("[SCT]: Successfully fetched Farming Weight for {}", playerName)
                    runCallback(onComplete)
                }
                return@thenAccept
            }

            handleFarmingweightResponse(response, playerName) { body ->
                FarmingweightManager.updateFarmingweight(body)
                logger.info("[SCT]: Successfully fetched Farming Weight for {}", playerName)
                runCallback(onComplete)
            }

        }.exceptionally {
            logger.error("[SCT]: Error fetching Farming Weight.", it)
            null
        }
    }

    fun fetchFarmingweightLbAsync(onComplete: Runnable?) {
        ApiManager.requestAsync("farmingweight/lb")
            .thenAccept { response ->
                handleStringResponse(response, "Received empty response when fetching Farming Weight leaderboard.") { body ->
                    FarmingweightManager.updateFarmingweightLb(body, false)
                    logger.info("[SCT]: Successfully fetched Farming Weight leaderboard.")
                    runCallback(onComplete)
                }

            }.exceptionally {
                logger.error("[SCT]: Error fetching Farming Weight leaderboard.", it)
                null
            }
    }

    fun fetchFarmingweightLbTop1k() {
        try {
            val response = ApiManager.request("farmingweight/top1k")

            handleStringResponse(response, "Received empty response when fetching Farming Weight leaderboard.") { body ->
                FarmingweightManager.updateFarmingweightLb(body, true)
                hasFarmingweightLb = true
                logger.info("[SCT]: Successfully fetched Farming Weight leaderboard.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Farming Weight leaderboard.", e)
        }
    }

    fun setGlobalColor(playerName: String, uuid: String, color: String) {
        try {
            val headers = authHeaders(uuid, playerName).apply {
                add("X-COLOR" to color)
            }

            val response = authenticatedPost(headers)

            if (response.statusCode() == 200) {
                logger.info("[SCT]: Successfully set global Farming Weight color for {}", playerName)

                Minecraft.getInstance().execute {
                    ChatUtils.sendComponent(
                        Component.empty()
                            .append("§aGlobal color set to ")
                            .append(ColorUtils.coloredText(color))
                            .append("."),
                        true
                    )
                }
            } else {
                logger.warn("[SCT]: Failed to set global Farming Weight color for {}. HTTP {}", playerName, response.statusCode())
                sendError("§cFailed to set global Farming Weight color.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error setting Farming Weight color.", e)
        }
    }

    fun fetchFarmingweightTopColors() {
        try {
            val response = ApiManager.request("farmingweight/colors")

            handleStringResponse(response, "Received empty response when fetching Farming Weight top colors.") { body ->
                FarmingweightManager.updateFarmingweightTopColors(body)
                hasFarmingweightTopColors = true
                logger.info("[SCT]: Successfully fetched Farming Weight top colors.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Farming Weight top colors.", e)
        }
    }

    @JvmStatic
    fun fetchCollectionLeaderboard(collection: String): String? {
        return try {
            val response = authenticatedGet(
                "collection/leaderboard/${collection.replace(" ", "-")}",
                mutableListOf(
                    "Authorization" to "Bearer ${TokenManager.token}",
                    "X-UUID" to PlayerData.playerUUID
                )
            )

            when (response.statusCode()) {
                200 -> response.body()

                429 -> {
                    logger.warn("[SCT]: Farming Weight API rate limit exceeded.")
                    null
                }

                else -> {
                    logger.error("[SCT]: Failed to fetch leaderboard data. HTTP {}", response.statusCode())
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching leaderboard data.", e)
            null
        }
    }

    private fun authHeaders(uuid: String, playerName: String) = mutableListOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to uuid,
        "X-NAME" to playerName
    )

    private fun sendError(message: String) {
        Minecraft.getInstance().execute {
            ChatUtils.sendMessage(message, true)
        }
    }

    private fun runCallback(callback: Runnable?) {
        try {
            callback?.run()
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while executing callback.", e)
        }
    }

    private fun authenticatedGet(path: String, headers: List<Pair<String, String>>): HttpResponse<String> {
        var response = ApiManager.request(path, headers)

        if (response.statusCode() == 401) {
            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
            TokenManager.fetchAndStoreToken()

            response = ApiManager.request(path,
                headers.map {
                    if (it.first == "Authorization")
                        "Authorization" to "Bearer ${TokenManager.token}"
                    else it
                }
            )
        }
        return response
    }

    private fun authenticatedPost(headers: List<Pair<String, String>>): HttpResponse<String> {
        var response = ApiManager.post("farmingweight/color", headers)

        if (response.statusCode() == 401) {
            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
            TokenManager.fetchAndStoreToken()

            response = ApiManager.post("farming/color",
                headers.map {
                    if (it.first == "Authorization")
                        "Authorization" to "Bearer ${TokenManager.token}"
                    else it
                }
            )
        }
        return response
    }

    private inline fun handleFarmingweightResponse(
        response: HttpResponse<String>,
        playerName: String,
        onSuccess: (String) -> Unit
    ) {
        when (response.statusCode()) {
            200 -> {
                val body = response.body()

                if (body.isNullOrBlank()) {
                    sendError("§cCouldn't find $playerName's Farming Weight.")
                    logger.warn("[SCT]: Empty response for {}", playerName)
                    return
                }
                onSuccess(body)
            }

            429 -> {
                logger.warn("[SCT]: Farming Weight API rate limit exceeded.")
                sendError("§cFarming Weight fetching limit reached! Try again later.")
            }

            else -> {
                logger.warn("[SCT]: Failed to fetch Farming Weight for {}. HTTP {}", playerName, response.statusCode())
                sendError("§cCouldn't find $playerName's Farming Weight.")
            }
        }
    }

    private inline fun handleStringResponse(
        response: HttpResponse<String>,
        emptyMessage: String,
        success: (String) -> Unit
    ) {
        when (response.statusCode()) {
            200 -> {
                val body = response.body()

                if (body.isNullOrBlank()) {
                    logger.warn(emptyMessage)
                    return
                }
                success(body)
            }

            else -> logger.warn("[SCT]: Request failed. HTTP {}",response.statusCode())
        }
    }
}

