package io.github.chindeaone.collectiontracker.config.categories.coleweight;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ColeweightConfig {

    @Expose
    @ConfigOption(
            name = "Coleweight Ranks in chat",
            desc = "Enable Coleweight ranks in chat.\n§eNote: If you use Skyhanni's chat formatting, make sure you have at least §bPlayer Name §evisible!"
    )
    @ConfigEditorBoolean
    public boolean coleweightRankingInChat = false;

    @Expose
    @ConfigOption(
            name = "Mining Islands Only",
            desc = "Show Coleweight ranks in chat only when on the Mining Islands."
    )
    @ConfigEditorBoolean
    public boolean onlyOnMiningIslands = false;

    @Expose
    @ConfigOption(
            name = "Coleweight ability format",
            desc = "Changes ability display and title to use Coleweight's format."
    )
    @ConfigEditorBoolean
    public boolean coleweightAbilityFormat = false;

    @Expose
    @ConfigOption(name = "Custom Coleweight Rank Color", desc = "")
    @Accordion
    public ColeweightColorConfig coleweightColorConfig = new ColeweightColorConfig();

    @Expose
    @ConfigOption(name = "Dwarven Heatmap", desc = "")
    @Accordion
    public HeatmapConfig heatmapConfig = new HeatmapConfig();

    @Expose
    @ConfigOption(name = "Precision Mining", desc = "")
    @Accordion
    public PrecisionMiningConfig precisionMiningConfig = new PrecisionMiningConfig();

    @Expose
    public Position coleweightTimerPosition = new Position(300, 200);

    @Expose
    public Position coleweightStopwatchPosition = new Position(300, 250);

    @Expose
    public Position coleweightTrackerPosition = new Position(400, 200);
}