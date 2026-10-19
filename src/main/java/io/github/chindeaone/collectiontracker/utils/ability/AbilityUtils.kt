package io.github.chindeaone.collectiontracker.utils.ability

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.pickaxeAbilityName
import io.github.chindeaone.collectiontracker.config.attributeLevel
import io.github.chindeaone.collectiontracker.config.axeAbilityName
import io.github.chindeaone.collectiontracker.config.cotfLevel
import io.github.chindeaone.collectiontracker.config.cotmLevel
import io.github.chindeaone.collectiontracker.utils.hotx.HotmData
import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping
import net.minecraft.client.Minecraft
import kotlin.collections.contains
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object AbilityUtils {

    enum class AbilityType {
        PICKAXE,
        AXE
    }

    data class AbilitySnapshot (
        val abilityType: AbilityType,
        val fuelTank: FuelTank?,
        val hasBlueCheesePart: Boolean,
    ) {
        val isPickaxe: Boolean get() = abilityType == AbilityType.PICKAXE
        val isAxe: Boolean get() = abilityType == AbilityType.AXE
    }

    enum class FuelTank(val cooldownReduction: Double) {
        MITHRIL(0.02),
        TITANIUM(0.04),
        GEMSTONE(0.06),
        PERFECTLY_CUT(0.10)
    }

    var currentPickaxeAbility: Ability? = null
    var currentAxeAbility: Ability? = null

    val pickaxeState = AbilityState()
    val axeState = AbilityState()

    val finalPickaxeCooldown: Double get() = pickaxeState.cooldownSeconds
    val finalPickaxeDuration: Double get() = pickaxeState.durationSeconds
    val finalAxeCooldown: Double get() = axeState.cooldownSeconds
    val finalAxeDuration: Double get() = axeState.durationSeconds
    val maxPickaxeCooldown: Duration get() = pickaxeState.maxCooldown
    val maxPickaxeDuration: Duration get() = pickaxeState.maxDuration
    val maxAxeCooldown: Duration get() = axeState.maxCooldown
    val maxAxeDuration: Duration get() = axeState.maxDuration

    private var lastTooltips: List<String> = emptyList()

    fun getPickaxeEffectiveLevel(cotmLevel: Int, hasBlegg: Boolean): Int {
        val baseLevel = cotmLevel.coerceIn(1, 2)
        return if (hasBlegg) baseLevel + 1 else baseLevel
    }

    fun getAxeEffectiveLevel(cotfLevel: Int): Int {
        return cotfLevel.coerceIn(1, 2)
    }

    fun calculatePickaxeAbilityReduction(
        baseCooldown: Duration,
        snap: AbilitySnapshot?
    ): Duration {
        var multiplier = 1.0

        // Fuel Tank
        if (snap?.fuelTank != null) {
            multiplier *= (1.0 - (snap.fuelTank.cooldownReduction))
        }

        if (attributeLevel > 0) {
            multiplier *= (1.0 - attributeLevel / 100f)
        }

        // Sky Mall
        if (HotmData.isSkyMallCooldown && MiningMapping.miningIslands.contains(IslandTracker.currentMiningIsland)) {
            multiplier *= 0.8 // apparently it's more for pickobulus
        }

        // Mayhem cooldown reduction
        if (IslandTracker.currentMiningIsland == "Mineshaft" && HotmData.isMayhemCooldown) {
            multiplier *= 0.75
        }

        val finalDuration = (baseCooldown.inWholeSeconds * multiplier).coerceAtLeast(0.0)
        return finalDuration.seconds
    }

    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % 2L != 0L) return

        val player = client.player ?: return
        val stack = player.mainHandItem

        val tooltips = if (stack.isEmpty) emptyList() else ItemUtils.getTooltipLines(stack, player, normalize = true)

        if (tooltips != lastTooltips) {
            lastTooltips = tooltips
            handleItemChange(tooltips)
        }
    }

    private fun handleItemChange(tooltips: List<String>) {
        // only for MSB
        if (!pickaxeState.isActive || currentPickaxeAbility != Ability.MINING_SPEED_BOOST) return

        val parsedSnap = AbilityItemParser.parse(tooltips)

        if (parsedSnap is AbilitySnapshot && pickaxeState.isActive) {
            val hasBlegg = parsedSnap.hasBlueCheesePart
            val effectiveLevel = getPickaxeEffectiveLevel(cotmLevel, hasBlegg)

            val newDuration = Ability.MINING_SPEED_BOOST.getDuration(effectiveLevel)

            pickaxeState.updateDuration(newDuration)
        }
    }

    fun onItemUse(snap: AbilitySnapshot?) {
        if (snap == null) return

        if (snap.isPickaxe && pickaxeState.isReady) {
            triggerPickaxeAbility(snap)
        } else if (snap.isAxe && axeState.isReady) {
            triggerAxeAbility()
        }
    }

    fun triggerPickaxeAbility(snap: AbilitySnapshot? = null) {
        if (!pickaxeState.isReady) return

        val selectedName = currentPickaxeAbility?.abilityName ?: pickaxeAbilityName
        val ability = Ability.fromName(selectedName) ?: return

        currentPickaxeAbility = ability

        val effectiveLevel = getPickaxeEffectiveLevel(cotmLevel, snap?.hasBlueCheesePart == true)

        val baseCooldown = ability.getCooldown(effectiveLevel)
        val finalCooldown = calculatePickaxeAbilityReduction(
            baseCooldown = baseCooldown,
            snap = snap,
        )
        val duration = ability.getDuration(effectiveLevel)

        pickaxeState.start(duration, finalCooldown)
        ConfigHelper.setPickaxeAbilityName(ability.abilityName)
    }

    fun triggerAxeAbility() {
        if (!axeState.isReady) return

        val selectedName = currentAxeAbility?.abilityName ?: axeAbilityName
        val ability = Ability.fromName(selectedName) ?: return

        currentAxeAbility = ability

        val effectiveLevel = getAxeEffectiveLevel(cotfLevel)

        val cooldown = ability.getCooldown(effectiveLevel)
        val duration = ability.getDuration(effectiveLevel)

        axeState.start(duration, cooldown)
        ConfigHelper.setAxeAbilityName(ability.abilityName)
    }

    fun updateAbilityFromName(ability: Ability, name: String) {
        if (ability.isPickaxeAbility() && currentPickaxeAbility != ability) {
            currentPickaxeAbility = ability
            ConfigHelper.setPickaxeAbilityName(name)
        } else if (ability.isAxeAbility() && currentAxeAbility != ability) {
            currentAxeAbility = ability
            ConfigHelper.setAxeAbilityName(name)
        }
    }

    fun syncCooldown(type: String, abilityName: String?, timeSeconds: Long) {
        when (type) {
            "Pickaxe" -> if (abilityName != "Pickobulus") pickaxeState.syncCooldown(timeSeconds)
            "Axe" -> axeState.syncCooldown(timeSeconds)
        }
    }

    fun resetPickaxeAbilities() {
        pickaxeState.reset()
    }
}