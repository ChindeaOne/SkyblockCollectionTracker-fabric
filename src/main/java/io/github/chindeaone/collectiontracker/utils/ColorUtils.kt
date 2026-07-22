package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaText
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import java.awt.Color

fun Int.toCWRankComponent(isMe: Boolean, playerName: String): Component =
    ColorUtils.customCWColorComponent(this, isMe, playerName)

fun Int.toFWRankComponent(isMe: Boolean, playerName: String): Component =
    ColorUtils.customFWColorComponent(this, isMe, playerName)

fun Color.toChromaColor(alpha: Int = this.alpha, chromaSpeedMillis: Int = 0): ChromaColour =
    ChromaColour.fromRGB(red, green, blue, chromaSpeedMillis, alpha)

object ColorUtils {
    const val CUSTOM_WHITE: Int = 0xFFCCD7E0.toInt()
    const val DUMMY_BG: Int = 0x80404040.toInt()
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val GREEN: Int = 0xFF55FF55.toInt()
    const val YELLOW: Int = 0xFFFFFF55.toInt()
    const val AQUA: Int = 0xFF55FFFF.toInt()
    const val GRAY: Int = 0xFFAAAAAA.toInt()
    const val RED: Int = 0xFFFF0000.toInt()
    const val DARK_GRAY: Int = 0xFFAAAAAA.toInt()
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

    fun customCWColorComponent(rank: Int, isMe: Boolean, playerName: String): Component {
        val color = getCWRankColor(rank, isMe, playerName)
        val text = "[⛏ $rank]"

        return Component.literal(text).withStyle(ChromaText.style(color))
    }

    fun customFWColorComponent(rank: Int, isMe: Boolean, playerName: String): Component {
        val color = getFWRankColor(rank, isMe, playerName)
        val text = "[🌾 $rank]"

        return Component.literal(text).withStyle(ChromaText.style(color))
    }

    fun getCWRankColor(rank: Int, isMe: Boolean, playerName: String): ChromaColour {
        if (isMe && ConfigAccess.isCustomCwColorEnabled()) {
            return ConfigAccess.getCustomCWColor()
        }

        if (!playerName.isEmpty()) {
            val hexString = ConfigHelper.getColeweightColor(playerName)
            if (hexString != null) {
                return Color.decode(hexString).toChromaColor()
            }
        }

        ColeweightManager.storage.topColors[playerName.lowercase()]?.let {
            return Color.decode(it).toChromaColor()
        }

        return when (rank) {
            1 -> Color(0, 191, 255).toChromaColor()
            2 -> Color(255, 215, 0).toChromaColor()
            3 -> Color(192, 192, 192).toChromaColor()
            in 4..25 -> Color(70, 130, 180).toChromaColor()
            in 26..100 -> Color(0, 255, 255).toChromaColor()
            in 101..250 -> Color(176, 196, 222).toChromaColor()
            in 251..500 -> Color(47, 79, 79).toChromaColor()
            in 501..1000 -> Color(112, 128, 144).toChromaColor()
            else -> Color.WHITE.toChromaColor()
        }
    }

    fun getFWRankColor(rank: Int, isMe: Boolean, playerName: String): ChromaColour {
        if (isMe && ConfigAccess.isCustomFWColorEnabled()) {
            return ConfigAccess.getCustomFWColor()
        }

        if (!playerName.isEmpty()) {
            val hexString = ConfigHelper.getFarmingweightColor(playerName)
            if (hexString != null) {
                return Color.decode(hexString).toChromaColor()
            }
        }

        FarmingweightManager.storage.topColors[playerName.lowercase()]?.let {
            return Color.decode(it).toChromaColor()
        }

        return when (rank) {
            1 -> Color(255, 215, 0).toChromaColor()
            2 -> Color(255, 140, 0).toChromaColor()
            3 -> Color(139, 69, 19).toChromaColor()
            in 4..25 -> Color(34, 139, 34).toChromaColor()
            in 26..100 -> Color(173, 255, 47).toChromaColor()
            in 101..250 -> Color(218, 165, 32).toChromaColor()
            in 251..500 -> Color(244, 164, 96).toChromaColor()
            in 501..1000 -> Color(107, 142, 35).toChromaColor()
            else -> Color.WHITE.toChromaColor()
        }
    }

    fun collToColor(collection: String): Component {
        return Component.literal(collection).withStyle {
            val colorInt = collectionColors[collection.lowercase()] ?: WHITE
            it.withColor(TextColor.fromRgb(colorInt))
        }
    }

    @JvmStatic
    fun coloredText(color: String): Component {
        return Component.literal(color).withStyle{
            it.withColor(TextColor.fromRgb(Color.decode(color).rgb))
        }
    }
}