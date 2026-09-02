package io.github.chindeaone.collectiontracker.collections.prices

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager

object GemstonePrices {
    var gemstoneInstantBuyPrices: MutableMap<String, Float> = mutableMapOf()
    var gemstoneInstantSellPrices: MutableMap<String, Float> = mutableMapOf()
    var recipes: MutableMap<String, Int> = mutableMapOf()

    var multiGemstoneInstantBuyPrices: MutableMap<String, MutableMap<String, Float>> = mutableMapOf()
    var multiGemstoneInstantSellPrices: MutableMap<String, MutableMap<String, Float>> = mutableMapOf()
    var multiGemstoneRecipes: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    fun setPrices(json: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()
        val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
        val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

        gemstoneInstantSellPrices.clear()
        for (entry in instantSell.entrySet()) {
            gemstoneInstantSellPrices[entry.key] = entry.value.asFloat
        }

        gemstoneInstantBuyPrices.clear()
        for (entry in instantBuy.entrySet()) {
            gemstoneInstantBuyPrices[entry.key] = entry.value.asFloat
        }

        setRecipes()
        BazaarCollectionsManager.hasBazaarData = true
    }

    fun setPrices(collection: String, json: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()
        val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
        val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

        val sellPrices: MutableMap<String, Float> = mutableMapOf()
        for (entry in instantSell.entrySet()) {
            sellPrices[entry.key] = entry.value.asFloat
        }
        multiGemstoneInstantSellPrices[collection] = sellPrices

        val buyPrices: MutableMap<String, Float> = mutableMapOf()
        for (entry in instantBuy.entrySet()) {
            buyPrices[entry.key] = entry.value.asFloat
        }
        multiGemstoneInstantBuyPrices[collection] = buyPrices

        setRecipes(collection, sellPrices)
        BazaarCollectionsManager.hasBazaarData = true
    }

    private fun setRecipes() {
        recipes.clear()
        for (key in gemstoneInstantSellPrices.keys) {
            var amount = 0
            when {
                key.contains("ROUGH") -> amount = 1
                key.contains("FLAWED") -> amount = 80
                key.contains("FINE") -> amount = 80 * 80
                key.contains("FLAWLESS") -> amount = 80 * 80 * 80
                key.contains("PERFECT") -> amount = 5 * 80 * 80 * 80
            }
            recipes[key] = amount
        }
    }

    private fun setRecipes(collection: String, sellPrices: MutableMap<String, Float>) {
        val recipesPerColl: MutableMap<String, Int> = mutableMapOf()
        for (key in sellPrices.keys) {
            var amount = 0
            when {
                key.contains("ROUGH") -> amount = 1
                key.contains("FLAWED") -> amount = 80
                key.contains("FINE") -> amount = 80 * 80
                key.contains("FLAWLESS") -> amount = 80 * 80 * 80
                key.contains("PERFECT") -> amount = 5 * 80 * 80 * 80
            }

            if (amount != 0) recipesPerColl[key] = amount

        }
        multiGemstoneRecipes[collection] = recipesPerColl
    }

    fun getInstantBuyPrice(gemstoneVariant: String): Float = gemstoneInstantBuyPrices.getOrDefault(gemstoneVariant, 0.0f)

    fun getInstantSellPrice(gemstoneVariant: String): Float = gemstoneInstantSellPrices.getOrDefault(gemstoneVariant, 0.0f)

    fun resetPrices() {
        gemstoneInstantBuyPrices.clear()
        gemstoneInstantSellPrices.clear()
        recipes.clear()
        multiGemstoneInstantBuyPrices.clear()
        multiGemstoneInstantSellPrices.clear()
        multiGemstoneRecipes.clear()
    }
}