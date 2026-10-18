package io.github.chindeaone.collectiontracker.config.migration

import com.google.gson.JsonElement
import com.google.gson.JsonObject

object ConfigMigrator {

    const val CURRENT_VERSION = 4

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
                3 -> migrateToVersion3(config)
                4 -> migrateToVersion4(config)
            }
        }

        val internal = config.getAsJsonObject("internal") ?: JsonObject().also { config.add("internal", it) }
        internal.addProperty("configVersion", CURRENT_VERSION)

        return oldVersion < CURRENT_VERSION
    }

    /**
     * Migrate X/Y position fields to lowercase field names.
     */
    private fun migrateToVersion1(config: JsonObject) {
        migratePositions(config)
    }

    /**
     * Preserve previous custom goal amounts.
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

        val leaderboardPositions = JsonObject()

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

            goal["position"]
                ?.takeIf { !it.isJsonNull }
                ?.let {
                    leaderboardPositions.add(collection, it.deepCopy())
                }

            goal["amount"]
                ?.takeIf { !it.isJsonNull }
                ?.let { amount ->
                    val milestone = JsonObject().apply {
                        add("target", amount.deepCopy())
                        addProperty("isTotal", true)
                        addProperty("progress", 0L)
                    }

                    milestones.add(collection, milestone)
                }
        }

        if (leaderboardPositions.size() > 0) {
            leaderboard.add("leaderboardPositions", leaderboardPositions)
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

    /**
     * Changed `Temporary Buff` config to `Mining Consumables` config.
     * Renamed all config fields as well.
     * Removed `refinedCacaoTime`.
     */
    private fun migrateToVersion3(config: JsonObject) {
        val mining = config.getAsJsonObject("mining") ?: return

        val tempBuffConfig = mining.remove("temporaryBuffsConfig")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return

        // rename fields
        tempBuffConfig.remove("enableTempBuffTracker")?.let {
            tempBuffConfig.add("enableConsumableTracker", it)
        }

        tempBuffConfig.remove("showTempBuffExpiredTitle")?.let {
            tempBuffConfig.add("showConsumableExpiredTitle", it)
        }

        tempBuffConfig.remove("tempBuffPosition")?.let {
            tempBuffConfig.add("consumablePosition", it)
        }

        tempBuffConfig.remove("refinedCacaoTime")

        mining.add("consumableConfig", tempBuffConfig)
    }

    /**
     * Changed `explicitValues` to `formattedValues` and inverted the value.
     */
    private fun migrateToVersion4(config: JsonObject) {
        val tracking = config.getAsJsonObject("tracking") ?: return

        tracking.remove("explicitValues")?.let {
            tracking.addProperty("formattedValues", !it.asBoolean)
        }
    }
}