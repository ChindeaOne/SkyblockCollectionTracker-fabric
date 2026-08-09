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

@Suppress("unused")
enum class Colors(val colorCode: Char, val color: Int) {
    BLACK('0', 0xFF000000.toInt()),
    DARK_BLUE('1', 0xFF0000AA.toInt()),
    DARK_GREEN('2', 0xFF00AA00.toInt()),
    DARK_AQUA('3', 0xFF00AAAA.toInt()),
    DARK_RED('4', 0xFFAA0000.toInt()),
    DARK_PURPLE('5', 0xFFAA00AA.toInt()),
    GOLD('6', 0xFFFFAA00.toInt()),
    GRAY('7', 0xFFAAAAAA.toInt()),
    DARK_GRAY('8', 0xFF555555.toInt()),
    BLUE('9', 0xFF5555FF.toInt()),
    GREEN('a', 0xFF55FF55.toInt()),
    AQUA('b', 0xFF55FFFF.toInt()),
    RED('c', 0xFFFF5555.toInt()),
    LIGHT_PURPLE('d', 0xFFFF55FF.toInt()),
    YELLOW('e', 0xFFFFFF55.toInt()),
    WHITE('f', 0xFFFFFFFF.toInt());
}

object ColorUtils {
    const val CUSTOM_WHITE: Int = 0xFFCCD7E0.toInt()
    const val DUMMY_BG: Int = 0x80404040.toInt()
    const val SILVER_BLUE: Int = 0xFF7FB4DB.toInt()

    val GRADIENT_START_COLOR: Color = Color(255, 212, 71)
    val GRADIENT_END_COLOR: Color = Color(255, 159, 46)

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
    fun getPrefixComponent(): Component {
        return Component.empty()
            .append(Component.literal("[").withColor(Colors.DARK_AQUA.color))
            .append(Component.literal("SCT").withStyle { ChromaText.prefixStyle() })
            .append(Component.literal("]").withColor(Colors.DARK_AQUA.color))
            .append(Component.literal(" "))
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
            val colorInt = collectionColors[collection.lowercase()] ?: Colors.WHITE.color
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