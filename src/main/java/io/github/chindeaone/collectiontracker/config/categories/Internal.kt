package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.migration.ConfigMigrator

class Internal {

    @Expose
    @Suppress("unused")
    var configVersion: Int = ConfigMigrator.CURRENT_VERSION
}