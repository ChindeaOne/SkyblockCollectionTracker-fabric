package io.github.chindeaone.collectiontracker.utils.chat

import io.github.chindeaone.collectiontracker.api.skilltreeapi.FetchSkillTree
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightUtils
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.*
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import io.github.chindeaone.collectiontracker.utils.tab.CommissionWidget
import io.github.chindeaone.collectiontracker.utils.world.FarmingMapping
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

object ChatListener {

    enum class Patterns(pattern: String, vararg options: RegexOption) {
        // Skyhanni's skill pattern
        SKILL("""\+(?<gains>[\d,.]+)\s+(?<skillName>.+?)\s*\((?<current>[\d,]+)\s*/\s*(?<needed>[\d,]+)\)""", RegexOption.IGNORE_CASE),
        // Coleweight pattern
        ABILITY("""^You used your (.+?)(?: (Pickaxe|Axe) Ability)?!""", RegexOption.IGNORE_CASE),
        CHANGE_ABILITY("""^You selected (.+?) as your (Pickaxe|Axe)? ?Ability""", RegexOption.IGNORE_CASE),
        CONSUME("""^You consumed an? (.+?) and gained""", RegexOption.IGNORE_CASE),
        ON_COOLDOWN("""^Your (.+?) ability is on cooldown for (\d+)s.""", RegexOption.IGNORE_CASE),
        ATTRIBUTE("""^ATTRIBUTE\s+LEVEL\s+UP\s+Pickaxe\s+Cooldown.*?([\d]+|[IVX]+)\b$""", RegexOption.IGNORE_CASE),
        COMMISSION("""^(.+?)\s+Commission Complete!.*$""", RegexOption.IGNORE_CASE);
        val regex: Regex = Regex(pattern, options.toSet())

        fun find(input: CharSequence): MatchResult? = regex.find(input)
    }

    var lastSkillValue = 0L

    @JvmStatic var currentSkyMallBuff = "§cUnknown"
    var isPickaxeAbility = false
    @JvmStatic var currentLotteryBuff = "§cUnknown"
    @JvmStatic var currentBeekeeperBuff = "§cUnknown"

    @JvmStatic
    var nextBuffTime: Long = 0
    var abilityName: String? = null

    private var expectingSkyMallBuff = false
    private var expectingLotteryBuff = false
    private var expectingBeekeeper = false

    private val pickaxeDuration = TimerState()
    private val pickaxeCooldown = TimerState()
    private val axeDuration = TimerState()
    private val axeCooldown = TimerState()

    @JvmStatic val finalCooldown: Double get() = pickaxeCooldown.remainingSeconds
    @JvmStatic val finalDuration: Double get() = pickaxeDuration.remainingSeconds
    @JvmStatic val finalAxeCooldown: Double get() = axeCooldown.remainingSeconds
    @JvmStatic val finalAxeDuration: Double get() = axeDuration.remainingSeconds

    @JvmStatic var maxCooldown = 0.0
        private set
    @JvmStatic var maxDuration = 0.0
        private set
    @JvmStatic var maxAxeCooldown = 0.0
        private set
    @JvmStatic var maxAxeDuration = 0.0
        private set

    fun onChatMessage(message: Component) {
        if (!HypixelUtils.isOnSkyblock) return

        val text = message.string
        val cleanText = text.removeColor()

        if (cleanText.contains("ATTRIBUTE")) setCooldownAttribute(cleanText)
        if (cleanText.contains("used your")) abilityListener(cleanText)
        if (cleanText.contains("on cooldown")) onCooldownListener(cleanText)
        if (cleanText.contains("You selected")) abilitySwapListener(cleanText)
        if (cleanText.contains("consumed")) consumableListener(cleanText)
        if (cleanText.contains("You have reset")) treeResetListener(cleanText)
        if (cleanText.contains("Commission Complete")) commissionListener(cleanText)
        if (cleanText.startsWith("Disabled")) disableTreeBuffs(cleanText)
        if (cleanText.startsWith("Enabled") || cleanText.startsWith("You equipped")) enableTreeBuffs(cleanText)
        sacksListener(message, actionBar = false)

        if (text.startsWith("  THE RIFT IS COLLAPSING") || text.startsWith("Warping")) {
            TrackingHandler.pauseRiftTracking()
            MultiTrackingHandler.pauseMultiRiftTracking()
        }
    }

    fun sacksListener(component: Component, actionBar: Boolean) {
        if (actionBar) return
        val normalTracking = TrackingHandler.isTracking
        val multiTracking = MultiTrackingHandler.isMultiTracking

        if (!normalTracking && !multiTracking) return
        if (ConfigAccess.isApiTrackingEnabled()) return

        if (component.string.startsWith("[Sacks]")) {
            parseSacksMessage(component)
        }
    }

    @JvmStatic
    fun skillListener(text: String) {
        if (!SkillTrackingHandler.isTracking || !HypixelUtils.isOnSkyblock) return
        val cleanText = text.removeColor()

        val match = Patterns.SKILL.find(cleanText)
        if (match != null) {
            parseSkillMessage(match)
        }
    }

    private fun setCooldownAttribute(text: String) {
        if (ConfigAccess.hasCooldownAttributeMaxed()) return

        val value = Patterns.ATTRIBUTE.find(text.trimStart())?.groupValues?.get(1)
        if (value != null) {
            ConfigHelper.setAttributeLevel(value.toLevel())
        }
    }

    private fun String.toLevel(): Int = when (uppercase()) {
        "I" -> 1
        "II" -> 2
        "III" -> 3
        "IV" -> 4
        "V" -> 5
        "VI" -> 6
        "VII" -> 7
        "VIII" -> 8
        "IX" -> 9
        "X" -> 10
        else -> toIntOrNull() ?: 0
    }

    private fun abilityListener(text: String) {
        val match = Patterns.ABILITY.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        this.abilityName = abilityName
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

    private fun onCooldownListener(text: String) {
        if (!ConfigAccess.isServerLagProtectionEnabled()) return

        val match = Patterns.ON_COOLDOWN.find(text) ?: return
        val type = match.groupValues[1].trim()
        val time = match.groupValues[2].toLongOrNull() ?: return

        if (type == "Pickaxe" && abilityName != "Pickobulus") {
            syncTimer(pickaxeCooldown, time)
        } else if (type == "Axe") {
            syncTimer(axeCooldown, time)
        }
    }

    private fun syncTimer(timer: TimerState, time: Long) {
        val currentRemainingMs = (timer.remainingSeconds * 1000).toLong()
        val currentSeconds = currentRemainingMs / 1000

        if (currentSeconds != time) {
            val millisOffset = currentRemainingMs % 1000
            val newCooldown = (time * 1000) + millisOffset
            timer.start(newCooldown)
        }
    }

    private fun abilitySwapListener(text: String) {
        val match = Patterns.CHANGE_ABILITY.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        val toolType = match.groupValues[2].lowercase()

        if (toolType == "axe") {
            ConfigHelper.setAxeAbilityName(abilityName)
        } else {
            ConfigHelper.setAbilityName(abilityName)
        }
    }

    private fun consumableListener(text: String) {
        val match = Patterns.CONSUME.find(text) ?: return
        val consumableName = match.groupValues[1].trim()

        if (consumableName == "Refined Dark Cacao Truffle") {
            TemporaryBuffsParser.resetRefinedCacao()
        }
    }

    private fun startAbilityTimeline(ability: String, snap: AbilityUtils.PickaxeAbilitySnapshot?) {
        val cotm = ConfigAccess.getCotmLevel()
        val abilityLevel = if (cotm >= 2) 2 else 1
        val hasBlueCheese = snap?.hasBlueCheesePart == true

        val baseCooldown = AbilityUtils.getBaseCooldown(ability, abilityLevel, hasBlueCheese)
        val finalCooldownSec = AbilityUtils.calculateReduction(
            baseCooldown = baseCooldown,
            snap = snap,
            skyMallActive = isSkyMallPickaxeAbilityActive(),
            abilityName = ability
        )

        val durationMs = (AbilityUtils.getBaseDuration(ability, abilityLevel, hasBlueCheese) * 1000).toLong()

        maxCooldown = finalCooldownSec
        maxDuration = durationMs / 1000.0

        pickaxeDuration.start(durationMs)
        pickaxeCooldown.start((finalCooldownSec * 1000).toLong())

        ConfigHelper.setAbilityName(ability)
    }

    private fun startAxeAbilityTimeline(ability: String) {
        val cotf = ConfigAccess.getCotfLevel()
        val abilityLevel = if (cotf >= 2) 2 else 1

        val finalCooldownSec = AbilityUtils.getBaseAxeCooldown(ability, abilityLevel).toDouble()
        val durationMs = (AbilityUtils.getBaseAxeDuration(ability, abilityLevel) * 1000).toLong()

        maxAxeCooldown = finalCooldownSec
        maxAxeDuration = durationMs / 1000.0

        axeDuration.start(durationMs)
        axeCooldown.start((finalCooldownSec * 1000).toLong())

        ConfigHelper.setAxeAbilityName(ability)
    }

    private fun isSkyMallPickaxeAbilityActive(): Boolean {
        return isPickaxeAbility
    }

    @JvmStatic
    fun dailyPerksUpdate(message: Component): Boolean {
        if (!HypixelUtils.isOnSkyblock) return false

        val remaining = nextBuffTime - System.currentTimeMillis()
        if ((remaining > 60_000L && remaining < 19 * 60_000L) && ScoreboardUtils.checkTime) {
            return false
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
            text.contains("Your Beekeeper buff changed") -> {
                expectingBeekeeper = true
                return ConfigAccess.isBeekeeperEnabled()
            }
            text.startsWith("New buff: ") -> {
                val buffText = text.substringAfter("New buff: ").trim()

                val compact = compactBuffs(buffText)
                if (expectingSkyMallBuff) {
                    isPickaxeAbility = "Pickaxe Ability" in text
                    currentSkyMallBuff = compact
                    ConfigHelper.setLastSkyMallBuff(compact)
                    expectingSkyMallBuff = false
                    if (ConfigAccess.isSkyMallChatMessagesDisabled()) return true // Don't render Sky Mall buff in chat, but update the buffs in overlay

                    // Compact messages if overlay is enabled
                    if (ConfigAccess.isSkyMallEnabled()) {
                        ChatUtils.sendMessage("§eNew §bSky Mall §eBuff§r: $compact", prefix = true)
                        return true
                    }
                    return false
                }
                if (expectingLotteryBuff) {
                    currentLotteryBuff = compact
                    ConfigHelper.setLastLotteryBuff(compact)
                    expectingLotteryBuff = false
                    if (ConfigAccess.isLotteryChatMessagesDisabled()) return true // Don't render Lottery buff in chat, but update the buffs in overlay

                    // Compact messages if overlay is enabled
                    if (ConfigAccess.isLotteryEnabled()) {
                        ChatUtils.sendMessage("§eNew §2Lottery §eBuff§r: $compact", prefix = true)
                        return true
                    }
                    return false
                }
                if (expectingBeekeeper) {
                    currentBeekeeperBuff = compact
                    ConfigHelper.setLastBeekeeperBuff(compact)
                    expectingBeekeeper = false
                    if (ConfigAccess.isBeekeeperChatMessagesDisabled()) return true

                    if (ConfigAccess.isBeekeeperEnabled()) {
                        ChatUtils.sendMessage("§eNew §6Beekeeper §eBuff§r: $compact", prefix = true)
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

    fun compactBuffs(message: String): String {
        val text = message.trim().removeSuffix(".")

        val numberRegex = Regex("[+-]?\\d+")
        val percentRegex = Regex("[+-]?\\d+%")
        val xRegex = Regex("\\d+x", RegexOption.IGNORE_CASE)

        return when {
            // Sky Mall buffs
            "Mining Speed" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num \uE015 Mining Speed"
            }
            "Mining Fortune" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num \uE053 Mining Fortune"
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
            "chance" in text -> {
                val x = xRegex.find(text)?.value ?: numberRegex.find(text)?.value?.let { "${it}x" }
                "§a$x §6Golden §7and §bDiamond §7Goblins"
            }

            // Lottery buffs
            "Fig" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num \uE054 Fig Fortune"
            }
            "Mangrove" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num \uE054 Mangrove Fortune"
            }
            "Helix" in text -> {
                val num = numberRegex.find(text)?.value
                "§6$num \uE054 Helix Fortune"
            }
            "Sweep" in text -> {
                var rawPct = percentRegex.find(text)?.value
                var pct: String
                if (rawPct != null) {
                    pct = "${rawPct.trimEnd('%')}%"
                    "§a$pct §2\uE023 Sweep"
                } else {
                    rawPct = numberRegex.find(text)?.value
                    "§a$rawPct §2\uE023 Sweep"
                }
            }

            // Beekeeper buffs
            "Honeyhives refill" in text -> {
                val rawPct = percentRegex.find(text)?.value
                "§a$rawPct §6Honeyhives Refill"
            }
            "Trees lathered" in text && "attract" in text -> {
                val rawPct = percentRegex.find(text)?.value
                "§a$rawPct §a\uE05BCritter Speed"
            }
            "Gain" in text && "Honeycomb" in text -> {
                val x = xRegex.find(text)?.value ?: numberRegex.find(text)?.value?.let { "${it}x" }
                "§a$x §6Honeycomb"
            }
            "second Critter" in text -> {
                val rawPct = percentRegex.find(text)?.value
                " §a$rawPct §a\uE05BExtra Critter"
            }
            else -> message // fallback to original text
        }
    }

    private fun treeResetListener(text: String) {
        when {
            text.startsWith("You have reset your Heart of the Mountain", ignoreCase = true) -> FetchSkillTree.resetHotm()
            text.startsWith("You have reset your Heart of the Forest", ignoreCase = true) -> FetchSkillTree.resetHotf()
        }
    }

    private fun commissionListener(text: String) {
        val match = Patterns.COMMISSION.find(text) ?: return
        val commissionName = match.groupValues[1].trim()

        CommissionWidget.completeCollectorCommission(commissionName)
    }

    private fun disableTreeBuffs(text: String) {
        when (text) {
            "Disabled Sky Mall" -> {
                currentSkyMallBuff = "§cDisabled"
                ConfigHelper.setLastSkyMallBuff(currentSkyMallBuff)
                isPickaxeAbility = false
            }
            "Disabled Lottery" -> {
                currentLotteryBuff = "§cDisabled"
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
            }
            "Disabled Beekeeper" -> {
                currentBeekeeperBuff = "§cDisabled"
                ConfigHelper.setLastBeekeeperBuff(currentBeekeeperBuff)
            }
        }
    }

    private fun enableTreeBuffs(text: String) {
        when (text) {
            "Enabled Sky Mall" -> {
                currentSkyMallBuff = "§cUnknown"
                ConfigHelper.setLastSkyMallBuff(currentSkyMallBuff)
            }
            "Enabled Lottery" -> {
                currentLotteryBuff = "§cUnknown"
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
            }
            "Enabled Beekeeper" -> {
                currentBeekeeperBuff = "§cUnknown"
                ConfigHelper.setLastBeekeeperBuff(currentBeekeeperBuff)
            }
            else -> {
                currentSkyMallBuff = "§cUnknown"
                currentLotteryBuff = "§cUnknown"
                currentBeekeeperBuff = "§cUnknown"
                ConfigHelper.setLastSkyMallBuff(currentSkyMallBuff)
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
                ConfigHelper.setLastBeekeeperBuff(currentBeekeeperBuff)
            }
        }
    }

    @JvmStatic
    fun coleweightHandle(message: Component): Component {
        if (!HypixelUtils.isOnSkyblock || !ConfigAccess.isColeweightRankingInChat()) return message

        if (ConfigAccess.isOnlyOnMiningIslands()) {
            if (!MiningMapping.miningIslands.contains(IslandTracker.currentMiningIsland)) return message
        }

        val text = message.string.removeColor()

        val left = text.substringBefore(":").trim()
        val tokens = left.split(" ")

        val playerName = tokens.firstOrNull { ColeweightManager.storage.leaderboardRanks.containsKey(it.lowercase()) } ?: return message
        val rankSuffix = ColeweightUtils.getRankComponent(playerName) ?: return message

        return insertRankSuffix(message, rankSuffix)
    }

    @JvmStatic
    fun farmingweightHandle(message: Component): Component {
        if (!HypixelUtils.isOnSkyblock || !ConfigAccess.isFarmingweightRankingInChat()) return message

        if (ConfigAccess.isOnlyOnFarmingIslands()) {
            if (!FarmingMapping.farmingAreas.contains(IslandTracker.currentFarmingIsland)) return message
        }

        val text = message.string.removeColor()


        val left = text.substringBefore(":").trim()
        val tokens = left.split(" ")

        val playerName = tokens.firstOrNull { FarmingweightManager.storage.leaderboardRanks.containsKey(it.lowercase()) } ?: return message
        val rankSuffix = FarmingweightUtils.getRankComponent(playerName) ?: return message

        return insertRankSuffix(message, rankSuffix)
    }

    private fun insertRankSuffix(message: Component, rankSuffix: Component): Component {
        if (rankSuffix == Component.empty() || rankSuffix.string.isEmpty()) return message

        val siblings = message.siblings
        val newComponent = MutableComponent.create(message.contents).withStyle(message.style)
        var hasRank = false

        for (i in message.siblings.indices) {
            val sibling = siblings[i]
            val text = sibling.string

            if (!hasRank && text.contains(":")) {
                val colonIndex = text.indexOf(":")

                if (colonIndex > 0) {
                    val beforePart = text.substring(0, colonIndex)
                    val afterPart = text.substring(colonIndex)

                    newComponent.append(Component.literal(beforePart).withStyle(sibling.style))
                    newComponent.append(Component.literal(" "))
                    newComponent.append(rankSuffix)
                    newComponent.append(Component.literal(afterPart).withStyle(sibling.style))
                } else {
                    newComponent.append(Component.literal(" "))
                    newComponent.append(rankSuffix)
                    newComponent.append(sibling)
                }
                hasRank = true
            } else {
                newComponent.append(sibling)
            }
        }
        return if (hasRank) newComponent else message
    }

    private fun parseSacksMessage(message: Component) {
        val sacksDetails = mutableMapOf<String, Int>()
        var hasGains = false
        val processedHovers = mutableSetOf<Component>()

        for (sibling in message.siblings) {
            val hoverComponent = (sibling.style.hoverEvent as? HoverEvent.ShowText)?.value ?:continue

            if (!processedHovers.add(hoverComponent)) continue

            val hoverText = hoverComponent.string
            val isAddition = "Added items:" in hoverText
            val isRemoval = "Removed items:" in hoverText

            if (!isAddition && !isRemoval) continue

            val hoverSiblings = mutableListOf<Component>()
            fun collect(c: Component) {
                hoverSiblings.add(c)
                c.siblings.forEach { collect(it) }
            }
            collect(hoverComponent)

            var i = 0
            while (i < hoverSiblings.size) {
                val text = hoverSiblings[i].string.trim()

                if (text.startsWith("+") || text.startsWith("-")) {
                    val amount = text.filter { it.isDigit() }.toIntOrNull()

                    if (amount != null) {
                        for (j in i + 1 until hoverSiblings.size) {
                            val sibling = hoverSiblings[j].string.trim()

                            if (sibling.isNotEmpty() && !sibling.startsWith("+") && !sibling.startsWith("-") && !sibling.contains("Sack)")) {
                                val collectionName = sibling.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()
                                val currentVal = sacksDetails.getOrDefault(collectionName, 0)

                                if (isAddition) {
                                    sacksDetails[collectionName] = currentVal + amount
                                    hasGains = true
                                } else {
                                    sacksDetails[collectionName] = currentVal - amount
                                }
                                i = j
                                break
                            }
                        }
                    }
                }
                i++
            }
        }

        if (hasGains) {
            SacksTrackingManager.onSacksGain(sacksDetails)
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