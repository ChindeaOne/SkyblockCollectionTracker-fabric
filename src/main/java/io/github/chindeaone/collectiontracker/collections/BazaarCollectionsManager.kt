package io.github.chindeaone.collectiontracker.collections

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.setPrices
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices

object BazaarCollectionsManager {
    var hasBazaarData: Boolean = false

    val enchantedRecipe: MutableMap<String, Int> = mutableMapOf()
    val superEnchantedRecipe: MutableMap<String, Int> = mutableMapOf()

    val multiEnchantedRecipes: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    val multiSuperEnchantedRecipes: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    fun setPricesAndRecipes(json: String, type: String) {
        enchantedRecipe.clear()
        superEnchantedRecipe.clear()

        val jsonObject = JsonParser.parseString(json).getAsJsonObject()

        if (!jsonObject.has("prices") && !jsonObject.has("recipe")) {
            setPrices(jsonObject.toString(), type)
        } else {
            val prices = jsonObject.getAsJsonObject("prices")
            val recipe = jsonObject.getAsJsonObject("recipe")

            val iterator = recipe.entrySet().iterator()

            if (iterator.hasNext()) {
                val entry = iterator.next()
                enchantedRecipe[entry.key] = entry.value.asInt
            }

            if (iterator.hasNext()) {
                val entry = iterator.next()
                superEnchantedRecipe[entry.key] = entry.value.asInt
            }
            setPrices(prices.toString(), type)
        }
        hasBazaarData = true
    }

    fun setPricesAndRecipes(collection: String, json: String, type: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()

        if (jsonObject.has("prices") || jsonObject.has("recipe")) {
            val prices = jsonObject.getAsJsonObject("prices")
            val recipe = jsonObject.getAsJsonObject("recipe")

            val enchanted: MutableMap<String, Int> = mutableMapOf()
            val superEnchanted: MutableMap<String, Int> = mutableMapOf()

            val iterator = recipe.entrySet().iterator()
            if (iterator.hasNext()) {
                val entry = iterator.next()
                enchanted[entry.key] = entry.value.asInt
            }
            if (iterator.hasNext()) {
                val entry = iterator.next()
                superEnchanted[entry.key] = entry.value.asInt
            }

            multiEnchantedRecipes[collection] = enchanted
            multiSuperEnchantedRecipes[collection] = superEnchanted
            setPrices(collection, prices.toString(), type)
        } else {
            setPrices(collection, jsonObject.toString(), type)
        }
        hasBazaarData = true
    }

    fun resetBazaarData() {
        hasBazaarData = false
        enchantedRecipe.clear()
        superEnchantedRecipe.clear()
        multiEnchantedRecipes.clear()
        multiSuperEnchantedRecipes.clear()
        BazaarPrices.resetPrices()
        GemstonePrices.resetPrices()
    }
}