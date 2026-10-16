package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotfConfig {
    @ConfigOption(
        name = "§aInfo",
        desc = "§eThese settings don't need to be manually updated, unless they aren't being detected correctly."
    )
    @ConfigEditorInfoText
    val info: Boolean = true

    @Expose
    @ConfigOption(name = "Center Of The Forest Level", desc = "")
    @ConfigEditorSlider(minValue = 0f, maxValue = 5f, minStep = 1f)
    var cotfLevel: Int = 0

    @Expose
    @ConfigOption(name = "Axe Ability Config", desc = "")
    @Accordion
    var axeAbilityConfig: AxeAbilityConfig = AxeAbilityConfig()

    @Expose
    @ConfigOption(name = "Lottery Config", desc = "")
    @Accordion
    var lotteryConfig: LotteryConfig = LotteryConfig()

    @Expose
    @ConfigOption(name = "Beekeeper Config", desc = "")
    @Accordion
    var beekeeperConfig: BeekeeperConfig = BeekeeperConfig()
}