package io.github.chindeaone.collectiontracker.config.categories.coleweight

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ColeweightConfig {
    @Expose
    @ConfigOption(
        name = "Coleweight Ranks in chat",
        desc = "Enable Coleweight ranks in chat.\n§eNote: If you use Skyhanni's chat formatting, make sure you have at least §bPlayer Name §evisible!"
    )
    @ConfigEditorBoolean
    var coleweightRankingInChat: Boolean = false

    @Expose
    @ConfigOption(name = "Coleweight Ranks in name tag", desc = "Displays Coleweight ranks in the name tag of players.")
    @ConfigEditorBoolean
    var coleweightRankInNameTag: Boolean = false

    @Expose
    @ConfigOption(name = "Mining Islands Only", desc = "Show Coleweight ranks in chat only when on the Mining Islands.")
    @ConfigEditorBoolean
    var onlyOnMiningIslands: Boolean = false

    @Expose
    @ConfigOption(name = "Custom Coleweight Rank Color", desc = "")
    @Accordion
    var coleweightColorConfig: ColeweightColorConfig = ColeweightColorConfig()

    @Expose
    @ConfigOption(name = "Dwarven Heatmap", desc = "")
    @Accordion
    var heatmapConfig: HeatmapConfig = HeatmapConfig()

    @Expose
    @ConfigOption(name = "Precision Mining", desc = "")
    @Accordion
    var precisionMiningConfig: PrecisionMiningConfig = PrecisionMiningConfig()

    @Expose
    var coleweightTimerPosition: Position = Position(300, 200)

    @Expose
    var coleweightStopwatchPosition: Position = Position(300, 250)

    @Expose
    var coleweightTrackerPosition: Position = Position(400, 200)
}