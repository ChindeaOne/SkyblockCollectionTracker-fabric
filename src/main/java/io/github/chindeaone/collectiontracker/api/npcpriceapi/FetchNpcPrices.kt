package io.github.chindeaone.collectiontracker.api.npcpriceapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FetchNpcPrices {

    private val logger: Logger = LogManager.getLogger(FetchNpcPrices::class.java)
    @Volatile
    var hasNpcPrice: Boolean = false

    fun fetchPrices() {
        ApiManager.requestAsync("npc", listOf())
            .thenAccept { response ->
                if (response.statusCode() == 200) {
                    val prices = Gson().fromJson<Map<String, Int>>(
                        response.body(),
                        object : TypeToken<Map<String, Int>>() {}.type
                    )

                    NpcPrices.collectionPrices.putAll(prices)
                    hasNpcPrice = true
                    logger.info("[SCT]: Successfully received the npc prices.")

                } else {
                    logger.error("[SCT]: Failed to fetch NPC prices. HTTP {}", response.statusCode())
                }
            }
            .exceptionally { e ->
                logger.error("[SCT]: Error while receiving the npc prices", e)
                null
            }
    }
}