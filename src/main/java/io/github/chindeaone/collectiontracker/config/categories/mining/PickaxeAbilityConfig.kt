package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.Misc;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.*;

public class PickaxeAbilityConfig {

    @Expose
    @ConfigOption(
            name = "Ability Display",
            desc = "Displays current pickaxe ability and remaining duration while active."
    )
    @ConfigEditorBoolean
    public boolean displayPickaxeAbility = false;

    @Expose
    @ConfigOption(
            name = "Ability Indicator",
            desc = "Select a custom ability indicator."
    )
    @ConfigEditorDropdown
    public Misc.AbilityDisplayIndicator indicator = Misc.AbilityDisplayIndicator.NONE; // Default to none

    @Expose
    @ConfigOption(
            name = "Mining Islands Only",
            desc = "Allows the pickaxe ability display to be rendered only in Mining Islands."
    )
    @ConfigEditorBoolean
    public boolean pickaxeAbilityInMiningIslandsOnly = false;

    @Expose
    @ConfigOption(
            name = "Show Ready Title",
            desc = "Shows a title when pickaxe ability is ready."
    )
    @ConfigEditorBoolean
    public boolean showPickaxeReadyAbilityTitle = true;

    @Expose
    @ConfigOption(
            name = "Show Expired Title",
            desc = "Shows a title when pickaxe ability expires."
    )
    @ConfigEditorBoolean
    public boolean showPickaxeExpiredAbilityTitle = true;

    @Expose
    public String abilityName = "";

    @Expose
    @ConfigOption(
            name = "Cooldown Attribute Level",
            desc = "Manually set the attribute level for cooldown calculation."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
    public int attributeLevel = 0;

    @Expose
    @ConfigLink(owner = PickaxeAbilityConfig.class, field = "displayPickaxeAbility")
    public Position pickaxeAbilityPosition = new Position(500, 150);
}
