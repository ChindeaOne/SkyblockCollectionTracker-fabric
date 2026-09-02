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
    @ConfigOption(name = "Ability Display", desc = "Displays current axe ability and remaining duration while active.")
    @ConfigEditorBoolean
    var displayAxeAbility: Boolean = false

    @Expose
    @ConfigOption(name = "Ability Indicator", desc = "Select a custom ability indicator.")
    @ConfigEditorDropdown
    var indicator: Misc.AbilityDisplayIndicator = Misc.AbilityDisplayIndicator.NONE // Default to none

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Allows the axe ability display to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    var axeAbilityInForagingIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(name = "Show Title", desc = "Shows a title when axe ability is ready.")
    @ConfigEditorBoolean
    var showAxeReadyAbilityTitle: Boolean = true

    @Expose
    @ConfigOption(name = "Show Expired Title", desc = "Shows a title when axe ability expires.")
    @ConfigEditorBoolean
    var showAxeExpiredAbilityTitle: Boolean = true

    @Expose
    var abilityNameAxe: String = ""

    @Expose
    @ConfigLink(owner = AxeAbilityConfig::class, field = "displayAxeAbility")
    var axeAbilityPosition: Position = Position(500, 200)
}
