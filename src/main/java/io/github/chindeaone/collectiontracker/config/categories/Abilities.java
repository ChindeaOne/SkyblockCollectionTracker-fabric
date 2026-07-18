package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigUtilsKt;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Abilities {

    @ConfigOption(
            name = "Pickaxe Ability Config",
            desc = "Jump to the Pickaxe Ability Config section."
    )
    @ConfigEditorButton(buttonText = "Jump")
    @SuppressWarnings("unused")
    public Runnable jumpToPickaxeAbilityConfig = () -> ConfigUtilsKt.jumpToConfig(ConfigAccess.getPickaxeAbilityPosition());

    @ConfigOption(
            name = "Axe Ability Config",
            desc = "Jump to the Axe Ability Config section."
    )
    @ConfigEditorButton(buttonText = "Jump")
    @SuppressWarnings("unused")
    public Runnable jumpToAxeAbilityConfig = () -> ConfigUtilsKt.jumpToConfig(ConfigAccess.getAxeAbilityPosition());

    @Expose
    @ConfigOption(
            name = "Enable Circle",
            desc = ""
    )
    @ConfigEditorBoolean
    public boolean circle = false;
}
