package io.github.chindeaone.collectiontracker.utils.ability

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class Ability(
    val abilityName: String,
    val pickaxeAbility: Boolean = false,
    val axeAbility: Boolean = false,
    private val cooldownsByLevel: Map<Int, Duration> = emptyMap(),
    private val durationsByLevel: Map<Int, Duration> = emptyMap(),
    private val defaultCooldown: Duration = 120.seconds,
    private val defaultDuration: Duration = Duration.ZERO
) {
    // Pickaxe Abilities
    MINING_SPEED_BOOST(
        abilityName = "Mining Speed Boost",
        pickaxeAbility = true,
        durationsByLevel = mapOf(1 to 10.seconds, 2 to 15.seconds, 3 to 20.seconds),
    ),
    PICKOBULUS(
        abilityName = "Pickobulus",
        pickaxeAbility = true,
        cooldownsByLevel = mapOf(1 to 60.seconds, 2 to 50.seconds, 3 to 40.seconds),
        defaultDuration = Duration.ZERO
    ),
    TUNNEL_VISION(
        abilityName = "Tunnel Vision",
        pickaxeAbility = true,
        cooldownsByLevel = mapOf(1 to 120.seconds, 2 to 110.seconds, 3 to 100.seconds),
        defaultDuration = 30.seconds
    ),
    MANIAC_MINER(
        abilityName = "Maniac Miner",
        pickaxeAbility = true,
        durationsByLevel = mapOf(1 to 25.seconds, 2 to 30.seconds, 3 to 35.seconds)
    ),
    GEMSTONE_INFUSION(
        abilityName = "Gemstone Infusion",
        pickaxeAbility = true,
        durationsByLevel = mapOf(1 to 20.seconds, 2 to 25.seconds, 3 to 30.seconds)
    ),
    SHEER_FORCE(
        abilityName = "Sheer Force",
        pickaxeAbility = true,
        durationsByLevel = mapOf(1 to 20.seconds, 2 to 25.seconds, 3 to 30.seconds)
    ),

    // Axe Abilities
    DAMAGE_BOOST(
        abilityName = "Damage Boost",
        axeAbility = true,
        cooldownsByLevel = mapOf(1 to 120.seconds, 2 to 110.seconds),
        defaultDuration = 10.seconds
    ),
    AXE_TOSS(
        abilityName = "Axe Toss",
        axeAbility = true,
        cooldownsByLevel = mapOf(1 to 120.seconds, 2 to 112.seconds),
        defaultDuration = 10.seconds
    ),
    MANIAC_SLICER(
        abilityName = "Maniac Slicer",
        axeAbility = true,
        cooldownsByLevel = mapOf(1 to 60.seconds, 2 to 58.seconds),
        durationsByLevel = mapOf(1 to 15.seconds, 2 to 20.seconds)
    ),
    UNKNOWN(
        abilityName = "Unknown Ability"
    );

    fun getCooldown(level: Int = 1): Duration = cooldownsByLevel[level] ?: defaultCooldown

    fun getDuration(level: Int = 1): Duration = durationsByLevel[level] ?: defaultDuration

    companion object {
        private val BY_NAME = entries.associateBy { it.abilityName.lowercase() }

        fun fromName(name: String): Ability? = BY_NAME[name.lowercase()]
    }
}