package io.github.chindeaone.collectiontracker.config.categories.milestones

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SkillMilestonesConfig {

    @Expose
    @ConfigOption(
        name = "Skill Milestones Overlay",
        desc = "Enables the milestones overlay during skill tracking."
    )
    @ConfigEditorBoolean
    var skillMilestones: Boolean = false

    @Expose
    @ConfigOption(
        name = "Title Notification",
        desc = "Send a title when you reach a skill milestone."
    )
    @ConfigEditorBoolean
    var skillMilestonesTitleNotification: Boolean = false

    @Expose
    @ConfigOption(
        name = "Sound Notification",
        desc = "Play a sound when you reach a skill milestone."
    )
    @ConfigEditorBoolean
    var skillMilestonesSoundNotification: Boolean = false

    @Expose
    @ConfigLink(owner = SkillMilestonesConfig::class, field = "skillMilestones")
    var skillMilestonesPosition: Position = Position(250, 100)
}