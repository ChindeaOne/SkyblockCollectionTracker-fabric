package io.github.chindeaone.collectiontracker.utils.chat

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.attributeLevel
import io.github.chindeaone.collectiontracker.config.coleweightRankingInChat
import io.github.chindeaone.collectiontracker.config.farmingweightRankingInChat
import io.github.chindeaone.collectiontracker.config.onlyOnFarmingIslands
import io.github.chindeaone.collectiontracker.config.onlyOnMiningIslands
import io.github.chindeaone.collectiontracker.config.serverLagProtection
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightUtils
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.ability.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.hotx.HotfData
import io.github.chindeaone.collectiontracker.utils.hotx.HotmData
import io.github.chindeaone.collectiontracker.utils.tab.CommissionWidget
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

object ChatListener {

    private enum class Patterns(pattern: String, vararg options: RegexOption) {
        // Skyhanni's skill pattern
        SKILL("""\+(?<gains>[\d,.]+)\s+(?<skillName>.+?)\s*\((?<current>[\d,]+)\s*/\s*(?<needed>[\d,]+)\)""", RegexOption.IGNORE_CASE),
        // Coleweight pattern
        ABILITY_USE("""^You used your (.+?)(?: (Pickaxe|Axe) Ability)?!""", RegexOption.IGNORE_CASE),
        CHANGE_ABILITY("""^You selected (.+?) as your (Pickaxe|Axe)? Ability""", RegexOption.IGNORE_CASE),
        ON_COOLDOWN("""^Your (.+?) ability is on cooldown for (\d+)s.""", RegexOption.IGNORE_CASE),
        EXPIRED_ABILITY("""^Your (.+?) has expired!""", RegexOption.IGNORE_CASE),
        ATTRIBUTE("""^ATTRIBUTE\s+LEVEL\s+UP\s+Pickaxe\s+Cooldown.*?([\d]+|[IVX]+)\b$""", RegexOption.IGNORE_CASE),
        COMMISSION("""^(.+?)\s+Commission Complete!.*$""", RegexOption.IGNORE_CASE);
        val regex: Regex = Regex(pattern, options.toSet())

        fun find(input: CharSequence): MatchResult? = regex.find(input)
    }

    var lastSkillValue = 0L

    fun onChatMessage(message: Component) {
        val text = message.string
        val cleanText = text.removeColor()

        if (cleanText.contains("ATTRIBUTE")) setCooldownAttribute(cleanText)

        // fallback methods
        if (cleanText.contains("is now available")) onReadyAbility(cleanText)
        if (cleanText.contains("on cooldown")) onCooldownAbility(cleanText)
        if (cleanText.contains("has expired!")) onExpiredAbility(cleanText)
        if (cleanText.contains("You used your")) onUsedAbility(cleanText)
        if (cleanText.contains("You selected")) onChangedAbility(cleanText)

        treeResetListener(cleanText)
        sacksListener(message)

        if (cleanText.contains("Commission Complete")) commissionListener(cleanText)

        if (text.startsWith("  THE RIFT IS COLLAPSING") || text.startsWith("Warping")) {
            TrackingHandler.pauseRiftTracking()
            MultiTrackingHandler.pauseMultiRiftTracking()
        }
    }

    fun sacksListener(component: Component) {
        if (!TrackingHandler.isTracking && !MultiTrackingHandler.isMultiTracking) return
        if (TrackingHandler.isPaused || MultiTrackingHandler.isMultiPaused) return
        if (apiTracking) return

        if (component.string.startsWith("[Sacks]")) parseSacksMessage(component)
    }

    @JvmStatic
    fun skillListener(text: String) {
        if (!SkillTrackingHandler.isTracking) return
        val cleanText = text.removeColor()

        val match = Patterns.SKILL.find(cleanText)
        if (match != null) {
            parseSkillMessage(match)
        }
    }

    private fun setCooldownAttribute(text: String) {
        if (attributeLevel == 10) return

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

    private fun onReadyAbility(text: String) {
        val abilityName = text.substringBefore("is now available").trim()
        AbilityUtils.onChatFallback(abilityName, fallbackType = AbilityUtils.FallbackType.READY)
    }

    private fun onExpiredAbility(text: String) {
        val match = Patterns.EXPIRED_ABILITY.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        AbilityUtils.onChatFallback(abilityName, fallbackType = AbilityUtils.FallbackType.EXPIRED)
    }

    private fun onUsedAbility(text: String) {
        val match = Patterns.ABILITY_USE.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        AbilityUtils.onChatFallback(abilityName, fallbackType = AbilityUtils.FallbackType.USED)
    }

    private fun onCooldownAbility(text: String) {
        if (!serverLagProtection) return

        val match = Patterns.ON_COOLDOWN.find(text) ?: return
        val type = match.groupValues[1].trim()
        val time = match.groupValues[2].toIntOrNull() ?: return

        AbilityUtils.onChatFallback(type = type, time = time, fallbackType = AbilityUtils.FallbackType.COOLDOWN)
    }

    private fun onChangedAbility(text: String) {
        val match = Patterns.CHANGE_ABILITY.find(text) ?: return
        val abilityName = match.groupValues[1].trim()
        AbilityUtils.onChatFallback(abilityName, fallbackType = AbilityUtils.FallbackType.CHANGED)
    }

    private fun treeResetListener(text: String) {
        when {
            text.startsWith("You have reset your Heart of the Mountain") -> {
                HotmData.resetHotm()
            }

            text.startsWith("You have reset your Heart of the Forest") -> {
                HotfData.resetHotf()
            }
        }
    }

    private fun commissionListener(text: String) {
        val match = Patterns.COMMISSION.find(text) ?: return
        val commissionName = match.groupValues[1].trim()

        CommissionWidget.completeCollectorCommission(commissionName)
    }

    fun coleweightHandle(message: Component): Component {
        if (!coleweightRankingInChat) return message
        if (onlyOnMiningIslands && !IslandTracker.isMiningIsland()) return message

        val text = message.string.removeColor()
        if (text.startsWith("[SCT]")) return message

        val left = text.substringBefore(":").trim()
        val tokens = left.split(" ")

        val playerName = tokens.firstOrNull { ColeweightManager.storage.leaderboardRanks.containsKey(it.lowercase()) } ?: return message
        val rankSuffix = ColeweightUtils.getRankComponent(playerName) ?: return message

        return insertRankSuffix(message, rankSuffix)
    }

    fun farmingweightHandle(message: Component): Component {
        if (!farmingweightRankingInChat) return message
        if (onlyOnFarmingIslands && !IslandTracker.isFarmingIsland()) return message

        val text = message.string.removeColor()
        if (text.startsWith("[SCT]")) return message

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

        for (i in siblings.indices) {
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
}