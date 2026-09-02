package io.github.chindeaone.collectiontracker.collections.prices

object NpcPrices {
    val collectionPrices: MutableMap<String, Int> = mutableMapOf()

    fun getNpcPrice(collection: String): Int = collectionPrices.getOrDefault(collection, -1)
}