package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import net.minecraft.network.chat.Component
import java.awt.Color

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

    // Inspired by Skyhanni's prefix gradient
    fun gradientText(prefix: String): Component {
        val firstBracket = prefix.indexOf('[')
        val lastBracket = prefix.lastIndexOf(']')
        val text = Component.empty()

        val colorBracket = Color(0, 170, 170) // Dark Aqua
        val colorStart = Color(255, 155, 0) // Orange
        val colorEnd = Color(255, 185, 0) // Lighter Orange

        for ((index, char) in prefix.withIndex()) {
            val color = when (index) {
                firstBracket, lastBracket -> colorBracket
                in (firstBracket + 1) until lastBracket -> {
                    val textLength = lastBracket - firstBracket - 1
                    val t = (index - (firstBracket + 1)).toDouble() / (textLength - 1).coerceAtLeast(1)
                    blendColors(colorStart, colorEnd, t)
                }
                else -> colorBracket
            }
            text.append(Component.literal(char.toString()).withStyle { it.withColor(color.rgb) })
        }
        return text
    }

    private fun blendColors(start: Color, end: Color, percent: Double): Color {
        val r = (start.red + (end.red - start.red) * percent).toInt()
        val g = (start.green + (end.green - start.green) * percent).toInt()
        val b = (start.blue + (end.blue - start.blue) * percent).toInt()
        return Color(r, g, b)
    }
}