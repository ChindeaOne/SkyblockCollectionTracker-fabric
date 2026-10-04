package io.github.chindeaone.collectiontracker.utils

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.customCWColor
import io.github.chindeaone.collectiontracker.config.customFWColor
import io.github.chindeaone.collectiontracker.config.enableCustomCWColor
import io.github.chindeaone.collectiontracker.config.enableCustomFWColor
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.utils.render.ChromaText
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

fun Int.toCWRankComponent(isMe: Boolean, playerName: String): Component =
    ColorUtils.customCWColorComponent(this, isMe, playerName)

fun Int.toFWRankComponent(isMe: Boolean, playerName: String): Component =
    ColorUtils.customFWColorComponent(this, isMe, playerName)

fun Int.toChromaColor(alpha: Int = 255, chromaSpeedMillis: Int = 0): ChromaColour =
    ChromaColour.fromRGB(this.red(), this.green(), this.blue(), chromaSpeedMillis, alpha)

fun String.toColor(): Component = ColorUtils.collToColor(this)

fun Int.red(): Int = (this shr 16) and 0xFF
fun Int.green(): Int = (this shr 8) and 0xFF
fun Int.blue(): Int = this and 0xFF

@Suppress("unused")
enum class Colors(private val colorCode: Char, val color: Int) {
    BLACK('0', 0x000000),
    DARK_BLUE('1', 0x0000AA),
    DARK_GREEN('2', 0x00AA00),
    DARK_AQUA('3', 0x00AAAA),
    DARK_RED('4', 0xAA0000),
    DARK_PURPLE('5', 0xAA00AA),
    GOLD('6', 0xFFAA00),
    GRAY('7', 0xAAAAAA),
    DARK_GRAY('8', 0x555555),
    BLUE('9', 0x5555FF),
    GREEN('a', 0x55FF55),
    AQUA('b', 0x55FFFF),
    RED('c', 0xFF5555),
    LIGHT_PURPLE('d', 0xFF55FF),
    YELLOW('e', 0xFFFF55),
    WHITE('f', 0xFFFFFF);
}

@Suppress("unused")
enum class Formatting(private val code: Char, val format: ChatFormatting) {
    OBFUSCATED('k', ChatFormatting.OBFUSCATED),
    BOLD('l', ChatFormatting.BOLD),
    STRIKETHROUGH('m', ChatFormatting.STRIKETHROUGH),
    UNDERLINE('n', ChatFormatting.UNDERLINE),
    ITALIC('o', ChatFormatting.ITALIC),
    RESET('r', ChatFormatting.RESET);
}

enum class ScreenColors(val color: Int) {
    SCREEN_BG(0xCC080808.toInt()),

    BUTTON(0x444444),
    BUTTON_HOVER(0x777777),

    DROPDOWN_HOVER(0x303030),
    DROPDOWN(0xEE181818.toInt()),

    PANEL_BG(0xEE101010.toInt()),
    PANEL_BORDER(0x555555),
    PANEL_HEADER(0x333333),
    
    COLEWEIGHT_WIDGET_BG(0x2E6F7E),
    FARMINGWEIGHT_WIDGET_BG(0x3B6E38),

    COLEWEIGHT_TEXT(0x508FBA),
    FARMINGWEIGHT_TEXT(0x659446),

    WIDGET_TITLE_TEXT(0xE8F1F5),
    WIDGET_VALUE_TEXT(0xFFD166),
}

object ColorUtils {
    const val OVERLAY_VALUE_COLOR: Int = 0xCCD7E0
    const val DUMMY_BG: Int = 0x80404040.toInt()
    const val TOOLTIP_BG: Int = 0x90000000.toInt()
    
    const val COLEWEIGHT: Int = 0x7FB4DB
    const val FARMINGWEIGHT: Int = 0x58CF30

    const val GRADIENT_START_COLOR: Int = 0xFFD447
    const val GRADIENT_END_COLOR: Int = 0xFF9F2E

    @Volatile
    var skillColors: Map<String, Int> = emptyMap()
    @Volatile
    var collectionColors: Map<String, Int> = emptyMap()

    fun setupColors(json: JsonObject) {
        parseColorMap(json, "skills")?.let { values ->
            skillColors = values
        }

        parseColorMap(json, "collections")?.let { values ->
            collectionColors = values
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
        if (isMe && enableCustomCWColor) return customCWColor

        if (!playerName.isEmpty()) {
            val hexString = ConfigHelper.getColeweightColor(playerName)
            if (hexString != null) {
                return parseHexColor(hexString).toChromaColor()
            }
        }

        ColeweightManager.storage.topColors[playerName.lowercase()]?.let {
            return parseHexColor(it).toChromaColor()
        }

        return when (rank) {
            1 -> 0x00BFFF
            2 -> 0xFFD700
            3 -> 0xC0C0C0
            in 4..25 -> 0x4682B4
            in 26..100 -> 0x00FFFF
            in 101..250 -> 0xB0C4DE
            in 251..500 -> 0x2F4F4F
            in 501..1000 -> 0x708090
            else -> 0xFFFFFF
        }.toChromaColor()
    }

    fun getFWRankColor(rank: Int, isMe: Boolean, playerName: String): ChromaColour {
        if (isMe && enableCustomFWColor) {
            return customFWColor
        }

        if (!playerName.isEmpty()) {
            val hexString = ConfigHelper.getFarmingweightColor(playerName)
            if (hexString != null) {
                return parseHexColor(hexString).toChromaColor()
            }
        }

        FarmingweightManager.storage.topColors[playerName.lowercase()]?.let {
            return parseHexColor(it).toChromaColor()
        }

        return when (rank) {
            1 -> 0xFFD700
            2 -> 0xFF8C00
            3 -> 0x8B4513
            in 4..25 -> 0x228B22
            in 26..100 -> 0xADFF2F
            in 101..250 -> 0xDAA520
            in 251..500 -> 0xF4A460
            in 501..1000 -> 0x6B8E23
            else -> Colors.WHITE.color
        }.toChromaColor()
    }

    fun parseHexColor(hex: String): Int = hex.removePrefix("#").toIntOrNull(16) ?: Colors.WHITE.color

    fun collToColor(collection: String): Component = Component.literal(collection).withColor(collectionColors[collection.lowercase()] ?: Colors.WHITE.color)

    fun coloredText(color: String): Component = Component.literal(color).withColor(parseHexColor(color))
}