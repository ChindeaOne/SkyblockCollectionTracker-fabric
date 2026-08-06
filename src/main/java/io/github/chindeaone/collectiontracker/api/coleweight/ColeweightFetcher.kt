package io.github.chindeaone.collectiontracker.api.coleweight

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.net.http.HttpResponse

object ColeweightFetcher {

    private val logger = LogManager.getLogger(ColeweightFetcher::class.java)

    @Volatile
    var hasColeweightLb = false
        private set

    @Volatile
    var hasColeweightTopColors = false
        private set

    fun fetchColeweightDataAsync(
        playerName: String,
        uuid: String,
        onComplete: Runnable?
    ) {
        ApiManager.requestAsync("coleweight", authHeaders(uuid, playerName)).thenAccept { response ->
            if (response.statusCode() == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
                TokenManager.fetchAndStoreToken()

                handleColeweightResponse(authenticatedGet(authHeaders(uuid, playerName)), playerName) { body ->
                    ColeweightManager.updateColeweight(body)
                    logger.info("[SCT]: Successfully fetched Coleweight for {}", playerName)
                    runCallback(onComplete)
                }
                return@thenAccept
            }

            handleColeweightResponse(response, playerName) { body ->
                ColeweightManager.updateColeweight(body)
                logger.info("[SCT]: Successfully fetched Coleweight for {}", playerName)
                runCallback(onComplete)
            }
        }.exceptionally {
            logger.error("[SCT]: Error fetching Coleweight.", it)
            null
        }
    }

    fun fetchColeweightLbAsync(onComplete: Runnable?) {
        ApiManager.requestAsync("coleweight/lb")
            .thenAccept { response ->

                handleStringResponse(
                    response,
                    "Received empty response when fetching Coleweight leaderboard."
                ) { body ->
                    ColeweightManager.updateColeweightLb(body, false)
                    logger.info("[SCT]: Successfully fetched Coleweight leaderboard.")
                    runCallback(onComplete)
                }

            }
            .exceptionally {
                logger.error("[SCT]: Error fetching Coleweight leaderboard.", it)
                null
            }
    }

    fun fetchColeweightLbTop1k() {
        try {
            val response = ApiManager.request("coleweight/top1k")

            handleStringResponse(response, "Received empty response when fetching Coleweight leaderboard.") { body ->
                ColeweightManager.updateColeweightLb(body, true)
                hasColeweightLb = true
                logger.info("[SCT]: Successfully fetched Coleweight leaderboard.")
            }

        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Coleweight leaderboard.", e)
        }
    }

    fun fetchColeweightData(playerName: String, uuid: String): String? {
        return try {
            val response = authenticatedGet(authHeaders(uuid, playerName))

            when (response.statusCode()) {

                200 -> {
                    logger.info("[SCT]: Successfully fetched Coleweight for {}", playerName)
                    response.body()
                }

                else -> {
                    sendError("§cCouldn't find your coleweight.")
                    logger.warn("[SCT]: Failed to fetch Coleweight for {}. HTTP {}", playerName, response.statusCode())
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Coleweight.", e)
            null
        }
    }

    fun setGlobalColor(playerName: String, uuid: String, color: String) {
        try {
            val headers = authHeaders(uuid, playerName).apply {
                add("X-COLOR" to color)
            }
            val response = authenticatedPost(headers)

            if (response.statusCode() == 200) {
                logger.info("[SCT]: Successfully set global color for {}", playerName)

                ChatUtils.sendComponent(
                    Component.empty()
                        .append("§aGlobal color set to ")
                        .append(ColorUtils.coloredText(color))
                        .append("."),
                    true
                )
            } else {
                logger.warn(
                    "[SCT]: Failed to set global color for {}. HTTP {}",
                    playerName,
                    response.statusCode()
                )
                sendError("§cFailed to set global Coleweight color.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error setting Coleweight color.", e)
        }
    }

    fun fetchColeweightTopColors() {
        try {
            val response = ApiManager.request("coleweight/colors")

            handleStringResponse(response, "Received empty response when fetching Coleweight top colors.") { body ->
                ColeweightManager.updateColeweightTopColors(body)
                hasColeweightTopColors = true
                logger.info("[SCT]: Successfully fetched Coleweight top colors.")
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching Coleweight top colors.", e)
        }
    }

    private fun authHeaders(uuid: String, playerName: String) = mutableListOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to uuid,
        "X-NAME" to playerName
    )

    private fun sendError(message: String) {
        ChatUtils.sendMessage(message, true)
    }

    private fun runCallback(callback: Runnable?) {
        try {
            callback?.run()
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while executing callback.", e)
        }
    }

    private fun authenticatedGet(headers: List<Pair<String, String>>): HttpResponse<String> {
        var response = ApiManager.request("coleweight", headers)

        if (response.statusCode() == 401) {
            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
            TokenManager.fetchAndStoreToken()

            response = ApiManager.request(
                "coleweight",
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

        var response = ApiManager.post("coleweight/color", headers)

        if (response.statusCode() == 401) {
            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")

            TokenManager.fetchAndStoreToken()

            response = ApiManager.post(
                "coleweight/color",
                headers.map {
                    if (it.first == "Authorization")
                        "Authorization" to "Bearer ${TokenManager.token}"
                    else it
                }
            )
        }
        return response
    }

    private inline fun handleColeweightResponse(
        response: HttpResponse<String>,
        playerName: String,
        onSuccess: (String) -> Unit
    ) {
        when (response.statusCode()) {

            200 -> {
                val body = response.body()

                if (body.isNullOrBlank()) {
                    sendError("§cCouldn't find $playerName's coleweight.")
                    logger.warn("[SCT]: Empty response for {}", playerName)
                    return
                }
                onSuccess(body)
            }

            429 -> {
                logger.warn("[SCT]: Coleweight API rate limit exceeded.")
                sendError("§cColeweight fetching limit reached! Try again later.")
            }

            else -> {
                logger.warn("[SCT]: Failed to fetch Coleweight for {}. HTTP {}", playerName, response.statusCode())
                sendError("§cCouldn't find $playerName's coleweight.")
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

            else -> logger.warn(
                "[SCT]: Request failed. HTTP {}",
                response.statusCode()
            )
        }
    }
}