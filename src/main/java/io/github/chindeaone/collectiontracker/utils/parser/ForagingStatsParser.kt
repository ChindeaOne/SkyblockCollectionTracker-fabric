package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowDetailedForagingFortune
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser.lastDisplayedBeaconFortuneValue
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser.lastDisplayedFortuneColor
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser.lastDisplayedSpecificFortune
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser.lastDisplayedSpecificFortuneValue
import io.github.chindeaone.collectiontracker.utils.tab.StatsWidget
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.isInPark
import java.util.regex.Pattern

object ForagingStatsParser {
    var lastDisplayedSpecificFortune = ""
    var lastDisplayedSpecificFortuneValue = 0
    var lastDisplayedBeaconFortuneValue = 0
    var lastDisplayedFortuneColor = "§6"

    private var cachedLines: List<String> = emptyList()
    private val NON_DIGIT = Regex("[^0-9]+")

    fun onClientTick() {
        if (!HypixelUtils.isInSkyblock) {
            clear()
            return
        }

        cachedLines = parse(StatsWidget.rawStats, StatsWidget.rawBeaconStats, StatsWidget.rawStarbornTempleStats)
    }

    fun getLines(): List<String> = cachedLines

    fun clear() {
        cachedLines = emptyList()
        lastDisplayedFortuneColor = "§6"
        lastDisplayedBeaconFortuneValue = 0
        lastDisplayedSpecificFortuneValue = 0
        lastDisplayedSpecificFortune = ""
    }

    fun parse(raw: List<String>, rawBeacon: List<String>, rawStarbornTemple: String): List<String> {
        val formatted = mutableListOf<String>()
        if (raw.isEmpty()) return formatted

        if (ConfigAccess.isForagingStatsOverlayEnabled() && ConfigAccess.isForagingStatsOverlayInForagingIslandsOnly() && !IslandTracker.isForagingIsland()) {
            return formatted
        }

        val ctx = ForagingContext(BlockWatcher.foragingBlockType)

        // stat map
        val statMap = mapOf(
            "Sweep" to ctx.sweep,
            "Foraging Wisdom" to ctx.wisdom,
            "Timber" to ctx.timber
        )

        for (line in raw) {
            when {
                line.contains("Foraging Fortune") ||
                        line.contains("Fig Fortune") ||
                        line.contains("Mangrove Fortune") ||
                        line.contains("Helix Fortune") -> processFortuneLine(line, ctx, false)
                else -> {
                    statMap.forEach { (key, stat) ->
                        if (line.contains(key)) {
                            stat.parse(line)
                        }
                    }
                }
            }
        }

        if (rawBeacon.isNotEmpty()) {
            for (line in rawBeacon) {
                val trimmed = line.trim { it <= ' ' }
                if (trimmed.isEmpty()) continue

                processFortuneLine(trimmed, ctx, true)
            }
        }

        if (rawStarbornTemple.isNotBlank()) {
            processFortuneLine("Starborn $rawStarbornTemple", ctx, false)
        }

        formatted.addIfNotEmpty(ctx.sweep.format())
        formatted.addIfNotEmpty(ctx.formatTotalFortune())
        formatted.addIfNotEmpty(ctx.timber.format())
        formatted.addIfNotEmpty(ctx.wisdom.format())

        return formatted
    }

    private fun processFortuneLine(line: String, ctx: ForagingContext, isBeacon: Boolean) {
        val value = extractFortune(line)

        when {
            (line.contains("Stacks") || line.contains("Stacks")) && isBeacon -> ctx.beaconStacks = line.replace(".*: ".toRegex(), "")
            line.contains("Starborn") -> ctx.starbornTempleFortune = value
            line.contains("Foraging Fortune") && !line.contains("Starborn") -> ctx.globalFortune = value
            line.contains("Fig Fortune") -> {
                if (isBeacon) ctx.beaconFigFortune = value
                else ctx.figFortune = value
            }
            line.contains("Mangrove Fortune") -> {
                if (isBeacon) ctx.beaconMangroveFortune = value
                else ctx.mangroveFortune = value
            }
            line.contains("Helix Fortune") -> {
                if (isBeacon) ctx.beaconHelixFortune = value
                else ctx.helixFortune = value
            }
        }

        when (ctx.blockType) {
            "fig" -> {
                lastDisplayedSpecificFortune = "Fig Fortune"
                lastDisplayedSpecificFortuneValue = ctx.figFortune
                lastDisplayedBeaconFortuneValue = ctx.beaconFigFortune
                lastDisplayedFortuneColor = "§e"
            }
            "mangrove" -> {
                lastDisplayedSpecificFortune = "Mangrove Fortune"
                lastDisplayedSpecificFortuneValue = ctx.mangroveFortune
                lastDisplayedBeaconFortuneValue = ctx.beaconMangroveFortune
                lastDisplayedFortuneColor = "§c"
            }
            "helix" -> {
                lastDisplayedSpecificFortune = "Helix Fortune"
                lastDisplayedSpecificFortuneValue = ctx.helixFortune
                lastDisplayedBeaconFortuneValue = ctx.beaconHelixFortune
                lastDisplayedFortuneColor = "§b"
            }
        }
    }

    private fun extractFortune(line: String): Int {
        var line = line
        try {
            val comma = line.indexOf('.')
            if (comma != -1) {
                line = line.substring(0, comma)
            }

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

private class ForagingContext(
    val blockType: String
) {
    var globalFortune: Int = 0
    var figFortune: Int = 0
    var mangroveFortune: Int = 0
    var beaconFigFortune: Int = 0
    var beaconMangroveFortune: Int = 0
    var helixFortune: Int = 0
    var beaconHelixFortune: Int = 0
    var starbornTempleFortune: Int = 0
    var beaconStacks: String = ""

    var sweep: ForagingStat = ForagingStat("Sweep", "\uE023", "§2")
    var wisdom: ForagingStat = ForagingStat("Foraging Wisdom", "\u262F", "§3")
    var timber: ForagingStat = ForagingStat("Timber", "\uE02E", "§4")

    fun formatTotalFortune(): String {
        val symbol = "\uE054"
        var baseSpecific = 0
        var beaconSpecific = 0
        var specificFortuneName = ""
        var specificColor = "§6"

        when (blockType) {
            "fig" -> {
                baseSpecific = figFortune
                beaconSpecific = beaconFigFortune
                specificFortuneName = "Fig Fortune"
                specificColor = "§e"
            }
            "mangrove" -> {
                baseSpecific = mangroveFortune
                beaconSpecific = beaconMangroveFortune
                specificFortuneName = "Mangrove Fortune"
                specificColor = "§c"
            }
            "helix" -> {
                baseSpecific = helixFortune
                beaconSpecific = beaconHelixFortune
                specificFortuneName = "Helix Fortune"
                specificColor = "§b"
            }
        }

        val totalGlobal = globalFortune + starbornTempleFortune
        val total = totalGlobal + (if (specificFortuneName.isEmpty())
            (lastDisplayedSpecificFortuneValue + lastDisplayedBeaconFortuneValue)
        else
            (baseSpecific + beaconSpecific))

        if (total == 0) return ""
        val stackDisplay = if (beaconStacks.isEmpty()) "" else " §3($beaconStacks)"

        // If player isn't on Moonglade Marsh, use global fortune only
        if (isInPark) {
            return "§aForaging Fortune: §6$symbol$globalFortune"
        }

        val showDetailed = isShowDetailedForagingFortune()

        if (!specificFortuneName.isEmpty()) {
            var base = "§a$specificFortuneName: §6$symbol$total$stackDisplay"
            if (showDetailed) {
                base += " §7(§6$globalFortune §7+ $specificColor$baseSpecific"
                if (beaconSpecific > 0) base += " §7+ §3$beaconSpecific"

                if (starbornTempleFortune > 0) {
                    base += " §7+ §9$starbornTempleFortune"
                }

                base += "§7)"
            }
            return base
        }

        if (!lastDisplayedSpecificFortune.isEmpty()) {
            var base = "§a$lastDisplayedSpecificFortune: §6$symbol$total$stackDisplay"
            if (showDetailed) {
                base += " §7(§6$globalFortune §7+ $lastDisplayedFortuneColor$lastDisplayedSpecificFortuneValue"
                if (lastDisplayedBeaconFortuneValue > 0) base += " §7+ §3$lastDisplayedBeaconFortuneValue"

                if (starbornTempleFortune > 0) {
                    base += " §7+ §9$starbornTempleFortune"
                }

                base += "§7)"
            }
            return base
        }

        return "§aForaging Fortune: §6$symbol$totalGlobal"
    }
}

private class ForagingStat(
    var label: String,
    var symbol: String,
    var valueColor: String
) {
    var value: String = "0"

    fun parse(line: String) {
        val symbolMatcher = Pattern.compile("(\\D)\\d").matcher(line)
        if (symbolMatcher.find()) {
            this.symbol = symbolMatcher.group(1).trim { it <= ' ' }
        }
        this.value = line.replace((".*" + Pattern.quote(this.symbol)).toRegex(), "").trim { it <= ' ' }
    }

    fun format(): String {
        if (value == "0") return ""

        return "§a$label: $valueColor$symbol$value"
    }
}
