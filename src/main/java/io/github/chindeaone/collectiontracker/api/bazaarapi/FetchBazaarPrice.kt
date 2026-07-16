package io.github.chindeaone.collectiontracker.api.bazaarapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices
import io.github.chindeaone.collectiontracker.utils.PlayerData
import org.apache.logging.log4j.LogManager

object FetchBazaarPrice {

    private val logger = LogManager.getLogger(FetchBazaarPrice::class.java)

    @JvmStatic
    fun fetchData(collection: String) {
        try {
            val response = requestHelper(collection)

            if (response.statusCode() != 200) {
                return
            }

            val jsonObject = JsonParser.parseString(response.body()).asJsonObject

            val collectionObject = jsonObject.entrySet().first().value.asJsonObject
            val entry = collectionObject.entrySet().first()

            val type = entry.key
            val data = entry.value.asJsonObject.toString()

            if (type == "gemstone") {
                GemstonePrices.setPrices(data)
            } else {
                BazaarCollectionsManager.setPricesAndRecipes(data, type)
            }

            CollectionsManager.collectionType = type
            logger.info("[SCT]: Successfully fetched bazaar price for collection '{}'", collection)
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching bazaar price for collection '{}': {}", collection, e.message)
        }
    }

    @JvmStatic
    fun fetchData(collections: List<String>) {
        try {
            val requestCollections = addGemstones(collections)

            var response = requestHelper(requestCollections.joinToString(", "))

            if (response.statusCode() == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
                TokenManager.fetchAndStoreToken()
                response = requestHelper(requestCollections.joinToString(", "))
            }

            if (response.statusCode() != 200) {
                return
            }

            val jsonObject = JsonParser.parseString(response.body()).asJsonObject

            for ((collectionId, wrapperElement) in jsonObject.entrySet()) {
                val typeWrapper = wrapperElement.asJsonObject

                val typeEntry = typeWrapper.entrySet().firstOrNull() ?: continue

                val type = typeEntry.key
                val data = typeEntry.value.asJsonObject.toString()

                if (type == "gemstone") {
                    GemstonePrices.setPrices(collectionId, data)
                } else {
                    BazaarCollectionsManager.setPricesAndRecipes(collectionId, data, type)
                }

                CollectionsManager.multiCollectionTypes[collectionId] = type
            }

            logger.info("[SCT]: Successfully fetched bazaar price for collection list '{}'", collections)
        } catch (e: Exception) {
            logger.error("[SCT]: Error fetching bazaar price for collections '{}': {}", collections, e.message)
        }
    }

    private fun requestHelper(collection: String) =
        ApiManager.request(
            "bazaar-prices",
            listOf(
                "Authorization" to "Bearer ${TokenManager.token}",
                "X-UUID" to PlayerData.playerUUID,
                "X-COLLECTION" to collection
            )
        )

    private fun addGemstones(collections: List<String>): List<String> {
        val result = collections.toMutableList()

        if ("gemstone" in result) {
            result.remove("gemstone")
            result += listOf(
                "ruby",
                "sapphire",
                "topaz",
                "amethyst",
                "jade",
                "jasper",
                "amber",
                "opal",
                "aquamarine",
                "peridot",
                "citrine",
                "onyx"
            )
        }

        return result
    }
}