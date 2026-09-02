package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.*

class PickaxeAbilityConfig {
    @Expose
    @ConfigOption(
        name = "Ability Display",
        desc = "Displays current pickaxe ability and remaining duration while active."
    )
    @ConfigEditorBoolean
    var displayPickaxeAbility: Boolean = false

    @Expose
    @ConfigOption(name = "Ability Indicator", desc = "Select a custom ability indicator.")
    @ConfigEditorDropdown
    var indicator: Misc.AbilityDisplayIndicator = Misc.AbilityDisplayIndicator.NONE // Default to none

    @Expose
    @ConfigOption(
        name = "Mining Islands Only",
        desc = "Allows the pickaxe ability display to be rendered only in Mining Islands."
    )
    @ConfigEditorBoolean
    var pickaxeAbilityInMiningIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(name = "Show Ready Title", desc = "Shows a title when pickaxe ability is ready.")
    @ConfigEditorBoolean
    var showPickaxeReadyAbilityTitle: Boolean = true

    @Expose
    @ConfigOption(name = "Show Expired Title", desc = "Shows a title when pickaxe ability expires.")
    @ConfigEditorBoolean
    var showPickaxeExpiredAbilityTitle: Boolean = true

    @Expose
    var abilityName: String = ""

    @Expose
    @ConfigOption(
        name = "Cooldown Attribute Level",
        desc = "Manually set the attribute level for cooldown calculation."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 10f, minStep = 1f)
    var attributeLevel: Int = 0

    @Expose
    @ConfigLink(owner = PickaxeAbilityConfig::class, field = "displayPickaxeAbility")
    var pickaxeAbilityPosition: Position = Position(500, 150)
}
