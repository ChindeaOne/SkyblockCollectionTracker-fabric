package io.github.chindeaone.collectiontracker.utils.hotx

import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

enum class HotxPerkState {
    NOT_UNLOCKED,
    UNLOCKED,
    SELECTED,
    DISABLED,
    MAXED,
    UNKNOWN;

    fun isMaxed() = this == MAXED
    fun isSelected() = this == SELECTED
    fun isEnabled() = this == UNLOCKED

    companion object {
        private val LEVEL_REGEX = Regex("""^Level \s*(\d+)(?:\s*/\s*(\d+))?\s*$""", RegexOption.IGNORE_CASE)

        fun detectHotmPerk(stack: ItemStack): HotxPerkState {
            val model = stack.get(DataComponents.ITEM_MODEL)

            return when (model) {
                HotxModels.COAL.getIdentifierFromModel() -> NOT_UNLOCKED
                HotxModels.EMERALD.getIdentifierFromModel() -> UNLOCKED
                HotxModels.DIAMOND.getIdentifierFromModel() -> MAXED
                HotxModels.REDSTONE_BLOCK.getIdentifierFromModel() -> DISABLED
                else -> detectPerkFromTooltips(stack)
            }
        }

        fun detectHotmAbility(stack: ItemStack): HotxPerkState {
            val model = stack.get(DataComponents.ITEM_MODEL)

            return when (model) {
                HotxModels.COAL_BLOCK.getIdentifierFromModel() -> NOT_UNLOCKED
                HotxModels.EMERALD_BLOCK.getIdentifierFromModel() -> SELECTED
                HotxModels.REDSTONE_BLOCK.getIdentifierFromModel() -> UNLOCKED
                else -> detectAbilityFromTooltips(stack)
            }
        }

        fun detectHotfPerk(stack: ItemStack): HotxPerkState {
            val model = stack.get(DataComponents.ITEM_MODEL)

            return when (model) {
                HotxModels.PALE_OAK_BUTTON.getIdentifierFromModel() -> NOT_UNLOCKED
                HotxModels.STRIPPED_OAK_LOG.getIdentifierFromModel() -> UNLOCKED
                HotxModels.OAK_LOG.getIdentifierFromModel() -> MAXED
                HotxModels.STRIPPED_MANGROVE_LOG.getIdentifierFromModel() -> DISABLED
                else -> detectPerkFromTooltips(stack)
            }
        }

        fun detectHotfAbility(stack: ItemStack): HotxPerkState {
            val model = stack.get(DataComponents.ITEM_MODEL)

            return when (model) {
                HotxModels.PALE_OAK_SAPLING.getIdentifierFromModel() -> NOT_UNLOCKED
                HotxModels.OAK_SAPLING.getIdentifierFromModel() -> detectAbilityFromTooltips(stack)
                else -> detectAbilityFromTooltips(stack)
            }
        }

        fun parsePerkCurrentLevel(stack: ItemStack): Int? {
            val lines = ItemUtils.getTooltipLines(stack)
            val level = lines.getOrNull(1) ?: return null

            val match = LEVEL_REGEX.matchEntire(level.trimStart()) ?: return null
            return match.groupValues[1].toIntOrNull()
        }

        private fun detectPerkFromTooltips(stack: ItemStack): HotxPerkState {
            val lines = ItemUtils.getTooltipLines(stack)

            return when {
                lines.any { it.contains("DISABLED") } -> DISABLED
                lines.any { it.contains("ENABLED") } -> UNLOCKED
                else -> UNKNOWN
            }
        }

        private fun detectAbilityFromTooltips(stack: ItemStack): HotxPerkState {
            val lines = ItemUtils.getTooltipLines(stack)

            return when {
                lines.any { it.contains("SELECTED") } -> SELECTED
                lines.any { it.contains("Click to select") } -> UNLOCKED
                else -> UNKNOWN
            }
        }

        fun fromApi(level: Int, enabled: Boolean): HotxPerkState = when {
            level == 0 -> NOT_UNLOCKED
            enabled -> UNLOCKED
            else -> DISABLED
        }
    }
}
