package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardConfig {

    @ConfigOption(
            name = "§eLeaderboard Overlay",
            desc = "Shows your collection or skill rank (if eligible), next player and their collection/skill xp, eta until you pass them and progress to the next rank"
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

    @Expose
    @ConfigOption(
            name = "Enable Previous Position",
            desc = "Show details about the previous position on the leaderboard."
    )
    @ConfigEditorBoolean
    public boolean previousPosition = false;

    @Expose
    @ConfigOption(
            name = "Enable Custom Position",
            desc = "Enables a custom position for the leaderboard. This will show your progress towards the custom position instead of the next player on the leaderboard"
    )
    @ConfigEditorBoolean
    public boolean customPosition = false;

    @Expose
    public Map<String, Integer> customPositions = new HashMap<>();
}
