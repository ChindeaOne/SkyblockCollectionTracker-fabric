package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotfConfig {
    @Expose
    @ConfigOption(
        name = "Center Of The Forest Level",
        desc = "Input your Center Of The Forest level.\n§eRequired for more precise axe ability cooldown."
    )
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