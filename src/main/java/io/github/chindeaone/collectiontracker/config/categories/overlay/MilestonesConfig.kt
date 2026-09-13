package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.github.chindeaone.collectiontracker.config.categories.milestones.CollectionMilestonesConfig
import io.github.chindeaone.collectiontracker.config.categories.milestones.Milestone
import io.github.chindeaone.collectiontracker.config.categories.milestones.SkillMilestonesConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MilestonesConfig {

    @ConfigOption(
        name = "Milestones Info",
        desc = "Shows your progress towards your collection and skill milestones."
    )
    @ConfigEditorInfoText
    var info: Boolean = true

    @Expose
    @ConfigOption(name = "Collection Milestones Config", desc = "")
    @SerializedName("collectionMilestonesConfig")
    @Accordion
    var collectionMilestonesConfig: CollectionMilestonesConfig = CollectionMilestonesConfig()

    @Expose
    @ConfigOption(name = "Skill Milestones Config", desc = "")
    @SerializedName("skillMilestonesConfig")
    @Accordion
    var skillMilestonesConfig: SkillMilestonesConfig = SkillMilestonesConfig()

    @Expose
    var milestones: Map<String, Milestone> = emptyMap()
}