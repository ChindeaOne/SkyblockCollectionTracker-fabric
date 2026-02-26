package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class Misc {

    public enum TitleScale {
        SMALL(0.5f),
        MEDIUM(1.0f),
        LARGE(2.0f),
        HUGE(3.0f);

        private final float scale;

        TitleScale(float scale) {
            this.scale = scale;
        }

        public float getScale() {
            return scale;
        }
    }

    @Expose
    @ConfigOption(
            name = "Timer precision",
            desc = "Change how many decimals cooldown and duration will show."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 2, minStep = 1)
    public Property<Integer> abilityPrecision = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Title Duration",
            desc = "How long (in seconds) will titles remain on screen."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 8, minStep = 1)
    public Property<Integer> titleDisplayTimer = Property.of(3);

    @Expose
    @ConfigOption(
            name = "Title Scale",
            desc = "Change the scale of titles.\n§eSmall = 0.5x, Medium = 1x, Large = 2x, Huge = 3x"
    )
    @ConfigEditorDropdown
    public TitleScale titleScale = TitleScale.MEDIUM; // Default to MEDIUM

    @ConfigOption(
            name = "Title Position GUI",
            desc = "Edit the position of titles."
    )
    @ConfigEditorButton(buttonText = "Edit")
    @SuppressWarnings("unused")
    public Runnable editTitlePosition = GuiManager::openGuiTitlePositionEditor;

    @Expose
    @ConfigOption(
            name = "Ability Cooldown Only",
            desc = "Only display ability cooldowns."
    )
    @ConfigEditorBoolean
    public boolean abilityCooldownOnly = false;

    @Expose
    @ConfigOption(
            name = "Server Lag Protection",
            desc = "Prevents ability timers from counting down during server lag.\n§eMight desync timers if you swap lobbies a lot!"
    )
    @ConfigEditorBoolean
    public boolean serverLagProtection = false;

    @Expose
    @ConfigLink(owner = Misc.class, field = "Titles")
    public Position titlePosition = new Position(0, 0);
}
