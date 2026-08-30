package io.github.chindeaone.collectiontracker.api.coleweight

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture

object ColeweightFetcher {

    private val logger = LogManager.getLogger(ColeweightFetcher::class.java)

    @Volatile
    var hasColeweightLb = false
        private set

    @Volatile
    var hasColeweightTopColors = false
        private set

    fun fetchColeweightData(playerName: String, uuid: String): CompletableFuture<String?> {
        return ApiManager.requestAsync("coleweight", authHeaders(uuid, playerName))
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            ChatUtils.sendMessage("§cCouldn't find $playerName's coleweight.")
                            logger.warn("[SCT]: Empty response for {}", playerName)
                            null
                        } else body
                    }
                    429 -> {
                        logger.warn("[SCT]: Coleweight API rate limit exceeded.")
                        ChatUtils.sendMessage("§cColeweight fetching limit reached! Try again later.")
                        null
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Coleweight for {}. HTTP {}", playerName, response.statusCode())
                        ChatUtils.sendMessage("§cCouldn't find $playerName's coleweight.")
                        null
                    }
                }
            }.exceptionally {
                logger.error("[SCT]: Error fetching Coleweight.", it)
                null
            }
    }

    fun fetchColeweightLeaderboard(): CompletableFuture<String?> {
        return ApiManager.requestAsync("coleweight/lb")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Coleweight leaderboard.")
                            null
                        } else body
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Coleweight leaderboard. HTTP {}", response.statusCode())
                        null
                    }
                }
            }.exceptionally {
                logger.error("[SCT]: Error fetching Coleweight leaderboard.", it)
                null
            }
    }

    fun fetchColeweightLbTop1k() {
        ApiManager.requestAsync("coleweight/top1k")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Coleweight top 1k.")
                            null
                        } else body
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Coleweight top 1k. HTTP {}", response.statusCode())
                        null
                    }
                }
            }.thenAccept { body ->
                if (body != null) {
                    ColeweightManager.updateColeweightLb(body, true)
                }
            }.exceptionally {
                logger.error("[SCT]: Error fetching Coleweight top 1k.", it)
                null
            }
    }

    fun setGlobalColor(playerName: String, uuid: String, color: String): CompletableFuture<Boolean> {
        return ApiManager.postAsync("coleweight/color", authHeaders(uuid, playerName).apply {
            remove("X-NAME")
            put("X-COLOR", color)
        }).thenApply { response ->
            if (response.statusCode() == 200) {
                logger.info("[SCT]: Successfully set global color for {}", playerName)
                true
            } else {
                logger.warn("[SCT]: Failed to set global color for {}. HTTP {}", playerName, response.statusCode())
                false
            }
        }.exceptionally { e ->
            logger.error("[SCT]: Error setting Coleweight color.", e)
            false
        }
    }

    fun fetchColeweightTopColors() {
        ApiManager.requestAsync("coleweight/colors")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Coleweight top colors.")
                            null
                        } else body
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Coleweight top colors. HTTP {}", response.statusCode())
                        null
                    }
                }
            }.thenAccept { body ->
                if (body != null) {
                    ColeweightManager.updateColeweightTopColors(body)
                    hasColeweightTopColors = true
                    logger.info("[SCT]: Successfully fetched Coleweight top colors.")
                }
            }.exceptionally {
                logger.error("[SCT]: Error fetching Coleweight top colors.", it)
                null
            }
    }

    private fun authHeaders(uuid: String, playerName: String) = mutableMapOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to uuid,
        "X-NAME" to playerName
    )
}