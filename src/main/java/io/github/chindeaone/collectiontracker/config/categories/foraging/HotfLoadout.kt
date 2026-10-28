package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.hotx.HotxPerkState

class LotteryState {
    @Expose
    var state: HotxPerkState = HotxPerkState.UNKNOWN

    @Expose
    var lastLotteryBuff: String = "§cUnknown"
}

class BeekeeperState {
    @Expose
    var state: HotxPerkState = HotxPerkState.UNKNOWN

    @Expose
    var lastBeekeeperBuff: String = "§cUnknown"
}

class AxeAbilityState {
    @Expose
    var ability: Ability = Ability.UNKNOWN
}

data class HotfLoadout(
    @Expose
    var lottery: LotteryState = LotteryState(),

    @Expose
    var beekeeper: BeekeeperState = BeekeeperState(),

    @Expose
    var axeAbility: AxeAbilityState = AxeAbilityState()
)