package io.github.chindeaone.collectiontracker.config.categories.milestones

import com.google.gson.annotations.Expose

data class Milestone(

    @field:Expose
    var target: Long = 0L,

    @field:Expose
    var isTotal: Boolean = false,

    @field:Expose
    var progress: Long = 0L
)