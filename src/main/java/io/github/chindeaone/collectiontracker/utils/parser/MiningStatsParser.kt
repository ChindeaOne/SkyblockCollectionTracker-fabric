package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser.lastDisplayedSpecificFortune
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser.lastDisplayedSpecificFortuneValue
import io.github.chindeaone.collectiontracker.utils.tab.StatsWidget
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping

object MiningStatsParser {

    var lastDisplayedSpecificFortune = ""
    var lastDisplayedSpecificFortuneValue = 0

    private var cachedLines: List<String> = emptyList()
    private val NON_DIGIT = Regex("[^0-9]+")

    fun onClientTick() {
        if (!HypixelUtils.isInSkyblock) {
            clear()
            return
        }

        cachedLines = parse(StatsWidget.rawStats)
    }

    fun getLines(): List<String> = cachedLines

    fun clear() {
        cachedLines = emptyList()
        lastDisplayedSpecificFortuneValue = 0
        lastDisplayedSpecificFortune = ""
    }

    private fun parse(raw: List<String>): List<String> {
        val formatted = mutableListOf<String>()
        if (raw.isEmpty()) return formatted

        if (ConfigAccess.isMiningStatsOverlayEnabled() && ConfigAccess.isMiningStatsOverlayInMiningIslandsOnly() && !IslandTracker.isMiningIsland()) {
            return formatted
        }

        val ctx = MiningContext(BlockWatcher.miningBlockType)

        // stat map
        val statsMap = mapOf(
            "Mining Spread" to ctx.miningSpread,
            "Gemstone Spread" to ctx.gemstoneSpread,
            "Pristine" to ctx.pristine,
            "Mining Wisdom" to ctx.wisdom,
            "Cold Resistance" to ctx.cold,
            "Heat Resistance" to ctx.heat,
            "Breaking Power" to ctx.breakingPower
        )

        for (line in raw) {
            when {
                line.contains("Mining Speed") -> addMiningSpeedPerks(line, ctx)
                line.contains("Mining Fortune") ||
                        line.contains("Ore Fortune") ||
                        line.contains("Gemstone Fortune") ||
                        line.contains("Dwarven Metal Fortune") ||
                        line.contains("Block Fortune") -> processFortuneLine(line, ctx)
                else -> {
                    statsMap.forEach { (key, stat) ->
                        if (line.contains(key)) {
                            stat.parse(line)
                        }
                    }
                }
            }
        }

        formatted.addIfNotEmpty(ctx.formatTotalSpeed())
        formatted.addIfNotEmpty(ctx.formatTotalFortune())

        if (ctx.isGemstone) {
            formatted.addIfNotEmpty(ctx.gemstoneSpread.format())
            formatted.addIfNotEmpty(ctx.pristine.format())
        } else {
            formatted.addIfNotEmpty(ctx.miningSpread.format())
        }

        formatted.addIfNotEmpty(ctx.wisdom.format())
        formatted.addIfNotEmpty(ctx.cold.format())
        formatted.addIfNotEmpty(ctx.heat.format())
        formatted.addIfNotEmpty(ctx.breakingPower.format())

        return formatted
    }

    private fun extractFortune(line: String): Int {
        try {
            val digits = line.replace(NON_DIGIT, "")
            return if (digits.isEmpty()) 0 else digits.toInt()
        } catch (_: NumberFormatException) {
            return 0
        }
    }

    private fun processFortuneLine(line: String, ctx: MiningContext) {
        val value = extractFortune(line)

        if (line.contains("Mining Fortune")) {
            ctx.globalFortune = value
            return
        }

        if (!ctx.shouldShowSpecificFortune()) return

        val match = when (ctx.blockType) {
            "dwarven_metals" -> line.contains("Dwarven Metal Fortune")
            "pure_ores", "ores" -> line.contains("Ore Fortune")
            "gemstones" -> line.contains("Gemstone Fortune")
            "blocks" -> line.contains("Block Fortune")
            else -> false
        }

        if (match) {
            ctx.specificFortune = value
            ctx.specificFortuneName = ctx.getFortuneLabel()

            // Update last displayed specific fortune
            lastDisplayedSpecificFortune = ctx.specificFortuneName
            lastDisplayedSpecificFortuneValue = ctx.specificFortune
        }
    }
    private fun addMiningSpeedPerks(line: String, ctx: MiningContext) {
        val value = extractMiningSpeed(line)

        val professional = ConfigAccess.getProfessionalMS()
        val strongArm = ConfigAccess.getStrongArmMS()

        val total = when (ctx.blockType) {
            "dwarven_metals" -> value + strongArm
            "gemstones" -> value + professional
            else -> value
        }

        ctx.speed.value = total.toString()
    }

    private fun extractMiningSpeed(line: String): Int {
        try {
            val digits = line.replace(NON_DIGIT, "")
            return if (digits.isEmpty()) 0 else digits.toInt()
        } catch (_: NumberFormatException) {
            return 0
        }
    }

    private fun MutableList<String>.addIfNotEmpty(value: String) {
        if (value.isNotEmpty()) add(value)
    }
}

private class MiningContext(
    val blockType: String,
) {
    val island: String? = IslandTracker.currentMiningIsland
    val isGemstone: Boolean = "gemstones" == blockType
    val allowedIslands: Set<String>? = MiningMapping.miningBlocksPerArea[blockType]
    val allowSpecificFortune: Boolean = allowedIslands != null && island != null && allowedIslands.contains(island)

    var globalFortune = 0
    var specificFortune = 0
    var specificFortuneName = ""

    val speed = MiningStat("Mining Speed", "\uE015", "§6")
    val miningSpread = MiningStat("Mining Spread", "\uE016", "§e")
    val gemstoneSpread = MiningStat("Gemstone Spread", "\uE00F", "§e")
    val pristine = MiningStat("Pristine", "\uE01C", "§5")
    val wisdom = MiningStat("Mining Wisdom", "\u262F", "§3")
    val cold = MiningStat("Cold Resistance", "\uE006", "§b")
    val heat = MiningStat("Heat Resistance", "\u2668", "§c")
    val breakingPower = MiningStat("Breaking Power", "\u24C5", "§2")

    fun shouldShowSpecificFortune(): Boolean {
        return allowSpecificFortune
    }

    fun getFortuneLabel(): String {
        return when (blockType) {
            "dwarven_metals" -> "Dwarven Metal Fortune"
            "pure_ores", "ores" -> "Ore Fortune"
            "gemstones" -> "Gemstone Fortune"
            "blocks" -> "Block Fortune"
            else -> ""
        }
    }

    fun getFortuneColor(): String {
        return when (blockType) {
            "dwarven_metals" -> "§a" // Green
            "pure_ores", "ores" -> "§e" // Yellow
            "gemstones" -> "§d" // Light Purple
            "blocks" -> "§8" // Dark Gray
            else -> "" // No color
        }
    }

    fun formatTotalFortune(): String {
        val symbol = "\uE053"
        val color = getFortuneColor()
        val total = globalFortune + specificFortune
        if (total == 0) return ""

        val showDetailed = ConfigAccess.isShowDetailedMiningFortune()

        // Show specific fortune if available
        if (!specificFortuneName.isEmpty()) {
            var base = "§a$specificFortuneName: §6$symbol$total"
            if (showDetailed && specificFortune != 0) {
                base += " §7(§6$globalFortune §7+ $color$specificFortune§7)"
            }
            return base
        }
        // Fallback to last displayed specific fortune
        if (!lastDisplayedSpecificFortune.isEmpty()) {
            var base = "§a$lastDisplayedSpecificFortune: §6$symbol$total"
            if (showDetailed && lastDisplayedSpecificFortuneValue != 0) {
                base += " §7(§6$globalFortune §7+ $color$lastDisplayedSpecificFortuneValue§7)"
            }
            return base
        }

        // Fallback to mining fortune
        return "§aMining Fortune: §6$symbol$total"
    }

    fun formatTotalSpeed(): String {
        return speed.format()
    }
}

private class MiningStat(
    val label: String,
    var symbol: String,
    val valueColor: String
) {
    var value: String = "0"

    fun parse(line: String) {
        val content = line.substringAfter(label).trim().removePrefix(":")

        val match = Regex("""^(.*?)\s*([+-]?[\d,.]+)$""").find(content)

        if (match != null) {
            val extractedSymbol = match.groupValues[1].trim()
            if (extractedSymbol.isNotEmpty()) {
                this.symbol = extractedSymbol
            }
            this.value = match.groupValues[2]
        }
    }

    fun format(): String {
        if (value == "0") return ""
        // for cold
        if (label == "Cold Resistance" && !ScoreboardUtils.isColdStatRelevant()) return ""

        if (label == "Cold Resistance") {
            val coldValue = ScoreboardUtils.coldValue
            if (coldValue != 0) return "§a$label: $valueColor$symbol$value/$valueColor$coldValue"
        }
        // for heat
        if (label == "Heat Resistance" && !ScoreboardUtils.isHeatStatRelevant()) return ""

        if (label == "Heat Resistance") {
            val heatValue = ScoreboardUtils.heatValue
            if (heatValue != 0) return "§a$label: $valueColor$symbol$value/$valueColor$heatValue"
        }

        return "§a$label: $valueColor$symbol$value"
    }
}