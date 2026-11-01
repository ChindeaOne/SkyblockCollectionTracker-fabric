package io.github.chindeaone.collectiontracker.collections.prices

import com.google.gson.JsonParser

object BazaarPrices {

    enum class BazaarPriceType(private val displayName: String) {
        INSTANT_SELL("Instant Sell"),
        INSTANT_BUY("Instant Buy");

        override fun toString(): String = displayName
    }

    enum class BazaarType(private val displayName: String, val shortName: String) {
        ENCHANTED_VERSION("Enchanted", "ench"),
        SUPER_ENCHANTED_VERSION("Super Enchanted", "super ench");

        override fun toString(): String = displayName
    }

    var baseInstantBuy: Float = 0.0f
    var baseInstantSell: Float = 0.0f
    var enchantedInstantBuy: Float = 0.0f
    var enchantedInstantSell: Float = 0.0f
    var superEnchantedInstantBuy: Float = 0.0f
    var superEnchantedInstantSell: Float = 0.0f

    val multiBaseInstantBuy: MutableMap<String, Float> = mutableMapOf()
    val multiBaseInstantSell: MutableMap<String, Float> = mutableMapOf()
    val multiEnchantedInstantBuy: MutableMap<String, Float> = mutableMapOf()
    val multiEnchantedInstantSell: MutableMap<String, Float> = mutableMapOf()
    val multiSuperEnchantedInstantBuy: MutableMap<String, Float> = mutableMapOf()
    val multiSuperEnchantedInstantSell: MutableMap<String, Float> = mutableMapOf()

    fun setPrices(json: String, type: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()

        if (type == "normal") {
            // normal: { "ITEM_ID": { "INSTANT_BUY": 25498.465, "INSTANT_SELL": 24380.9 } }
            for ((_, value) in jsonObject.entrySet()) {
                val prices = value.getAsJsonObject()
                baseInstantBuy = prices.get("INSTANT_BUY").asFloat
                baseInstantSell = prices.get("INSTANT_SELL").asFloat
                break // Should only be one item
            }
        } else if (type == "enchanted") {
            // enchanted: { "INSTANT_SELL": { "ENCHANTED_GOLD": 486.3, "ENCHANTED_GOLD_BLOCK": 83085.2 }, "INSTANT_BUY": { ... } }
            val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
            val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

            val sellIterator = instantSell.entrySet().iterator()
            val buyIterator = instantBuy.entrySet().iterator()

            if (sellIterator.hasNext()) {
                enchantedInstantSell = sellIterator.next().value.asFloat
            }
            if (buyIterator.hasNext()) {
                enchantedInstantBuy = buyIterator.next().value.asFloat
            }

            if (sellIterator.hasNext()) {
                superEnchantedInstantSell = sellIterator.next().value.asFloat
            }
            if (buyIterator.hasNext()) {
                superEnchantedInstantBuy = buyIterator.next().value.asFloat
            }
        }
    }

    fun setPrices(collection: String, json: String, type: String) {
        val jsonObject = JsonParser.parseString(json).getAsJsonObject()

        if (type == "normal") {
            for ((_, value) in jsonObject.entrySet()) {
                val prices = value.getAsJsonObject()
                multiBaseInstantBuy[collection] = prices.get("INSTANT_BUY").asFloat
                multiBaseInstantSell[collection] = prices.get("INSTANT_SELL").asFloat
                break
            }
        } else if (type == "enchanted") {
            val instantSell = jsonObject.getAsJsonObject("INSTANT_SELL")
            val instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY")

            val sellIterator = instantSell.entrySet().iterator()
            val buyIterator = instantBuy.entrySet().iterator()

            if (sellIterator.hasNext()) {
                multiEnchantedInstantSell[collection] = sellIterator.next().value.asFloat
            }
            if (buyIterator.hasNext()) {
                multiEnchantedInstantBuy[collection] = buyIterator.next().value.asFloat
            }

            if (sellIterator.hasNext()) {
                multiSuperEnchantedInstantSell[collection] = sellIterator.next().value.asFloat
            }
            if (buyIterator.hasNext()) {
                multiSuperEnchantedInstantBuy[collection] = buyIterator.next().value.asFloat
            }
        }
    }

    fun resetPrices() {
        baseInstantBuy = 0.0f
        baseInstantSell = 0.0f
        enchantedInstantBuy = 0.0f
        enchantedInstantSell = 0.0f
        superEnchantedInstantBuy = 0.0f
        superEnchantedInstantSell = 0.0f

        multiBaseInstantBuy.clear()
        multiBaseInstantSell.clear()
        multiEnchantedInstantBuy.clear()
        multiEnchantedInstantSell.clear()
        multiSuperEnchantedInstantBuy.clear()
        multiSuperEnchantedInstantSell.clear()
    }
}