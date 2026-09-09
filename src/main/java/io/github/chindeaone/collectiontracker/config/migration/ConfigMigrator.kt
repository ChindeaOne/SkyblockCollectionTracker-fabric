package io.github.chindeaone.collectiontracker.config.migration

import com.google.gson.JsonElement
import com.google.gson.JsonObject

object ConfigMigrator {

    const val CURRENT_VERSION = 2

    fun migrate(config: JsonObject): Boolean {
        val oldVersion = config
            .getAsJsonObject("internal")
            ?.get("configVersion")
            ?.asInt
            ?: 0

        var currentVersion = oldVersion

        while (currentVersion < CURRENT_VERSION) {
            when (++currentVersion) {
                1 -> migrateToVersion1(config)
                2 -> migrateToVersion2(config)
            }
        }

        config.getAsJsonObject("internal") ?: JsonObject().also { config.add("internal", it) }
            .addProperty("configVersion", CURRENT_VERSION)

        return oldVersion < CURRENT_VERSION
    }

    /**
     * Migrate X/Y position fields to lowercase field names.
     */
    private fun migrateToVersion1(config: JsonObject) {
        migratePositions(config)
    }

    /**
     * Preserve custom goal amounts set in the old config.
     * They now live in the milestone config.
     */
    private fun migrateToVersion2(config: JsonObject) {
        val tracking = config.getAsJsonObject("tracking") ?: return

        val leaderboard = tracking
            .getAsJsonObject("leaderboardConfig")
            ?: return

        val oldGoals = leaderboard
            .remove("customGoals")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return

        val customPositions = JsonObject()

        val milestonesConfig = tracking
            .getAsJsonObject("milestonesConfig")
            ?: JsonObject().also {
                tracking.add("milestonesConfig", it)
            }

        val milestones = milestonesConfig
            .getAsJsonObject("milestones")
            ?: JsonObject().also {
                milestonesConfig.add("milestones", it)
            }

        for ((collection, value) in oldGoals.entrySet()) {
            val goal = value.asJsonObject

            println("$collection: $goal")

            goal["position"]
                ?.takeIf { !it.isJsonNull }
                ?.let {
                    customPositions.add(collection, it.deepCopy())
                }

            goal["amount"]
                ?.takeIf { !it.isJsonNull }
                ?.let {
                    milestones.add(collection, it.deepCopy())
                }
        }

        if (customPositions.size() > 0) {
            leaderboard.add("customPositions", customPositions)
        }
    }

    private fun migratePositions(element: JsonElement) {
        when {
            element.isJsonObject -> {
                val obj = element.asJsonObject

                if (obj.has("X") || obj.has("Y")) {
                    if (!obj.has("x") && obj.has("X")) {
                        obj.add("x", obj.remove("X"))
                    }

                    if (!obj.has("y") && obj.has("Y")) {
                        obj.add("y", obj.remove("Y"))
                    }
                }

                obj.entrySet().forEach { (_, child) -> migratePositions(child) }
            }

            element.isJsonArray -> element.asJsonArray.forEach(::migratePositions)
        }
    }
}