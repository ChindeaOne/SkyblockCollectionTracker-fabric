package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class AxeAbilityConfig {
    @Expose
    @ConfigOption(
        name = "Ability Display",
        desc = "Display current axe ability name and cooldown/duration time."
    )
    @ConfigEditorBoolean
    var displayAxeAbility: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ability Indicator",
        desc = "Select a custom ability indicator."
    )
    @ConfigEditorDropdown
    var indicator: Misc.AbilityDisplayIndicator = Misc.AbilityDisplayIndicator.NONE // Default to none

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Show the axe ability display only on Foraging Islands."
    )
    @ConfigEditorBoolean
    var axeAbilityInForagingIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ready Title Notification",
        desc = "Show a title when the ability is ready."
    )
    @ConfigEditorBoolean
    var showAxeReadyAbilityTitle: Boolean = true

    @Expose
    @ConfigOption(
        name = "Expired Title Notification",
        desc = "Show a title when the ability expires."
    )
    @ConfigEditorBoolean
    var showAxeExpiredAbilityTitle: Boolean = true

    @Expose
    @ConfigLink(owner = AxeAbilityConfig::class, field = "displayAxeAbility")
    var axeAbilityPosition: Position = Position(500, 200)

    @Expose
    var abilityNameAxe: String = ""
}
