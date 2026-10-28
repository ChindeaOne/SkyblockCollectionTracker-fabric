package io.github.chindeaone.collectiontracker.config.migration

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.utils.ability.Ability

object ConfigMigrator {

    const val CURRENT_VERSION = 5

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
                5 -> migrateToVersion5(config)
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

    /**
     * Migrate to the new tree loadout system
     */
    private fun migrateToVersion5(config: JsonObject) {
        val mining = config.getAsJsonObject("mining")
        val hotm = mining?.getAsJsonObject("hotmConfig")
        if (hotm != null) {
            val profMs = hotm.remove("professionalMS")?.asInt ?: 0
            val strongArmMs = hotm.remove("strongArmMS")?.asInt ?: 0

            val skyMall = hotm.getAsJsonObject("skyMallConfig")
            val lastSkyMall = skyMall?.remove("lastSkyMallBuff")?.asString ?: ""

            val pickaxeAbility = hotm.getAsJsonObject("pickaxeAbilityConfig")
            val abilityName = pickaxeAbility?.remove("abilityName")?.asString ?: "Unknown Ability"
            val parsedPickaxeAbility = Ability.fromName(abilityName) ?: Ability.UNKNOWN

            val initialHotmLoadout = JsonObject().apply {
                add("professional", JsonObject().apply {
                    val profLevel = if (profMs > 0) profMs / 5 - 50 else 0
                    addProperty("state", if (profLevel > 0) "UNLOCKED" else "UNKNOWN")
                    addProperty("level", profLevel)
                })
                add("strongArm", JsonObject().apply {
                    val strongArmLevel = if (strongArmMs > 0) strongArmMs / 5 else 0
                    addProperty("state", if (strongArmLevel > 0) "UNLOCKED" else "UNKNOWN")
                    addProperty("level", strongArmLevel)
                })
                add("skyMall", JsonObject().apply {
                    addProperty("state", if (lastSkyMall.isNotBlank()) "UNLOCKED" else "UNKNOWN")
                    addProperty("lastSkyMallBuff", lastSkyMall)
                })
                add("pickaxeAbility", JsonObject().apply {
                    addProperty("ability", parsedPickaxeAbility.name)
                })
            }

            val loadouts = JsonObject().apply {
                add("Heart of the Mountain 1", initialHotmLoadout)
            }
            hotm.addProperty("activeLoadoutName", "Heart of the Mountain 1")
            hotm.add("loadouts", loadouts)
        }

        val foraging = config.getAsJsonObject("foraging")
        val hotf = foraging?.getAsJsonObject("hotfConfig")
        if (hotf != null) {
            val lottery = hotf.getAsJsonObject("lotteryConfig")
            val lastLottery = lottery?.remove("lastLotteryBuff")?.asString ?: ""

            val beekeeper = hotf.getAsJsonObject("beekeeperConfig")
            val lastBeekeeper = beekeeper?.remove("lastBeekeeperBuff")?.asString ?: ""

            val axeAbility = hotf.getAsJsonObject("axeAbilityConfig")
            val abilityName = axeAbility?.remove("abilityNameAxe")?.asString ?: "UNKNOWN"
            val parsedAxeAbility = Ability.fromName(abilityName) ?: Ability.UNKNOWN

            val initialHotfLoadout = JsonObject().apply {
                add("lottery", JsonObject().apply {
                    addProperty("state", if (lastLottery.isNotBlank()) "UNLOCKED" else "UNKNOWN")
                    addProperty("lastLotteryBuff", lastLottery)
                })
                add("beekeeper", JsonObject().apply {
                    addProperty("state", if (lastBeekeeper.isNotBlank()) "UNLOCKED" else "UNKNOWN")
                    addProperty("lastBeekeeperBuff", lastBeekeeper)
                })
                add("axeAbility", JsonObject().apply {
                    addProperty("ability", parsedAxeAbility.name)
                })
            }

            val loadouts = JsonObject().apply {
                add("Heart of the Forest 1", initialHotfLoadout)
            }
            hotf.addProperty("activeLoadoutName", "Heart of the Forest 1")
            hotf.add("loadouts", loadouts)
        }
    }
}