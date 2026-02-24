package io.github.chindeaone.collectiontracker.config.categories.foraging;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class AxeAbilityConfig {

    @Expose
    @ConfigOption(
            name = "Axe Ability Display",
            desc = "Displays current axe ability and remaining duration while active."
    )
    @ConfigEditorBoolean
    public boolean displayAxeAbility = false;

    @Expose
    @ConfigOption(
            name = "Foraging Islands Only",
            desc = "Allows the axe ability display to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    public boolean axeAbilityInForagingIslandsOnly = false;

    @Expose
    @ConfigOption(
            name = "Show Title",
            desc = "Shows a title when axe ability is ready."
    )
    @ConfigEditorBoolean
    public boolean showAxeReadyAbilityTitle = true;

    @Expose
    @ConfigOption(
            name = "Show Expired Title",
            desc = "Shows a title when axe ability expires."
    )
    @ConfigEditorBoolean
    public boolean showAxeExpiredAbilityTitle = true;

    @Expose
    public String abilityNameAxe = "";

    @Expose
    @ConfigLink(owner = AxeAbilityConfig.class, field = "Axe Ability")
    public Position axeAbilityPosition = new Position(500, 200);
}
