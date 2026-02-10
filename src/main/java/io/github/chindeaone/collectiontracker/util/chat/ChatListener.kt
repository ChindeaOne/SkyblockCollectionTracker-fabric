package io.github.chindeaone.collectiontracker.util.chat

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.util.ChatUtils
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import io.github.chindeaone.collectiontracker.util.ScoreboardUtils
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.util.tab.TabWidget
import io.github.chindeaone.collectiontracker.util.world.MiningMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ChatListener {

    // Skyhanni skill pattern
    private val SKILL_PATTERN = Regex("\\+(?<gains>[\\d,.]+)\\s+(?<skillName>.+?)\\s*\\((?<current>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)", RegexOption.IGNORE_CASE)
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)
    // Coleweight pattern
    private val NAME_PATTERN = Regex( """^(?:\[\d+]\s+)?(?:⸕\s+)?(?:(?:Guild|Party|Co-op)\s*>\s+|\[:v:]\s+)?(?:\[[^]]+]\s+)?([A-Za-z0-9_]{1,16})(?:\s+♲)?(?:\s+\[[^]]{1,6}])?\s*:\s*(.*)$""", RegexOption.IGNORE_CASE)

    var lastSkillValue = 0L

    @JvmStatic
    var currentSkyMallBuff = "§cUnknown"
        private set
    @JvmStatic
    var currentLotteryBuff = "§cUnknown"
        private set
    @JvmStatic
    var nextBuffTime: Long = 0
        private set

    private var expectingSkyMallBuff = false
    private var expectingLotteryBuff = false

    fun trackingHandle(message: Component) {
        if (!HypixelUtils.isOnSkyblock) return

        val text = message.string.removeColor()

        if (text.startsWith("[Sacks]")) {
            parseSacksMessage(text)
            return
        }

        // Check pattern for past max skill level
        val match = SKILL_PATTERN.find(text)
        if (match != null) {
            parseSkillMessage(match)
            return
        }
    }

    fun dailyPerksUpdate(message: Component): Boolean {
        if (!HypixelUtils.isOnSkyblock) return false

        val now = System.currentTimeMillis()

        if (ScoreboardUtils.timeLeft > 0) {
            nextBuffTime = now + ScoreboardUtils.timeLeft * 1000L
            ScoreboardUtils.timeLeft = 0
        }

        val text = message.string.removeColor()

        when {
            text.contains("Your Sky Mall buff changed!") -> {
                expectingSkyMallBuff = true
                return true
            }
            text.contains("Your Lottery buff changed!") -> {
                expectingLotteryBuff = true
                return true
            }
            text.startsWith("New buff: ") -> {
                println ("[SCT] New buff change message: '$text'")
                val buffText = text.substringAfter("New buff: ").trim()
                // Set default 20 mins only when a new buff is sent in chat
                if (nextBuffTime - now < 10_000L) { // Allows chat checking 10 seconds before
                    nextBuffTime = now + 1_200_000
                }

                val compact = compactBuffs(buffText)
                if (expectingSkyMallBuff) {
                    currentSkyMallBuff = compact
                    expectingSkyMallBuff = false
                    if (ConfigAccess.isDisableSkyMallChatMessages()) return true // Don't render Sky Mall buff in chat, but update the buffs in overlay

                    // Compact messages if overlay is enabled
                    if (ConfigAccess.isSkyMallEnabled()) {
                        ChatUtils.sendMessage("§eNew §bSky Mall §eBuff§r: $compact", prefix = true)
                        return true
                    }
                    return false
                }
                if (expectingLotteryBuff) {
                    currentLotteryBuff = compact
                    expectingLotteryBuff = false
                    if (ConfigAccess.isDisableLotteryChatMessages()) return true // Don't render Lottery buff in chat, but update the buffs in overlay

                    // Compact messages if overlay is enabled
                    if (ConfigAccess.isLotteryEnabled()) {
                        ChatUtils.sendMessage("§eNew §2Lottery §eBuff§r: $compact", prefix = true)
                        return true
                    }
                    return false
                }
            }
            // Don't render these messages at all
            text.startsWith("You can disable this messaging by toggling") -> return true
        }
        return false
    }

    private fun compactBuffs(message: String): String {
        val text = message.trim().removeSuffix(".")

        val numberRegex = Regex("[+-]?\\d+")
        val percentRegex = Regex("[+-]?\\d+%")
        val xRegex = Regex("\\d+x", RegexOption.IGNORE_CASE)

        return when {
            // Sky Mall buffs
            "Mining Speed" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num ⸕ Mining Speed"
            }
            "Mining Fortune" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num ☘ Mining Fortune"
            }
            "Titanium" in text -> {
                val x = xRegex.find(text)?.value ?: numberRegex.find(text)?.value?.let { "${it}x" }
                "§a$x §9Titanium"
            }
            "Pickaxe Ability" in text -> {
                val rawPct = percentRegex.find(text)?.value
                val pct = "${rawPct?.trimEnd('%')}%"
                "§a$pct §9Pickaxe Ability Cooldown"
            }
            "Powder" in text -> {
                val rawPct = percentRegex.find(text)?.value
                "§a$rawPct §9Powder"
            }
            "Goblins" in text -> {
                val x = xRegex.find(text)?.value ?: numberRegex.find(text)?.value?.let { "${it}x" }
                "§a$x §9Golden and Diamond Goblins"
            }

            // Lottery buffs
            "Fig" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num ☘ Fig Fortune"
            }
            "Mangrove" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num ☘ Mangrove Fortune"
            }
            "Sweep" in text -> {
                val rawPct = percentRegex.find(text)?.value
                val pct = "${rawPct?.trimEnd('%')}%"
                "§a$pct §2∮ Sweep"
            }
            else -> message // fallback to original text
        }
    }

    fun coleweightHandle(message: Component): Component {
        if (!HypixelUtils.isOnSkyblock) return message
        if (!ConfigAccess.isColeweightRankingInChat()) return message

        val text = message.string.removeColor()

        if (ConfigAccess.isOnlyOnMiningIslands()) {
            // Check tab area widget
            val areaWidget = TabWidget.AREA

            if (areaWidget.isPresent) {
                val currentIsland = areaWidget.lines.firstNotNullOfOrNull { line ->
                    MiningMapping.miningIslands.firstOrNull { name ->
                        line.contains(name, ignoreCase = true)
                    }
                }
                if (currentIsland == null) {
                    return message
                }
            }
        }
        val match = NAME_PATTERN.find(text)?: return message
        val playerName = match.groupValues[1]

        val storage = ColeweightManager.storage
        val leaderboardRank = storage.leaderboard.indexOfFirst { it.name.equals(playerName, ignoreCase = true) }

        if (leaderboardRank != -1) {
            val rank = leaderboardRank + 1
            val rankSuffix = ColeweightUtils.getRankColors(rank)

            if (rankSuffix.isNotEmpty()) {
                val newComponent = MutableComponent.create(message.contents).withStyle(message.style)
                var nameFound = false
                var hasRank = false

                for (sibling in message.siblings) {
                    if (hasRank) {
                        newComponent.append(sibling)
                        continue
                    }

                    val siblingText = sibling.string
                    // Look for player name in the siblings
                    if (!nameFound && siblingText.contains(playerName)) {
                        nameFound = true
                    }

                    // Check for colon to add the rank suffix
                    if (nameFound && siblingText.contains(":")) {
                        val colonIndex = siblingText.indexOf(":")
                        val before = siblingText.substring(0, colonIndex)
                        val after = siblingText.substring(colonIndex)

                        newComponent.append(Component.literal(before).withStyle(sibling.style))
                        newComponent.append(Component.literal(" $rankSuffix").withStyle(sibling.style))
                        newComponent.append(Component.literal(after).withStyle(sibling.style))
                        hasRank = true
                    } else {
                        newComponent.append(sibling)
                    }
                }
                return if (hasRank) newComponent else message
            }
        }
        return message
    }

    private fun parseSacksMessage(message: String) {
        if (!ConfigAccess.isSacksTrackingEnabled()) return

        val match = SACKS_PATTERN.find(message)
        if (match != null) {
            val itemsStr = match.groupValues[1].replace(",", "")
            val items = itemsStr.toIntOrNull() ?: 0

            SacksTrackingManager.onChatCollection(items)
        }
    }

    private fun parseSkillMessage(match: MatchResult) {
        val currentRaw = match.groups["current"]?.value ?: return
        val skillName = match.groups["skillName"]?.value?.trim() ?: return

        val current = currentRaw.replace(",", "").toLongOrNull() ?: return
        if (current != lastSkillValue) {
            SkillTrackingHandler.onSkillGain(current, skillName)
            lastSkillValue = current
        }
    }
}