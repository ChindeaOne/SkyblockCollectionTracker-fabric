package io.github.chindeaone.collectiontracker.coleweight

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.util.ChatUtils

object ColeweightManager {

    @Volatile
    var storage: ColeweightStorage = ColeweightStorage()
        private set

    @JvmStatic
    fun updateColeweight(data: String) {
        val root = JsonParser.parseString(data).asJsonObject

        if (root.has("error")) {
            // API returned an error, player not found
            ChatUtils.sendMessage("§cCouldn't find coleweight", true)
            return
        }

        storage = storage.copy(
            coleweight = root.get("coleweight").asFloat,
            rank = root.get("rank").asInt,
            percentage = root.get("percentile").asFloat,
            experience = parseDetail("experience", root),
            powder = parseDetail("powder", root),
            collection = parseDetail("collection", root),
            miscellaneous = parseDetail("miscellaneous", root)
        )
    }

    @JvmStatic
    fun updateColeweightLb(data: String, isTop: Boolean) {
        val arr = JsonParser.parseString(data).asJsonArray
        val list = arr.map { el ->
            val obj = el.asJsonObject
            val name = obj.get("name")?.asString ?: ""
            val cw = obj.get("coleweight")?.asFloat ?: 0f
            ColeweightPlayer(name, cw)
        }

        storage = if (isTop) {
            storage.copy(leaderboard = list)
        } else {
            storage.copy(tempLeaderboard = list)
        }
    }

    private fun parseDetail(name: String, root: JsonObject): Map<String, Float> {
        val obj = root.getAsJsonObject(name)
        val entries = mutableMapOf<String, Float>()

        for ((k, v) in obj.entrySet()) {
            entries[k] = v.asFloat
        }
        return entries
    }
}