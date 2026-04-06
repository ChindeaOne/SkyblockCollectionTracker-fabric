package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import java.awt.Color

fun Int.toRankComponent(isMe: Boolean, playerName: String): Component = ColorUtils.customColorComponent(this, isMe, playerName)

object ColorUtils {
    const val CUSTOM_WHITE: Int = 0xFFCCD7E0.toInt()
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val GREEN: Int = 0xFF55FF55.toInt()
    const val YELLOW: Int = 0xFFFFFF55.toInt()
    const val AQUA: Int = 0xFF55FFFF.toInt()
    const val GRAY: Int = 0xFFAAAAAA.toInt()
    const val SILVER_BLUE: Int = 0xFF7FB4DB.toInt()

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

    fun customColorComponent(rank: Int, isMe: Boolean, playerName: String): Component {
        val color = getRankColor(rank, isMe, playerName)
        val text = "[⛏ #$rank]"

        return Component.literal(text).withStyle {
            it.withColor(TextColor.fromRgb(color.rgb))
        }
    }

    fun getRankColor(rank: Int, isMe: Boolean, playerName: String): Color {
        if (isMe && ConfigAccess.isCustomColorEnabled()) {
            return Color(ConfigAccess.getCustomColor().getEffectiveColourRGB())
        }

        if (!playerName.isEmpty()) {
            val hexString = ConfigHelper.getColeweightColor(playerName)
            if (hexString != null) {
                return Color.decode(hexString)
            }
        }

        return when (rank) {
            1 -> Color.BLACK
            2 -> Color(170, 0, 0)
            3 -> Color(0, 170, 0)
            in 4..25 -> Color(255, 170, 0)
            in 26..100 -> Color(0, 170, 170)
            in 101..250 -> Color(85, 255, 255)
            in 251..500 -> Color(85, 85, 255)
            in 501..1000 -> Color(170, 170, 170)
            else -> Color.WHITE
        }
    }

    fun collToColor(collection: String): Component {
        return Component.literal(collection).withStyle {
            val colorInt = collectionColors[collection.lowercase()] ?: WHITE
            it.withColor(TextColor.fromRgb(colorInt))
        }
    }

    fun coloredText(color: String): Component {
        return Component.literal(color).withStyle{
            it.withColor(TextColor.fromRgb(Color.decode(color).rgb))
        }
    }
}