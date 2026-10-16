package io.github.chindeaone.collectiontracker.config.categories.farmingweight

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FarmingweightConfig {
    @Expose
    @ConfigOption(
        name = "Chat Farming Weight Ranks",
        desc = "Enable Farming Weight ranks in chat.\n§eNote: If you use Skyhanni's chat formatting, make sure you have at least §bPlayer Name §evisible!"
    )
    @ConfigEditorBoolean
    var farmingweightRankingInChat: Boolean = false

    @Expose
    @ConfigOption(
        name = "Nametag Farming Weight Ranks",
        desc = "Display Farming Weight ranks in the player names."
    )
    @ConfigEditorBoolean
    var farmingweightRankInNameTag: Boolean = false

    @Expose
    @ConfigOption(
        name = "Farming Islands Only",
        desc = "Show Farming Weight ranks in chat only on Farming Islands."
    )
    @ConfigEditorBoolean
    var onlyOnFarmingIslands: Boolean = false

    @Expose
    @ConfigOption(name = "Custom Farming Weight Rank Color", desc = "")
    @Accordion
    var farmingweightColorConfig: FarmingweightColorConfig = FarmingweightColorConfig()
}

