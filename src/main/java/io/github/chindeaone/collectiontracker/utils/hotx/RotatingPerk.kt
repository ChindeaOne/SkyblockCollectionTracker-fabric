package io.github.chindeaone.collectiontracker.utils.hotx

import io.github.chindeaone.collectiontracker.config.disableBeekeeperChatMessages
import io.github.chindeaone.collectiontracker.config.disableLotteryChatMessages
import io.github.chindeaone.collectiontracker.config.disableSkyMallChatMessages
import io.github.chindeaone.collectiontracker.config.enableBeekeeper
import io.github.chindeaone.collectiontracker.config.enableLottery
import io.github.chindeaone.collectiontracker.config.enableSkyMall
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import kotlin.text.isNotBlank

enum class RotatingPerk(
    private val perkName: String,
    val displayName: String
) {
    SKY_MALL(
        "Sky Mall",
        "§6Sky Mall"
    ),
    LOTTERY(
        "Lottery",
        "§2Lottery"
    ),
    BEEKEEPER(
        "Beekeeper",
        "§2Beekeeper"
    );

    companion object {
        private val BY_NAME = RotatingPerk.entries.associateBy { it.perkName.lowercase() }

        fun fromName(name: String): RotatingPerk? = BY_NAME[name.lowercase()]

        val ROTATING_PERK = Regex("""^New day! Your (Sky Mall|Lottery|Beekeeper) buff changed!""", RegexOption.IGNORE_CASE)
        val ROTATING_BUFF = Regex("""^New buff:\s*(.+)""", RegexOption.IGNORE_CASE)

        private var pendingRotatingBuff: RotatingPerk? = null

        fun formatRotatingBuffs(message: Component): Boolean {
            val text = message.string.removeColor()

            val perkMatch = ROTATING_PERK.find(text)
            if (perkMatch != null) {
                val perk = perkMatch.groupValues[1].trim()
                pendingRotatingBuff = fromName(perk)

                return when (pendingRotatingBuff) {
                    SKY_MALL -> !enableSkyMall
                    LOTTERY -> !enableLottery
                    BEEKEEPER -> !enableBeekeeper
                    else -> true
                }
            }

            val buffMatch = ROTATING_BUFF.find(text)
            if (buffMatch != null) {
                val buffText = buffMatch.groupValues[1].trim()
                val compactBuff = compactRotatingPerkBuff(buffText)

                return updateRotatingPerkFromChat(compactBuff)
            }

            // Don't render these messages at all
            if (enableSkyMall || enableLottery || enableBeekeeper) {
                return !text.startsWith("You can disable this messaging by toggling")
            }

            return true
        }

        private fun updateRotatingPerkFromChat(compactBuff: String): Boolean {
            when (val perk = pendingRotatingBuff) {
                SKY_MALL -> {
                    HotmData.updateSkyMallFromChat(compactBuff)
                    pendingRotatingBuff = null

                    if (disableSkyMallChatMessages) return false
                    if (enableSkyMall) {
                        ChatUtils.sendMessage("§eNew ${perk.displayName} §ebuff§r: $compactBuff")
                        return false
                    }
                    return true
                }

                LOTTERY -> {
                    HotfData.updateLotteryFromChat(compactBuff)
                    pendingRotatingBuff = null

                    if (disableLotteryChatMessages) return false
                    if (enableLottery) {
                        ChatUtils.sendMessage("§eNew ${perk.displayName} §ebuff§r: $compactBuff")
                        return false
                    }
                    return true
                }

                BEEKEEPER -> {
                    HotfData.updateBeekeeperFromChat(compactBuff)
                    pendingRotatingBuff = null

                    if (disableBeekeeperChatMessages) return false
                    if (enableBeekeeper) {
                        ChatUtils.sendMessage("§eNew ${perk.displayName} §ebuff§r: $compactBuff")
                        return false
                    }
                    return true
                }
                null -> return true
            }
        }

        fun parseRotatingPerkEffect(stack: ItemStack): String? {
            val lines = ItemUtils.getTooltipLines(stack)
            val idx = lines.indexOfFirst { it == "Your Current Effect" }
            if (idx < 0) return null

            val effect = lines.drop(idx + 1)
                .takeWhile { it.isNotBlank() }
                .joinToString(" ")
                .takeIf { it.isNotEmpty() }
                ?.let(::compactRotatingPerkBuff)

            return effect
        }

        fun compactRotatingPerkBuff(message: String): String {
            val numberRegex = Regex("[+-]?\\d+")
            val percentRegex = Regex("[+-]?\\d+%")
            val xRegex = Regex("\\d+x", RegexOption.IGNORE_CASE)

            return when {
                // Sky Mall buffs
                "Mining Speed" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§6$num\uE015 Mining Speed"
                }
                "Mining Fortune" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§6$num\uE053 Mining Fortune"
                }
                "Titanium" in message -> {
                    val x = xRegex.find(message)?.value ?: numberRegex.find(message)?.value?.let { "${it}x" }
                    "§a$x §9Titanium"
                }
                "Pickaxe Ability" in message -> {
                    val pct = percentRegex.find(message)?.value
                    "§a$pct §9Pickaxe CD"
                }
                "Powder" in message -> {
                    val pct = percentRegex.find(message)?.value
                    "§a$pct §9Powder"
                }
                "Goblins" in message -> {
                    val x = xRegex.find(message)?.value ?: numberRegex.find(message)?.value?.let { "${it}x" }
                    "§a$x §6Elusive §bGoblins"
                }

                // Lottery buffs
                "Fig" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§6$num\uE054 Fig Fortune"
                }
                "Mangrove" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§6$num\uE054 Mangrove Fortune"
                }
                "Helix" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§6$num\uE054 Helix Fortune"
                }
                "Sweep" in message -> {
                    val num = numberRegex.find(message)?.value
                    "§2$num\uE023 Sweep"
                }

                // Beekeeper buffs
                "Honeyhives refill" in message -> {
                    val pct = percentRegex.find(message)?.value
                    "§a$pct §6Honeyhives Refill"
                }
                "Critters" in message -> {
                    val pct = percentRegex.find(message)?.value
                    "§a$pct Faster \uE05BCritters"
                }
                "Gain 2x" in message -> {
                    val x = xRegex.find(message)?.value ?: numberRegex.find(message)?.value?.let { "${it}x" }
                    "§a$x §6Honeycomb"
                }
                "Critter" in message -> {
                    val pct = percentRegex.find(message)?.value
                    "§a$pct Extra \uE05BCritter"
                }

                else -> message // fallback to original text
            }
        }
    }
}