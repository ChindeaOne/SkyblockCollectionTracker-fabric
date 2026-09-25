package io.github.chindeaone.collectiontracker.farmingweight

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.collections.component1
import kotlin.collections.component2

object FarmingweightManager {

    @Volatile
    var storage: FarmingweightStorage = FarmingweightStorage()

    @Volatile
    var loadedPlayer: String? = null

    fun updateFarmingweightRank(data: String) {
        val root = JsonParser.parseString(data).asJsonObject

        storage = storage.copy(
            weight = root.get("weight")?.asFloat ?: 0f,
            rank = root.get("rank")?.asInt ?: 0
        )
    }

    fun updateFarmingweightData(data: String, playerName: String) {
        val root = JsonParser.parseString(data).asJsonObject

        storage = storage.copy(
            weight = root.get("weight")?.asFloat ?: 0f,
            rank = root.get("rank")?.asInt ?: 0,
            cropWeight = parseMap("cropWeight", root),
            bonusWeight = parseMap("bonusWeight", root)
        )

        loadedPlayer = playerName
    }

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
            val name = obj.get("username")?.asString ?: ""
            val weight = obj.get("weight")?.asFloat ?: 0f
            FarmingweightPlayer(name, weight)
        }

        storage = if (isTop) {
            storage.copy(
                leaderboard = list,
                leaderboardRanks = list.withIndex().associate {
                    it.value.name.lowercase() to (it.index + 1)
                }
            )
        } else {
            storage.copy(tempLeaderboard = list)
        }
    }

    private fun parseMap(name: String, root: JsonObject): Map<String, Float> {
        if (!root.has(name)) return emptyMap()

        val obj = root.getAsJsonObject(name)
        val entries = mutableMapOf<String, Float>()

        for ((k, v) in obj.entrySet()) {
            entries[k] = v.asFloat
        }
        return entries
    }

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
