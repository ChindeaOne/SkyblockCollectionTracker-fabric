package io.github.chindeaone.collectiontracker.config.categories.coleweight;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Coleweight {

    @Expose
    @ConfigOption(
            name = "Coleweight ranks in chat",
            desc = "Enable Coleweight ranks in chat"
    )
    @ConfigEditorBoolean
    public boolean coleweightRankingInChat = false;

    @Expose
    @ConfigOption(
            name = "Only on Mining Islands",
            desc = "Only show Coleweight ranks in chat when on the Mining Islands"
    )
    @ConfigEditorBoolean
    public boolean onlyOnMiningIslands = false;
}