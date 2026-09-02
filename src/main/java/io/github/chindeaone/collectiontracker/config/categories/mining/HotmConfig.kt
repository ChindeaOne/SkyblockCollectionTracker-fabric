package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HotmConfig {
    @Expose
    @ConfigOption(name = "Professional Mining Speed", desc = "Input your mining speed from Professional perk.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 755f, minStep = 1f)
    var professionalMS: Int = 0

    @Expose
    @ConfigOption(name = "Strong Arm Mining Speed", desc = "Input your mining speed from Strong Arm perk.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 505f, minStep = 1f)
    var strongArmMS: Int = 0

    @Expose
    @ConfigOption(
        name = "Core Of The Mountain Level",
        desc = "Input your Core Of The Mountain level.\n§eRequired for more precise pickaxe ability cooldown."
    )
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