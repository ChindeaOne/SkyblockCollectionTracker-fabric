package io.github.chindeaone.collectiontracker.coleweight

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object ColeweightManager {

    @Volatile
    var storage: ColeweightStorage = ColeweightStorage()

    fun updateColeweight(data: String) {
        val root = JsonParser.parseString(data).asJsonObject

        storage = storage.copy(
            coleweight = if (root.has("coleweight")) root.get("coleweight").asFloat else 0f,
            rank = if (root.has("rank")) root.get("rank").asInt else 0,
            percentage = if (root.has("percentile")) root.get("percentile").asFloat else 0f,
            experience = parseDetail("experience", root),
            powder = parseDetail("powder", root),
            collection = parseDetail("collection", root),
            miscellaneous = parseDetail("miscellaneous", root)
        )
    }

    fun updateColeweightLb(data: String, isTop: Boolean) {
        val rootElem = JsonParser.parseString(data)
        val entries = when {
            rootElem.isJsonObject -> rootElem.asJsonObject.getAsJsonArray("entries") ?: JsonArray()
            rootElem.isJsonArray -> rootElem.asJsonArray
            else -> JsonArray()
        }

        val list = entries.map { el ->
            val obj = el.asJsonObject
            val name = obj.get("username")?.asString ?: ""
            val cw = obj.get("weight")?.asFloat ?: 0f
            ColeweightPlayer(name, cw)
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

    private fun parseDetail(name: String, root: JsonObject): Map<String, Float> {
        if (!root.has(name)) return emptyMap()

        val obj = root.getAsJsonObject(name)
        val entries = mutableMapOf<String, Float>()

        for ((k, v) in obj.entrySet()) {
            entries[k] = v.asFloat
        }
        return entries
    }

    fun updateColeweightTopColors(data: String) {
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