package io.github.chindeaone.collectiontracker.config.categories.foraging;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HotfConfig {

    @Expose
    @ConfigOption(
            name = "Center Of The Forest Level",
            desc = "Input your Center Of The Forest level.\n§eRequired for more precise axe ability cooldown."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 5, minStep = 1)
    public int cotfLevel = 0;

    @Expose
    @ConfigOption(name = "Axe Ability Config", desc = "")
    @Accordion
    public AxeAbilityConfig axeAbilityConfig = new AxeAbilityConfig();

    @Expose
    @ConfigOption(name = "Lottery Config", desc = "")
    @Accordion
    public LotteryConfig lotteryConfig = new LotteryConfig();

    @Expose
    @ConfigOption(name = "Beekeeper Config", desc = "")
    @Accordion
    public BeekeeperConfig beekeeperConfig = new BeekeeperConfig();
}