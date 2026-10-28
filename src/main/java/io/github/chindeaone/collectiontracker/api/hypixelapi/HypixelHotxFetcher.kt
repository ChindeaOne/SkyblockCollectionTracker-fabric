package io.github.chindeaone.collectiontracker.api.hypixelapi

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.foraging.HotfLoadout
import io.github.chindeaone.collectiontracker.config.categories.mining.hotm.HotmLoadout
import io.github.chindeaone.collectiontracker.config.hotfConfig
import io.github.chindeaone.collectiontracker.config.hotmConfig
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.hotx.HotxPerkState
import io.github.chindeaone.collectiontracker.utils.hotx.RotatingPerk
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

object HypixelHotxFetcher {

    private val logger: Logger = LogManager.getLogger(HypixelHotxFetcher::class.java)

    fun fetchLoadoutData(): CompletableFuture<Void> {
        return ApiManager.requestAsync("${ApiManager.API_URL}/hotx-data", headers())
            .thenAccept { response ->
                when (response.statusCode()) {
                    200 -> processLoadoutResponse(response)
                    404 -> {
                        logger.warn("[SCT]: Hypixel's API unreachable!")
                    }
                    else -> logger.error("[SCT]: Failed to fetch hotx loadout data. HTTP ${response.statusCode()}")
                }
            }
            .exceptionally { e ->
                logger.error("[SCT]: Error while fetching hotx loadout data", e)
                null
            }
    }

    private fun processLoadoutResponse(response: HttpResponse<String>) {
        try {
            val root = JsonParser.parseString(response.body()).asJsonObject

            if (root.has("COTM")) {
                ConfigHelper.setCotmLevel(root.get("COTM").asInt)
            }
            if (root.has("COTF")) {
                ConfigHelper.setCotfLevel(root.get("COTF").asInt)
            }
            if (root.has("PICKAXE_COOLDOWN_LEVEL")) {
                ConfigHelper.setAttributeLevel(root.get("PICKAXE_COOLDOWN_LEVEL").asInt)
            }

            val hotmMap = LinkedHashMap<String, HotmLoadout>()
            for (i in 1..5) {
                val defaultKey = "Heart of the Mountain $i"
                val loadoutObj = root.getAsJsonObject(defaultKey)
                if (loadoutObj != null) {
                    val customName = loadoutObj.get("name")?.asString?.takeIf { it.isNotBlank() } ?: defaultKey
                    val loadout = parseHotmLoadout(loadoutObj)
                    hotmMap[customName] = loadout
                } else {
                    hotmMap[defaultKey] = HotmLoadout()
                }
            }
            hotmConfig.loadouts = hotmMap

            val hotfMap = LinkedHashMap<String, HotfLoadout>()
            for (i in 1..5) {
                val defaultKey = "Heart of the Forest $i"
                val loadoutObj = root.getAsJsonObject(defaultKey)
                if (loadoutObj != null) {
                    val customName = loadoutObj.get("name")?.asString?.takeIf { it.isNotBlank() } ?: defaultKey
                    val loadout = parseHotfLoadout(loadoutObj)
                    hotfMap[customName] = loadout
                } else {
                    hotfMap[defaultKey] = HotfLoadout()
                }
            }
            hotfConfig.loadouts = hotfMap

            if (root.has("SELECTED")) {
                val selectedArray = root.getAsJsonArray("SELECTED")
                if (selectedArray.size() >= 1) {
                    val defaultHotmKey = selectedArray[0].asString
                    val hotmNode = root.getAsJsonObject(defaultHotmKey)
                    val activeHotmName = hotmNode?.get("name")?.asString?.takeIf { it.isNotBlank() } ?: defaultHotmKey
                    hotmConfig.activeLoadoutName = activeHotmName
                }
                if (selectedArray.size() >= 2) {
                    val defaultHotfKey = selectedArray[1].asString
                    val hotfNode = root.getAsJsonObject(defaultHotfKey)
                    val activeHotfName = hotfNode?.get("name")?.asString?.takeIf { it.isNotBlank() } ?: defaultHotfKey
                    hotfConfig.activeLoadoutName = activeHotfName
                }
            }

            ConfigHelper.setHotxDataFetched(true)
            ChatUtils.sendMessage("§eLoaded HotM and HotF data.")
            logger.info("[SCT]: Successfully loaded HotM and HotF loadout data from API.")
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to parse hotx loadout API response", e)
        }
    }

    private fun parseHotmLoadout(obj: JsonObject): HotmLoadout {
        val loadout = HotmLoadout()

        obj.getAsJsonObject("Professional")?.let { perk ->
            val level = perk.get("level")?.asInt ?: 0
            val enabled = perk.get("state")?.asBoolean ?: false
            loadout.professional.level = level
            loadout.professional.state = HotxPerkState.fromApi(level, enabled)
        }

        obj.getAsJsonObject("Strong Arm")?.let { perk ->
            val level = perk.get("level")?.asInt ?: 0
            val enabled = perk.get("state")?.asBoolean ?: false
            loadout.strongArm.level = level
            loadout.strongArm.state = HotxPerkState.fromApi(level, enabled)
        }

        if (obj.has(RotatingPerk.SKY_MALL.name)) {
            val skyMallEnabled = obj.get(RotatingPerk.SKY_MALL.name).asBoolean
            loadout.skyMall.state = if (skyMallEnabled) HotxPerkState.UNLOCKED else HotxPerkState.DISABLED
        }

        obj.get("Ability")?.asString?.let { abilityName ->
            loadout.pickaxeAbility.ability = try {
                Ability.valueOf(abilityName)
            } catch (_: IllegalArgumentException) {
                Ability.fromName(abilityName) ?: Ability.UNKNOWN
            }
        }

        return loadout
    }

    private fun parseHotfLoadout(obj: JsonObject): HotfLoadout {
        val loadout = HotfLoadout()

        if (obj.has(RotatingPerk.LOTTERY.name)) {
            val lotteryEnabled = obj.get(RotatingPerk.LOTTERY.name).asBoolean
            loadout.lottery.state = if (lotteryEnabled) HotxPerkState.UNLOCKED else HotxPerkState.DISABLED
        }

        if (obj.has(RotatingPerk.BEEKEEPER.name)) {
            val beekeeperEnabled = obj.get(RotatingPerk.BEEKEEPER.name).asBoolean
            loadout.beekeeper.state = if (beekeeperEnabled) HotxPerkState.UNLOCKED else HotxPerkState.DISABLED
        }

        obj.get("Ability")?.asString?.let { abilityName ->
            loadout.axeAbility.ability = try {
                Ability.valueOf(abilityName)
            } catch (_: IllegalArgumentException) {
                Ability.fromName(abilityName) ?: Ability.UNKNOWN
            }
        }

        return loadout
    }

    private fun headers() = mapOf(
        "Authorization" to "Bearer ${TokenManager.token}",
        "X-UUID" to PlayerData.playerUUID
    )
}