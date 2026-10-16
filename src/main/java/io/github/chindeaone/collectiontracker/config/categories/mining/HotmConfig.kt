package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotmConfig {
    @ConfigOption(
        name = "§aInfo",
        desc = "§eThese settings don't need to be manually updated, unless they aren't being detected correctly."
    )
    @ConfigEditorInfoText
    val info: Boolean = true

    @Expose
    @ConfigOption(name = "Professional Level", desc = "")
    @ConfigEditorSlider(minValue = 0f, maxValue = 140f, minStep = 1f)
    var professionalMS: Int = 0

    @Expose
    @ConfigOption(name = "Strong Arm Level", desc = "")
    @ConfigEditorSlider(minValue = 0f, maxValue = 100f, minStep = 1f)
    var strongArmMS: Int = 0

    @Expose
    @ConfigOption(name = "Core Of The Mountain Level", desc = "")
    @ConfigEditorSlider(minValue = 0f, maxValue = 10f, minStep = 1f)
    var cotmLevel: Int = 0

    @Expose
    @ConfigOption(name = "Pickaxe Ability Config", desc = "")
    @Accordion
    var pickaxeAbilityConfig: PickaxeAbilityConfig = PickaxeAbilityConfig()

    @Expose
    @ConfigOption(name = "Sky Mall Config", desc = "")
    @Accordion
    var skyMallConfig: SkyMallConfig = SkyMallConfig()
}