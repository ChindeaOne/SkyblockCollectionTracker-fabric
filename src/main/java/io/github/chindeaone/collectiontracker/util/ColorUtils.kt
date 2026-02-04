package io.github.chindeaone.collectiontracker.util

import com.google.gson.JsonObject

object ColorUtils {
    val skillColors: MutableMap<String, Int> = HashMap()
    val collectionColors: MutableMap<String, Int> = HashMap()

    @JvmStatic
    fun setupColors(json: JsonObject) {
        parseColorMap(json, "skills")?.let { values ->
            synchronized(skillColors) {
                skillColors.clear()
                skillColors.putAll(values)
            }
        }

        parseColorMap(json, "collections")?.let { values ->
            synchronized(collectionColors) {
                collectionColors.clear()
                collectionColors.putAll(values)
            }
        }
    }

    private fun parseColorMap(json: JsonObject, key: String): Map<String, Int>? {
        val obj = json.get(key)?.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        val result = mutableMapOf<String, Int>()
        for ((k, elem) in obj.entrySet()) {
            val intVal = if (elem.isJsonPrimitive) {
                val prim = elem.asJsonPrimitive
                if (prim.isNumber) prim.asInt else prim.asString.toIntOrNull()
            } else null

            if (intVal != null) result[k] = intVal
        }
        return result
    }
}