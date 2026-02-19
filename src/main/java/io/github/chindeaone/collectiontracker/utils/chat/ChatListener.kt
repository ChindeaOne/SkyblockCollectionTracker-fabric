package io.github.chindeaone.collectiontracker.utils.chat

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.TimerState
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ChatListener {

    // Skyhanni skill pattern
    private val SKILL_PATTERN = Regex("\\+(?<gains>[\\d,.]+)\\s+(?<skillName>.+?)\\s*\\((?<current>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)", RegexOption.IGNORE_CASE)
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)
    // Coleweight pattern
    private val NAME_PATTERN = Regex( """^(?:\[\d+]\s+)?(?:[^\w\s]\s+)?(?:(?:Guild|Party|Co-op)\s*>\s+|\[:v:]\s+)?(?:\[[^]]+]\s+)?([A-Za-z0-9_]{1,16})(?:\s+♲)?(?:\s+\[[^]]{1,6}])?\s*:\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val ABILITY_PATTERN = Regex("^You used your (.+?)(?: (Pickaxe|Axe) Ability)?!", RegexOption.IGNORE_CASE)
    private val CHANGE_ABILITY_PATTERN = Regex("^You selected (.+?) as your (Pickaxe|Axe)? ?Ability", RegexOption.IGNORE_CASE)
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

    private val pickaxeDuration = TimerState()
    private val pickaxeCooldown = TimerState()
    private val axeDuration = TimerState()
    private val axeCooldown = TimerState()

    @JvmStatic val finalCooldown: Double get() = pickaxeCooldown.remainingSeconds
    @JvmStatic val finalDuration: Double get() = pickaxeDuration.remainingSeconds
    @JvmStatic val finalAxeCooldown: Double get() = axeCooldown.remainingSeconds
    @JvmStatic val finalAxeDuration: Double get() = axeDuration.remainingSeconds

    fun onChatMessage(message: Component) {
        if (!HypixelUtils.isOnSkyblock) return

        val text = message.string
        val cleanText = text.removeColor()

        collectionListener(cleanText)
        petSummoned(text)
        abilityListener(cleanText)
        abilitySwapListener(cleanText)
    }

    private fun collectionListener(text: String) {
        if (!TrackingHandler.isTracking) return

        if (text.startsWith("[Sacks]")) {
            parseSacksMessage(text)
        }
    }

    @JvmStatic
    fun skillListener(text: String) {
        if (!SkillTrackingHandler.isTracking || !HypixelUtils.isOnSkyblock) return
        val cleanText = text.removeColor()

        val match = SKILL_PATTERN.find(cleanText)
        if (match != null) {
            parseSkillMessage(match)
        }
    }

    private fun abilityListener(text: String) {
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

    fun abilitySwapListener(text: String) {
        val match = CHANGE_ABILITY_PATTERN.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        val toolType = match.groupValues[2].lowercase()

        if (toolType == "axe") {
            ConfigHelper.setAxeAbilityName(abilityName)
        } else {
            ConfigHelper.setAbilityName(abilityName)
        }
    }

    private fun petSummoned(text: String) {
        val match = SUMMON_PATTERN.find(text) ?: return
        val petSegment = match.groupValues[1]

        val name = petSegment.replace(" ✦", "").trim()

        val level = if (AbilityUtils.lastPet?.name == name) AbilityUtils.lastPet!!.level else 100
        val rarity = if (AbilityUtils.lastPet?.name == name) AbilityUtils.lastPet!!.rarity else AbilityUtils.PetRarity.LEGENDARY

        AbilityUtils.updatePet(AbilityUtils.Pet(name = name, level = level, rarity = rarity, timestamp = System.currentTimeMillis(), isManual = true))
    }

    // Listen to Autopet swap messages
    @JvmStatic
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
            'd' -> AbilityUtils.PetRarity.MYTHIC
            else -> return
        }

        AbilityUtils.updatePet(AbilityUtils.Pet(name = name, level = level, rarity = rarity, timestamp = System.currentTimeMillis()))
    }

    private fun startAbilityTimeline(ability: String, snap: AbilityUtils.PickaxeAbilitySnapshot?) {
        var cotm = ConfigAccess.getCotmLevel()
        if (cotm >= 1) cotm = 1
        val hasBlueCheese = snap?.hasBlueCheesePart == true

        val baseCooldown = AbilityUtils.getBaseCooldown(ability, cotm, hasBlueCheese)
        val finalCooldownSec = AbilityUtils.calculateReduction(
            baseCooldown = baseCooldown,
            snap = snap,
            skyMallActive = isSkyMallPickaxeAbilityActive()
            )

        val durationMs = (AbilityUtils.getBaseDuration(ability, cotm, hasBlueCheese) * 1000).toLong()

        pickaxeDuration.start(durationMs)
        pickaxeCooldown.start((finalCooldownSec * 1000).toLong())

        ConfigHelper.setAbilityName(ability)
    }

    private fun startAxeAbilityTimeline(ability: String) {
        var cotf = ConfigAccess.getCotfLevel()
        if (cotf >= 1) cotf = 1

        val baseCooldown = AbilityUtils.getBaseAxeCooldown(ability, cotf)
        val finalCooldownSec = AbilityUtils.calculateAxeReduction(
            baseCooldown = baseCooldown
        )

        val durationMs = (AbilityUtils.getBaseAxeDuration(ability, cotf) * 1000).toLong()
        val cooldownMs = (finalCooldownSec * 1000).toLong()

        axeDuration.start(durationMs)
        axeCooldown.start(cooldownMs)

        ConfigHelper.setAxeAbilityName(ability)
    }

    private fun isSkyMallPickaxeAbilityActive(): Boolean {
        return isPickaxeAbility
    }

    @JvmStatic
    fun dailyPerksUpdate(message: Component): Boolean {
        if (!HypixelUtils.isOnSkyblock) return false

        val now = System.currentTimeMillis()

        if (ScoreboardUtils.timeLeft > 0) {
            nextBuffTime = now + ScoreboardUtils.timeLeft * 1000L
            ScoreboardUtils.timeLeft = 0
        }

        val text = message.string.removeColor()

        when {
            text.startsWith("MAYHEM!") -> {
                AbilityUtils.isMayhemCooldown = text.contains("Your Pickaxe Ability cooldown was reduced from your Mineshaft Mayhem perk!")
            }
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

    @JvmStatic
    fun coleweightHandle(message: Component): Component {
        if (!HypixelUtils.isOnSkyblock) return message
        if (!ConfigAccess.isColeweightRankingInChat()) return message

        val text = message.string.removeColor()

        if (ConfigAccess.isOnlyOnMiningIslands()) {
            if (!MiningMapping.miningIslands.contains(MiningStatsWidget.currentMiningIsland)) return message
        }

        val match = NAME_PATTERN.find(text)?: return message
        val playerName = match.groupValues[1]

        val storage = ColeweightManager.storage
        val leaderboardRank = storage.leaderboard.indexOfFirst { it.name.equals(playerName, ignoreCase = true) }

        if (leaderboardRank != -1) {
            val rank = leaderboardRank + 1
            if (rank > 1000) return message // Don't show ranks for players outside of top 1000
            val rankSuffix = ColeweightUtils.getCustomColor(rank, playerName.equals(PlayerData.playerName, ignoreCase = true))

            if (rankSuffix != Component.empty() && rankSuffix.string.isNotEmpty()) {
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
                        newComponent.append(Component.literal(" "))
                        newComponent.append(rankSuffix)
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

    @JvmStatic
    fun resetPickaxeAbilities() {
        pickaxeDuration.reset()
        pickaxeCooldown.reset()
    }
}