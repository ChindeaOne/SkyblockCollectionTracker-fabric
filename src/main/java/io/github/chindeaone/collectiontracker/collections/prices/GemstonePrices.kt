package io.github.chindeaone.collectiontracker.collections.prices

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier

object GemstonePrices {
    var gemstoneInstantBuyPrices: MutableMap<String, Float> = mutableMapOf()
    var gemstoneInstantSellPrices: MutableMap<String, Float> = mutableMapOf()

    var multiGemstoneInstantBuyPrices: MutableMap<String, MutableMap<String, Float>> = mutableMapOf()
    var multiGemstoneInstantSellPrices: MutableMap<String, MutableMap<String, Float>> = mutableMapOf()

    fun setPrices(json: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()
        val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
        val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

        for ((key, value) in instantSell.entrySet()) {
            gemstoneInstantSellPrices[key] = value.asFloat
        }

        for ((key, value) in instantBuy.entrySet()) {
            gemstoneInstantBuyPrices[key] = value.asFloat
        }

        BazaarCollectionsManager.hasBazaarData = true
    }

    fun setPrices(collection: String, json: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()
        val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
        val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

        val sellPrices: MutableMap<String, Float> = mutableMapOf()
        for ((key, value) in instantSell.entrySet()) {
            sellPrices[key] = value.asFloat
        }
        multiGemstoneInstantSellPrices[collection] = sellPrices

        val buyPrices: MutableMap<String, Float> = mutableMapOf()
        for ((key, value) in instantBuy.entrySet()) {
            buyPrices[key] = value.asFloat
        }
        multiGemstoneInstantBuyPrices[collection] = buyPrices

        BazaarCollectionsManager.hasBazaarData = true
    }

    fun getInstantBuyPrice(gemstoneTier: GemstoneTier): Float = gemstoneInstantBuyPrices.getOrDefault(gemstoneTier.name, 0.0f)

    fun getInstantSellPrice(gemstoneTier: GemstoneTier): Float = gemstoneInstantSellPrices.getOrDefault(gemstoneTier.name, 0.0f)

    fun resetPrices() {
        gemstoneInstantBuyPrices.clear()
        gemstoneInstantSellPrices.clear()
        multiGemstoneInstantBuyPrices.clear()
        multiGemstoneInstantSellPrices.clear()
    }
}