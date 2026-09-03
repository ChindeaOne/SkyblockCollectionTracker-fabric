package io.github.chindeaone.collectiontracker.api.eliteapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture

object EliteApiFetcher {

    private val logger = LogManager.getLogger(EliteApiFetcher::class.java)

    @Volatile
    var hasFarmingweightLb = false

    @Volatile
    var hasFarmingweightTopColors = false

    fun fetchFarmingweightData(playerName: String, uuid: String): CompletableFuture<String?> {
        return ApiManager.requestAsync("farmingweight", authHeaders(uuid, playerName))
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            ChatUtils.sendMessage("§cCouldn't find $playerName's Farming Weight.")
                            logger.warn("[SCT]: Empty response for {}", playerName)
                            null
                        } else body
                    }
                    429 -> {
                        ChatUtils.sendMessage("§cRate limit exceeded for $playerName's Farming Weight.")
                        logger.warn("[SCT]: Rate limit exceeded for {}", playerName)
                        null
                    }
                    else -> {
                        ChatUtils.sendMessage("§cError fetching Farming Weight for $playerName.")
                        logger.warn("[SCT]: Error fetching Farming Weight for {}: HTTP {}", playerName, response.statusCode())
                        null
                    }
                }
            }
            .exceptionally {
                logger.error("[SCT]: Error fetching Farming Weight.", it)
                null
            }
    }

    fun fetchFarmingweightLeaderboard(): CompletableFuture<String?> {
        return ApiManager.requestAsync("farmingweight/lb")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Farming Weight leaderboard.")
                            null
                        } else body
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Farming Weight leaderboard. HTTP {}", response.statusCode())
                        null
                    }
                }
            }.exceptionally {
                logger.error("[SCT]: Error fetching Farming Weight leaderboard.", it)
                null
            }
    }

    fun fetchFarmingweightLbTop1k() {
        ApiManager.requestAsync("farmingweight/top1k")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()

                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Farming Weight top 1k.")
                            null
                        } else body
                    } else -> {
                        logger.warn("[SCT]: Failed to fetch Farming Weight top 1k. HTTP {}", response.statusCode())
                        null
                    }
                }
            }
            .thenAccept { body ->
                if (body != null) {
                    FarmingweightManager.updateFarmingweightLb(body, true)
                }
            }.exceptionally { e ->
                logger.error("[SCT]: Error fetching Farming Weight leaderboard.", e)
                null
            }
    }

    fun setGlobalColor(playerName: String, uuid: String, color: String): CompletableFuture<Boolean> {
        return ApiManager.postAsync("farmingweight/color", authHeaders(uuid, playerName).apply {
            remove("X-NAME")
            put("X-COLOR", color)
        }).thenApply { response ->
            if (response.statusCode() == 200) {
                logger.info("[SCT]: Successfully set global Farming Weight color for {}", playerName)
                true
            } else {
                logger.warn("[SCT]: Failed to set global Farming Weight color for {}. HTTP {}", playerName, response.statusCode())
                false
            }
        }.exceptionally { e ->
            logger.error("[SCT]: Error setting Farming Weight color.", e)
            false
        }
    }

    fun fetchFarmingweightTopColors() {
        ApiManager.requestAsync("farmingweight/colors")
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()
                        if (body.isNullOrBlank()) {
                            logger.warn("[SCT]: Received empty response when fetching Farming Weight top colors.")
                            null
                        } else body
                    }
                    else -> {
                        logger.warn("[SCT]: Failed to fetch Farming Weight top colors. HTTP {}", response.statusCode())
                        null
                    }
                }
            }.thenAccept { body ->
                if (body != null) {
                    FarmingweightManager.updateFarmingweightTopColors(body)
                    hasFarmingweightTopColors = true
                    logger.info("[SCT]: Successfully fetched Farming Weight top colors.")
                }
            }.exceptionally { e ->
                logger.error("[SCT]: Error fetching Farming Weight top colors.", e)
                null
            }
    }

    fun fetchCollectionLeaderboard(collection: String): CompletableFuture<String?> {
        return ApiManager.requestAsync("collection/leaderboard/${collection.replace(" ", "-")}", authHeaders(PlayerData.playerUUID, PlayerData.playerName).apply{
            remove("X-NAME")
            put("X-CONTAINS-WIPED", if (ConfigAccess.isIncludeWipedProfilesEnabled()) "true" else "false")
        }).thenApply { response ->
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
            }.exceptionally { e ->
                logger.error("[SCT]: Error fetching leaderboard data.", e)
                null
            }
    }

    private fun authHeaders(uuid: String, playerName: String) = mutableMapOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to uuid,
        "X-NAME" to playerName
    )
}

