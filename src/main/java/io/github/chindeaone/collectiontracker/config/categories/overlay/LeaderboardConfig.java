package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardConfig {

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

    @Expose
    @ConfigOption(
            name = "Enable Custom Goal",
            desc = "Enables a custom goal for the leaderboard overlay. This will show your progress towards the custom goal instead of the next player on the leaderboard"
    )
    @ConfigEditorBoolean
    public boolean customGoal = false;

    @Expose
    @ConfigOption(
            name = "Custom Goal Type",
            desc = "Select whether your custom goals use position-based or amount-based targets\n§ePosition: Target a specific rank\n§eAmount: Target a specific collection/skill amount"
    )
    @ConfigEditorDropdown
    public CustomGoalType customGoalType = CustomGoalType.AMOUNT;

    @Expose
    public Map<String, CustomGoalEntry> customGoals = new HashMap<>();

    public static class CustomGoalEntry {
        @Expose
        public Integer position;

        @Expose
        public Long amount;

        public CustomGoalEntry(Integer position, Long amount) {
            this.position = position;
            this.amount = amount;
        }
    }

    public enum CustomGoalType {
        POSITION,
        AMOUNT
    }
}
