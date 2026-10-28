package io.github.chindeaone.collectiontracker.utils.ability

import io.github.chindeaone.collectiontracker.config.attributeLevel
import io.github.chindeaone.collectiontracker.config.cotfLevel
import io.github.chindeaone.collectiontracker.config.cotmLevel
import io.github.chindeaone.collectiontracker.utils.hotx.HotfData
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

    enum class FallbackType {
        READY,
        EXPIRED,
        COOLDOWN,
        USED,
        CHANGED
    }

    enum class FuelTank(val cooldownReduction: Double) {
        MITHRIL(0.02),
        TITANIUM(0.04),
        GEMSTONE(0.06),
        PERFECTLY_CUT(0.10)
    }

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

    private var pendingPickaxeSnap: AbilitySnapshot? = null

    fun getPickaxeEffectiveLevel(cotmLevel: Int, hasBlegg: Boolean): Int {
        val baseLevel = cotmLevel.coerceIn(1, 2)
        return if (hasBlegg) baseLevel + 1 else baseLevel
    }

    fun getAxeEffectiveLevel(cotfLevel: Int): Int {
        return cotfLevel.coerceIn(1, 2)
    }

    fun calculatePickaxeAbilityReduction(
        baseCooldown: Duration,
        snap: AbilitySnapshot
    ): Duration {
        var multiplier = 1.0

        // Fuel Tank
        if (snap.fuelTank != null) {
            multiplier *= (1.0 - (snap.fuelTank.cooldownReduction))
        }

        if (attributeLevel > 0) {
            multiplier *= (1.0 - attributeLevel / 100f)
        }

        // Sky Mall
        if (HotmData.isSkyMallCooldown && MiningMapping.miningIslands.contains(IslandTracker.currentMiningIsland)) {
            multiplier *= if (HotmData.currentPickaxeAbility == Ability.PICKOBULUS) 0.76 else 0.8 // apparently it's more for pickobulus
        }

        // Mayhem cooldown reduction
        if (IslandTracker.currentMiningIsland == "Mineshaft" && HotmData.isMayhemCooldown) {
            multiplier *= 0.75
        }

        val finalDuration = (baseCooldown.inWholeSeconds.toDouble() * multiplier).coerceAtLeast(0.0)
        return finalDuration.seconds
    }

    fun onClientTick(client: Minecraft) {
        // only for MSB
        if (!pickaxeState.isActive || HotmData.currentPickaxeAbility != Ability.MINING_SPEED_BOOST) return

        val player = client.player ?: return
        val stack = player.mainHandItem

        val tooltips = if (stack.isEmpty) emptyList() else ItemUtils.getTooltipLines(stack, true)

        if (tooltips != lastTooltips) {
            lastTooltips = tooltips
            handleItemChange(tooltips)
        }
    }

    private fun handleItemChange(tooltips: List<String>) {
        val parsedSnap = AbilityItemParser.parse(tooltips)

        if (pickaxeState.isActive) {
            val hasBlegg = parsedSnap?.hasBlueCheesePart ?: false
            val effectiveLevel = getPickaxeEffectiveLevel(cotmLevel, hasBlegg)

            val newDuration = Ability.MINING_SPEED_BOOST.getDuration(effectiveLevel)

            pickaxeState.updateDuration(newDuration)
        }
    }

    fun onItemUse(snap: AbilitySnapshot?) {
        if (snap == null) return

        if (snap.isPickaxe && pickaxeState.isReady) {
            pendingPickaxeSnap = snap
            triggerPickaxeAbility(snap)
        } else if (snap.isAxe && axeState.isReady) {
            triggerAxeAbility()
        }
    }

    fun onChatFallback(
        name: String? = null,
        type: String? = null,
        time: Int? = null,
        fallbackType: FallbackType
    ) {
        when (fallbackType) {
            FallbackType.COOLDOWN -> {
                val abilityType = type ?: return
                val ability = if (abilityType == "Axe") {
                    HotfData.currentAxeAbility
                } else {
                    HotmData.currentPickaxeAbility
                }

                syncCooldown(type, ability, time!!)
            }

            else -> {
                val abilityName = name ?: return
                val ability = Ability.fromName(abilityName) ?: return

                if (ability.pickaxeAbility) {
                    if (HotmData.currentPickaxeAbility != ability) {
                        HotmData.currentPickaxeAbility = ability
                    }

                    when (fallbackType) {
                        FallbackType.READY -> {
                            if (!pickaxeState.isReady && !pickaxeState.wasRecentlyUsed) {
                                pickaxeState.reset()
                            }
                        }

                        FallbackType.EXPIRED -> {
                            if (pickaxeState.isActive) {
                                pickaxeState.syncCooldownOnExpired()
                            }
                        }

                        FallbackType.USED -> {
                            if (!pickaxeState.isReady) {
                                val snap = pendingPickaxeSnap ?: return

                                triggerPickaxeAbility(snap, forced = true)
                                pendingPickaxeSnap = null
                            }
                        }

                        FallbackType.CHANGED -> {}
                    }
                } else if (ability.axeAbility) {
                    if (HotfData.currentAxeAbility != ability) {
                        HotfData.currentAxeAbility = ability
                    }

                    when (fallbackType) {
                        FallbackType.READY -> {
                            if (!axeState.isReady && !axeState.wasRecentlyUsed) {
                                axeState.reset()
                            }
                        }

                        FallbackType.EXPIRED -> {
                            if (axeState.isActive) {
                                axeState.syncCooldownOnExpired()
                            }
                        }

                        FallbackType.USED -> {
                            if (!axeState.isReady) {
                                triggerAxeAbility(forced = true)
                            }
                        }

                        FallbackType.CHANGED -> {}
                    }
                }

            }
        }
    }

    fun triggerPickaxeAbility(snap: AbilitySnapshot, forced: Boolean = false) {
        if (!forced && !pickaxeState.isReady) return

        val ability = HotmData.currentPickaxeAbility
        if (ability == Ability.UNKNOWN || !ability.pickaxeAbility) return

        val effectiveLevel = getPickaxeEffectiveLevel(cotmLevel, snap.hasBlueCheesePart)
        val baseCooldown = ability.getCooldown(effectiveLevel)
        val finalCooldown = calculatePickaxeAbilityReduction(baseCooldown, snap)
        val duration = ability.getDuration(effectiveLevel)

        pickaxeState.start(duration, finalCooldown)
    }

    fun triggerAxeAbility(forced: Boolean = false) {
        if (!forced && !axeState.isReady) return

        val ability = HotfData.currentAxeAbility
        if (ability == Ability.UNKNOWN || !ability.axeAbility) return

        val effectiveLevel = getAxeEffectiveLevel(cotfLevel)
        val cooldown = ability.getCooldown(effectiveLevel)
        val duration = ability.getDuration(effectiveLevel)

        axeState.start(duration, cooldown)
    }

    fun syncCooldown(type: String, ability: Ability, timeSeconds: Int) {
        when (type) {
            "Pickaxe" -> if (ability != Ability.PICKOBULUS) pickaxeState.syncCooldown(timeSeconds)
            "Axe" -> axeState.syncCooldown(timeSeconds)
        }
    }

    fun resetPickaxeAbilities() {
        pickaxeState.reset()
    }
}