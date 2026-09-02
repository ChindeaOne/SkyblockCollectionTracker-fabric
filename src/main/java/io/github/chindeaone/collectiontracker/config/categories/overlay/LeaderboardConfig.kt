package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LeaderboardConfig {
    @ConfigOption(
        name = "§eLeaderboard Info",
        desc = "Shows your collection or skill rank (if eligible), next player and their collection/skill xp, eta until you pass them and progress to the next rank"
    )
    @ConfigEditorInfoText
    var info: Boolean = true

    @Expose
    @ConfigOption(name = "Collection Leaderboard", desc = "Enables leaderboard for collection tracking")
    @ConfigEditorBoolean
    var collectionLeaderboard: Boolean = false

    @Expose
    @ConfigOption(name = "Skill Leaderboard", desc = "Enables leaderboard for skill tracking")
    @ConfigEditorBoolean
    var skillLeaderboard: Boolean = false

    @Expose
    @ConfigOption(
        name = "Enable Previous Position",
        desc = "Show details about the previous position on the leaderboard."
    )
    @ConfigEditorBoolean
    var previousPosition: Boolean = false

    @Expose
    @ConfigOption(
        name = "Include Wiped Profiles",
        desc = "Leaderboards will include wiped players.\n§eNote: This feature is temporary, until admins will remove wiped players from the API."
    )
    @ConfigEditorBoolean
    var includeWipedProfiles: Boolean = false

    @Expose
    @ConfigOption(
        name = "Enable Custom Position",
        desc = "Enables a custom position for the leaderboard. This will show your progress towards the custom position instead of the next player on the leaderboard"
    )
    @ConfigEditorBoolean
    var customPosition: Boolean = false

    @Expose
    var customPositions: MutableMap<String, Int> = mutableMapOf()
}
