package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PickaxeAbilityConfig {

    @Expose
    @ConfigOption(
            name = "Pickaxe Ability Display",
            desc = "Displays current pickaxe ability and remaining duration while active."
    )
    @ConfigEditorBoolean
    public boolean displayPickaxeAbility = false;

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
    public String lastPet = "";

    @Expose
    @ConfigLink(owner = PickaxeAbilityConfig.class, field = "Pickaxe Ability")
    public Position pickaxeAbilityPosition = new Position(500, 150);
}
