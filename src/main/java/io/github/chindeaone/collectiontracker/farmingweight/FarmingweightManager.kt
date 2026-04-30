package io.github.chindeaone.collectiontracker.farmingweight

import com.google.gson.JsonArray
import com.google.gson.JsonParser

object FarmingweightManager {

    @Volatile
    var storage: FarmingweightStorage = FarmingweightStorage()
        private set

    @JvmStatic
    fun updateFarmingweight(data: String) {
        val root = JsonParser.parseString(data).asJsonObject

        storage = storage.copy(
            weight = root.get("weight")?.asFloat ?: 0f,
            rank = root.get("rank")?.asInt ?: 0
        )
    }

    @JvmStatic
    fun updateFarmingweightLb(data: String, isTop: Boolean) {
        val rootElem = JsonParser.parseString(data)
        val entries = when {
            rootElem.isJsonObject -> rootElem.asJsonObject.getAsJsonArray("entries") ?: JsonArray()
            rootElem.isJsonArray -> rootElem.asJsonArray
            else -> JsonArray()
        }

        val list = entries.mapNotNull { el ->
            if (!el.isJsonObject) return@mapNotNull null
            val obj = el.asJsonObject
            val name = obj.get("username")?.asString
                ?: obj.get("name")?.asString
                ?: return@mapNotNull null
            val weight = obj.get("weight")?.asFloat ?: 0f
            FarmingweightPlayer(name, weight)
        }

        storage = if (isTop) {
            storage.copy(leaderboard = list)
        } else {
            storage.copy(tempLeaderboard = list)
        }
    }

    @JvmStatic
    fun updateFarmingweightTopColors(data: String) {
        val obj = JsonParser.parseString(data).asJsonObject
        val colorMap = mutableMapOf<String, String>()

        for ((name, color) in obj.entrySet()) {
            colorMap[name.lowercase()] = color.asString
        }

        storage = storage.copy(
            topColors = colorMap
        )
    }
}
