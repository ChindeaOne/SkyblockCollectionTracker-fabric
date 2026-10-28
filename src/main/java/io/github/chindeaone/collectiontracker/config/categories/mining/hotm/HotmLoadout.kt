package io.github.chindeaone.collectiontracker.config.categories.mining.hotm

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.hotx.HotxPerkState

class StrongArmState {
    @Expose
    var state: HotxPerkState = HotxPerkState.UNKNOWN

    @Expose
    var level: Int = 0
}

class ProfessionalState {
    @Expose
    var state: HotxPerkState = HotxPerkState.UNKNOWN

    @Expose
    var level: Int = 0
}

class SkyMallState {
    @Expose
    var state: HotxPerkState = HotxPerkState.UNKNOWN

    @Expose
    var lastSkyMallBuff: String = "§cUnknown"
}

class PickaxeAbilityState {
    @Expose
    var ability: Ability = Ability.UNKNOWN
}

data class HotmLoadout(
    @Expose
    var strongArm: StrongArmState = StrongArmState(),

    @Expose
    var professional: ProfessionalState = ProfessionalState(),

    @Expose
    var pickaxeAbility: PickaxeAbilityState = PickaxeAbilityState(),

    @Expose
    var skyMall: SkyMallState = SkyMallState()
)