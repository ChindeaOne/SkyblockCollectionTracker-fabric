package io.github.chindeaone.collectiontracker.utils.hotx

import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.HotmLoadout
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.PickaxeAbilityState
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.ProfessionalState
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.SkyMallState
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.StrongArmState
import io.github.chindeaone.collectiontracker.config.cotmLevel
import io.github.chindeaone.collectiontracker.config.currentHotmLoadout
import io.github.chindeaone.collectiontracker.config.currentHotmLoadoutName
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.parser.ContainerParser
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

// TODO: Make mayhem as a config option
object HotmData {
    val selectedLoadout: HotmLoadout get() = currentHotmLoadout
    val loadoutName: String get() = currentHotmLoadoutName

    val strongArm: StrongArmState get() = selectedLoadout.strongArm
    val professional: ProfessionalState get() = selectedLoadout.professional
    val skyMall: SkyMallState get() = selectedLoadout.skyMall
    val pickaxeAbility: PickaxeAbilityState get() = selectedLoadout.pickaxeAbility

    var currentPickaxeAbility: Ability
        get() = pickaxeAbility.ability
        set(value) {
            pickaxeAbility.ability = value
        }

    val isSkyMallCooldown: Boolean get() = skyMall.lastSkyMallBuff.contains("Pickaxe")

    var mineshaftMayhemState: HotxPerkState = HotxPerkState.UNKNOWN
    var isMayhemCooldown: Boolean = false

    private var lastCoreLevel: Int = cotmLevel

    fun update(screen: AbstractContainerScreen<*>) {
        updateCurrentLoadout(screen)
        
        for (stack in ContainerParser.visibleSlots(screen)) {
            if (stack.isEmpty) continue

            val name = stack.hoverName.string

            if (isCOTM(name)) {
                updateCore(stack)
                continue
            }

            when (name) {
                "Sky Mall" -> updateSkyMall(stack)
                "Strong Arm" -> updateStrongArm(stack)
                "Professional" -> updateProfessional(stack)
                "Mineshaft Mayhem" -> updateMineshaftMayhem(stack)
            }

            updateAbilityCandidate(stack, name)
        }
    }
    
    private fun updateCore(stack: ItemStack) {
        if (cotmLevel == 10) return

        val level = HotxPerkState.parsePerkCurrentLevel(stack) ?: return
        if (level != lastCoreLevel) {
            lastCoreLevel = level
            ConfigHelper.setCotmLevel(level)
        }
    }

    private fun updateSkyMall(stack: ItemStack) {
        val state = HotxPerkState.detectHotmPerk(stack)
        val effect = RotatingPerk.parseRotatingPerkEffect(stack)
        
        skyMall.state = state

        when (state) {
            HotxPerkState.NOT_UNLOCKED -> skyMall.lastSkyMallBuff = "§eNot unlocked"
            HotxPerkState.DISABLED -> skyMall.lastSkyMallBuff = "§cDisabled"
            HotxPerkState.UNLOCKED, HotxPerkState.MAXED, HotxPerkState.SELECTED -> {
                skyMall.lastSkyMallBuff = if (effect.isNullOrBlank()) "§cUnknown" else effect
            }
            HotxPerkState.UNKNOWN -> skyMall.lastSkyMallBuff = ""
        }
    }

    private fun updateStrongArm(stack: ItemStack) {
        val state = HotxPerkState.detectHotmPerk(stack)
        strongArm.state = state
        if (state.isMaxed()) {
            strongArm.level = 100
            return
        }

        val level = HotxPerkState.parsePerkCurrentLevel(stack) ?: return

        if (!state.isEnabled()) {
            strongArm.level = 0
            return
        }

        strongArm.level = level
    }

    private fun updateProfessional(stack: ItemStack) {
        val state = HotxPerkState.detectHotmPerk(stack)
        professional.state = state
        if (state.isMaxed()) {
            professional.level = 140
            return
        }

        val level = HotxPerkState.parsePerkCurrentLevel(stack) ?: return

        if (!state.isEnabled()) {
            professional.level = 0
            return
        }

        professional.level = level
    }

    private fun updateMineshaftMayhem(stack: ItemStack) {
        mineshaftMayhemState = HotxPerkState.detectHotmPerk(stack)
    }

    private fun updateAbilityCandidate(stack: ItemStack, name: String) {
        val model = stack.get(DataComponents.ITEM_MODEL)

        if (model == HotxModels.EMERALD_BLOCK.getIdentifierFromModel()) {
            Ability.fromName(name)?.let {
                currentPickaxeAbility = it
                return
            }
        }

        val ability = Ability.fromName(name) ?: return
        val tooltipsState = HotxPerkState.detectHotmAbility(stack)
        if (tooltipsState.isSelected()) {
            currentPickaxeAbility = ability
        }
    }

    fun switchLoadout(newLoadoutName: String) {
        if (newLoadoutName.isBlank() || newLoadoutName == loadoutName) return

        ConfigHelper.switchHotmLoadout(newLoadoutName)
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
            ConfigHelper.renameHotmLoadout(slotIndex, name)
        }
    }

    private fun isCOTM(name: String): Boolean = name == "Core of the Mountain"

    fun updateSkyMallFromChat(compactBuff: String) {
        skyMall.lastSkyMallBuff = compactBuff
    }

    fun updateMayhemFromChat(component: Component): Boolean {
        if (IslandTracker.currentMiningIsland != "Mineshaft") return true

        val text = component.string
        if (!text.startsWith("MAYHEM!")) return true

        isMayhemCooldown = text.contains("Your Pickaxe Ability cooldown was reduced from your Mineshaft Mayhem perk!")

        return true
    }

    fun resetHotm() {
        strongArm.state = HotxPerkState.NOT_UNLOCKED
        strongArm.level = 0

        professional.state = HotxPerkState.NOT_UNLOCKED
        professional.level = 0

        skyMall.state = HotxPerkState.NOT_UNLOCKED
        skyMall.lastSkyMallBuff = "§eNot unlocked"

        currentPickaxeAbility = Ability.UNKNOWN

        mineshaftMayhemState = HotxPerkState.NOT_UNLOCKED
        isMayhemCooldown = false
    }
}