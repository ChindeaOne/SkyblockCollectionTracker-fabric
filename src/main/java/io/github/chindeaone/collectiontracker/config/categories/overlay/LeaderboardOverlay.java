package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LeaderboardOverlay {

    @ConfigOption(
            name = "§eLeaderboard Overlay",
            desc = "Shows your collection or skill rank (if eligible), next player and their collection/skill xp, eta until you reach them and progress to the next rank"
    )
    @ConfigEditorInfoText
    public boolean info = true;

    @Expose
    @ConfigOption(
            name = "Collection Leaderboard",
            desc = "Enables leaderboard for collection tracking"
    )
    @ConfigEditorBoolean
    public boolean collectionLeaderboard = false;

    @Expose
    @ConfigOption(
            name = "Skill Leaderboard",
            desc = "Enables leaderboard for skill tracking"
    )
    @ConfigEditorBoolean
    public boolean skillLeaderboard = false;
}
