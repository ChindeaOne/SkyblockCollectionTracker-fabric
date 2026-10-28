package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotfConfig {

    @Expose
    var activeLoadoutName: String = "Heart of the Forest 1"

    @Expose
    var loadouts: LinkedHashMap<String, HotfLoadout> = linkedMapOf(
        "Heart of the Forest 1" to HotfLoadout(),
        "Heart of the Forest 2" to HotfLoadout(),
        "Heart of the Forest 3" to HotfLoadout(),
        "Heart of the Forest 4" to HotfLoadout(),
        "Heart of the Forest 5" to HotfLoadout()
    )

    @Expose
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