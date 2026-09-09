package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose

class MilestonesConfig {

    @Expose
    var milestones: MutableMap<String, Long> = mutableMapOf()
}