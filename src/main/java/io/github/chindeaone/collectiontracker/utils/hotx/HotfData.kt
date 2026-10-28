package io.github.chindeaone.collectiontracker.utils.hotx

import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.foraging.AxeAbilityState
import io.github.chindeaone.collectiontracker.config.categories.foraging.BeekeeperState
import io.github.chindeaone.collectiontracker.config.categories.foraging.HotfLoadout
import io.github.chindeaone.collectiontracker.config.categories.foraging.LotteryState
import io.github.chindeaone.collectiontracker.config.cotfLevel
import io.github.chindeaone.collectiontracker.config.currentHotfLoadout
import io.github.chindeaone.collectiontracker.config.currentHotfLoadoutName
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.parser.ContainerParser
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack

object HotfData {
    val selectedLoadout: HotfLoadout get() = currentHotfLoadout
    val loadoutName: String get() = currentHotfLoadoutName

    val lottery: LotteryState get() = selectedLoadout.lottery
    val beekeeper: BeekeeperState get() = selectedLoadout.beekeeper
    val axeAbility: AxeAbilityState get() = selectedLoadout.axeAbility

    var currentAxeAbility: Ability
        get() = axeAbility.ability
        set(value) {
            axeAbility.ability = value
        }

    private var lastCoreLevel: Int = cotfLevel

    fun update(screen: AbstractContainerScreen<*>) {
        updateCurrentLoadout(screen)

        for (stack in ContainerParser.visibleSlots(screen)) {
            if (stack.isEmpty) continue

            val name = stack.hoverName.string
            if (name.isBlank()) continue

            if (isCOTF(name)) {
                updateCore(stack)
                continue
            }

            when (name) {
                "Lottery" -> updateLottery(stack)
                "Beekeeper" -> updateBeekeeper(stack)
            }

            updateAbilityCandidate(stack, name)
        }
    }

    private fun updateCore(stack: ItemStack) {
        if (cotfLevel == 5) return

        val level = HotxPerkState.parsePerkCurrentLevel(stack) ?: return
        if (level != lastCoreLevel) {
            lastCoreLevel = level
            ConfigHelper.setCotfLevel(level)
        }
    }

    private fun updateLottery(stack: ItemStack) {
        val state = HotxPerkState.detectHotfPerk(stack)
        val effect = RotatingPerk.parseRotatingPerkEffect(stack)

        lottery.state = state

        when (state) {
            HotxPerkState.NOT_UNLOCKED -> lottery.lastLotteryBuff = "§eNot unlocked"
            HotxPerkState.DISABLED -> lottery.lastLotteryBuff = "§cDisabled"
            HotxPerkState.UNLOCKED, HotxPerkState.MAXED, HotxPerkState.SELECTED -> {
                lottery.lastLotteryBuff = if (effect.isNullOrBlank()) "§cUnknown" else effect
            }
            HotxPerkState.UNKNOWN -> lottery.lastLotteryBuff = ""
        }
    }

    private fun updateBeekeeper(stack: ItemStack) {
        val state = HotxPerkState.detectHotfPerk(stack)
        val effect = RotatingPerk.parseRotatingPerkEffect(stack)

        beekeeper.state = state

        when (state) {
            HotxPerkState.NOT_UNLOCKED -> beekeeper.lastBeekeeperBuff = "§eNot unlocked"
            HotxPerkState.DISABLED -> beekeeper.lastBeekeeperBuff = "§cDisabled"
            HotxPerkState.UNLOCKED, HotxPerkState.SELECTED, HotxPerkState.MAXED -> {
                beekeeper.lastBeekeeperBuff = if (effect.isNullOrBlank()) "§cUnknown" else effect
            }
            HotxPerkState.UNKNOWN -> beekeeper.lastBeekeeperBuff = ""
        }
    }

    private fun updateAbilityCandidate(stack: ItemStack, cleanName: String) {
        val ability = Ability.fromName(cleanName) ?: return
        val state = HotxPerkState.detectHotfAbility(stack)
        if (state.isSelected()) {
            currentAxeAbility = ability
        }
    }

    fun switchLoadout(newLoadoutName: String) {
        if (newLoadoutName.isBlank() || newLoadoutName == loadoutName) return

        ConfigHelper.switchHotfLoadout(newLoadoutName)
    }

    fun updateCurrentLoadout(screen: AbstractContainerScreen<*>, slotIndex: Int = 47) {
        if (slotIndex >= screen.menu.slots.size) return
        val stack = screen.menu.getSlot(slotIndex).item
        val parsedName = ContainerParser.parseCurrentLoadoutName(stack) ?: return
        switchLoadout(parsedName)
    }

    fun updateSlots(screen: AbstractContainerScreen<*>) {
        for (slot in 11..15) {
            val stack = screen.menu.getSlot(slot).item
            if (stack.isEmpty) continue

            val name = stack.hoverName.string
            if (name.isBlank()) return

            val slotIndex = slot - 11
            ConfigHelper.renameHotfLoadout(slotIndex, name)
        }
    }

    private fun isCOTF(name: String): Boolean = name == "Center of the Forest"

    fun updateLotteryFromChat(compactBuff: String) {
        lottery.lastLotteryBuff = compactBuff
    }

    fun updateBeekeeperFromChat(compactBuff: String) {
        beekeeper.lastBeekeeperBuff = compactBuff
    }

    fun resetHotf() {
        lottery.state = HotxPerkState.NOT_UNLOCKED
        lottery.lastLotteryBuff = "§eNot unlocked"

        beekeeper.state = HotxPerkState.NOT_UNLOCKED
        beekeeper.lastBeekeeperBuff = "§eNot unlocked"

        currentAxeAbility = Ability.UNKNOWN
    }
}