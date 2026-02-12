package io.github.chindeaone.collectiontracker.util.chat

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.util.ChatUtils
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import io.github.chindeaone.collectiontracker.util.ScoreboardUtils
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.util.AbilityUtils
import io.github.chindeaone.collectiontracker.util.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.util.tab.TabWidget
import io.github.chindeaone.collectiontracker.util.world.MiningMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player

object ChatListener {

    // Skyhanni skill pattern
    private val SKILL_PATTERN = Regex("\\+(?<gains>[\\d,.]+)\\s+(?<skillName>.+?)\\s*\\((?<current>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)", RegexOption.IGNORE_CASE)
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)
    // Coleweight pattern
    private val NAME_PATTERN = Regex( """^(?:\[\d+]\s+)?(?:⸕\s+)?(?:(?:Guild|Party|Co-op)\s*>\s+|\[:v:]\s+)?(?:\[[^]]+]\s+)?([A-Za-z0-9_]{1,16})(?:\s+♲)?(?:\s+\[[^]]{1,6}])?\s*:\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val ABILITY_PATTERN = Regex("^You used your (.+?)(?: (Pickaxe|Axe) Ability)?!", RegexOption.IGNORE_CASE)
    private val SUMMON_PATTERN = Regex("^You summoned your (.+?)!")
    var lastSkillValue = 0L

    @JvmStatic
    var currentSkyMallBuff = "§cUnknown"
    var isPickaxeAbility = false
    @JvmStatic
    var currentLotteryBuff = "§cUnknown"

    @JvmStatic
    var nextBuffTime: Long = 0
        private set

    private var expectingSkyMallBuff = false
    private var expectingLotteryBuff = false

    @JvmStatic
    @Volatile
    var finalCooldown: Long = 0L
        private set
    @JvmStatic
    @Volatile
    var finalDuration: Long = 0L
        private set
    @JvmStatic
    @Volatile
    var finalAxeCooldown: Long = 0L
        private set
    @JvmStatic
    @Volatile
    var finalAxeDuration: Long = 0L
        private set

    fun onChatMessage(message: Component) {
        if (!HypixelUtils.isOnSkyblock) return

        val text = message.string
        val cleanText = text.removeColor()

        trackingListener(cleanText)
        petSummoned(text)
        petSwapListener(text)
        abilityListener(cleanText)
    }

    private fun trackingListener(text: String) {
        if (!TrackingHandler.isTracking && !SkillTrackingHandler.isTracking) return

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

    @Suppress("SameReturnValue")
    fun checkHandItem(player: Player, hand: InteractionHand): InteractionResult {
        if (!HypixelUtils.isOnSkyblock) return InteractionResult.PASS

        val stack = player.getItemInHand(hand)
        if (stack.isEmpty) return InteractionResult.PASS

        val snap = AbilityItemParser.snapshot(stack)
        if (snap != null) AbilityUtils.update(snap)

        return InteractionResult.PASS
    }

    fun abilityListener(text: String) {
        val match = ABILITY_PATTERN.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        val toolType = match.groupValues[2].lowercase()

        if (toolType == "axe") {
            val axeSnap = AbilityUtils.recentOrNullAxe()
            if (axeSnap != null && axeSnap.hasAbility) {
                startAxeAbilityTimeline(abilityName)
            }
        } else {
            val pickSnap = AbilityUtils.recentOrNull()
            if (pickSnap != null && pickSnap.hasAbility) {
                startAbilityTimeline(abilityName, pickSnap)
            }
        }
    }

    private fun petSummoned(text: String) {
        val match = SUMMON_PATTERN.find(text) ?: return
        val petSegment = match.groupValues[1]

        val name = petSegment.replace(" ✦", "").trim()

        val level = if (AbilityUtils.lastPet?.name == name) AbilityUtils.lastPet!!.level else 100
        val rarity = if (AbilityUtils.lastPet?.name == name) AbilityUtils.lastPet!!.rarity else AbilityUtils.PetRarity.LEGENDARY

        AbilityUtils.updatePet(
            AbilityUtils.Pet(
                name = name,
                level = level,
                rarity = rarity,
                timestamp = System.currentTimeMillis(),
                isManual = true
            )
        )
    }

    // Listen to Autopet swap messages
    fun petSwapListener(text: String) {
        // Example: "Autopet equipped your [Lvl 100] §6Bal§r§7! §eVIEW RULE"
        val regex = Regex("§cAutopet §eequipped your §7\\[Lvl (\\d{1,3})] (.+?)! ")
        val match = regex.find(text) ?: return
        val level = match.groupValues[1].toIntOrNull() ?: return
        if (level !in 1..200) return
        val petSegment = match.groupValues[2]

        val name = petSegment
            .replace(Regex("§."), "")
            .replace(" ✦", "")
            .trim()

        // Extract first color code preceding the pet name
        val colorCodeMatch = Regex("§.").find(petSegment)
        val code = colorCodeMatch?.value?.getOrNull(1)
        val rarity = when (code) {
            'f' -> AbilityUtils.PetRarity.COMMON
            'a' -> AbilityUtils.PetRarity.UNCOMMON
            '9' -> AbilityUtils.PetRarity.RARE
            '5' -> AbilityUtils.PetRarity.EPIC
            '6' -> AbilityUtils.PetRarity.LEGENDARY
            else -> return
        }

        AbilityUtils.updatePet(AbilityUtils.Pet(name = name, level = level, rarity = rarity, timestamp = System.currentTimeMillis()))
    }

    private fun startAbilityTimeline(ability: String, snap: AbilityUtils.PickaxeAbilitySnapshot?) {
        var cotm = ConfigAccess.getCotmLevel()
        if (cotm >= 1) cotm = 1
        val hasBlueCheese = snap?.hasBlueCheesePart == true

        val baseCooldownSeconds = AbilityUtils.getBaseCooldown(ability, cotm, hasBlueCheese)
        val calculateFinalCooldown = AbilityUtils.calculateReduction(
            baseCooldown = baseCooldownSeconds,
            snap = snap,
            skyMallActive = isSkyMallPickaxeAbilityActive()
            )

        finalDuration = AbilityUtils.getBaseDuration(ability, cotm, hasBlueCheese).toLong() * 1000 + System.currentTimeMillis()
        finalCooldown = (calculateFinalCooldown * 1000).toLong() + System.currentTimeMillis()

        ConfigHelper.setAbilityName(ability)
    }

    private fun startAxeAbilityTimeline(ability: String) {
        var cotf = ConfigAccess.getCotfLevel()
        if (cotf >= 1) cotf = 1

        val baseCooldownSeconds = AbilityUtils.getBaseAxeCooldown(ability, cotf)
        val calculateFinalCooldown = AbilityUtils.calculateAxeReduction(
            baseCooldown = baseCooldownSeconds
        )

        finalAxeDuration = AbilityUtils.getBaseAxeDuration(ability, cotf).toLong() * 1000 + System.currentTimeMillis()
        finalAxeCooldown = (calculateFinalCooldown * 1000).toLong() + System.currentTimeMillis()

        ConfigHelper.setAxeAbilityName(ability)
    }

    private fun isSkyMallPickaxeAbilityActive(): Boolean {
        return isPickaxeAbility
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
                return ConfigAccess.isSkyMallEnabled()
            }
            text.contains("Your Lottery buff changed!") -> {
                expectingLotteryBuff = true
                return ConfigAccess.isLotteryEnabled()
            }
            text.startsWith("New buff: ") -> {
                val buffText = text.substringAfter("New buff: ").trim()
                // Set default 20 mins only when a new buff is sent in chat
                if (nextBuffTime - now < 10_000L) { // Allows chat checking 10 seconds before
                    nextBuffTime = now + 1_200_000
                }

                val compact = compactBuffs(buffText)
                if (expectingSkyMallBuff) {
                    isPickaxeAbility = "Pickaxe Ability" in text
                    currentSkyMallBuff = compact
                    ConfigHelper.setLastSkyMallPerk(compact)
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
                    ConfigHelper.setLastLotteryPerk(compact)
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