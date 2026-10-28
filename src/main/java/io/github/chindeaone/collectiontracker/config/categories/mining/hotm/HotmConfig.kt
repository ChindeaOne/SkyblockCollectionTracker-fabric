package io.github.chindeaone.collectiontracker.config.categories.mining.hotm

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotmConfig {

    @Expose
    var activeLoadoutName: String = "Heart of the Mountain 1"

    @Expose
    var loadouts: LinkedHashMap<String, HotmLoadout> = linkedMapOf(
        "Heart of the Mountain 1" to HotmLoadout(),
        "Heart of the Mountain 2" to HotmLoadout(),
        "Heart of the Mountain 3" to HotmLoadout(),
        "Heart of the Mountain 4" to HotmLoadout(),
        "Heart of the Mountain 5" to HotmLoadout()
    )

    @Expose
    var cotmLevel: Int = 0

    @Expose
    @ConfigOption(
        name = "Compact Mayhem buff",
        desc = "Compact chat messages from Mineshaft Mayhem perk when entering a Mineshaft."
    )
    @ConfigEditorBoolean
    var compactMayhem: Boolean = false

    @Expose
    @ConfigOption(name = "Pickaxe Ability Config", desc = "")
    @Accordion
    var pickaxeAbilityConfig: PickaxeAbilityConfig = PickaxeAbilityConfig()

    @Expose
    @ConfigOption(name = "Sky Mall Config", desc = "")
    @Accordion
    var skyMallConfig: SkyMallConfig = SkyMallConfig()
}