package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.*

class PickaxeAbilityConfig {
    @Expose
    @ConfigOption(
        name = "Cooldown Attribute Level",
        desc = "§eNote: Doesn't need to be manually updated, unless it isn't being detected correctly."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 10f, minStep = 1f)
    var attributeLevel: Int = 0

    @Expose
    @ConfigOption(
        name = "Ability Display",
        desc = "Display current pickaxe ability name and cooldown/duration time."
    )
    @ConfigEditorBoolean
    var displayPickaxeAbility: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ability Indicator",
        desc = "Select a custom ability indicator."
    )
    @ConfigEditorDropdown
    var indicator: Misc.AbilityDisplayIndicator = Misc.AbilityDisplayIndicator.NONE // Default to none

    @Expose
    @ConfigOption(
        name = "Mining Islands Only",
        desc = "Show the pickaxe ability display only on Mining Islands."
    )
    @ConfigEditorBoolean
    var pickaxeAbilityInMiningIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ready Title Notification",
        desc = "Show a title when the ability is ready."
    )
    @ConfigEditorBoolean
    var showPickaxeReadyAbilityTitle: Boolean = true

    @Expose
    @ConfigOption(
        name = "Expired Title Notification",
        desc = "Show a title when the ability expires."
    )
    @ConfigEditorBoolean
    var showPickaxeExpiredAbilityTitle: Boolean = true

    @Expose
    @ConfigLink(owner = PickaxeAbilityConfig::class, field = "displayPickaxeAbility")
    var pickaxeAbilityPosition: Position = Position(500, 150)

    @Expose
    var abilityName: String = ""
}
