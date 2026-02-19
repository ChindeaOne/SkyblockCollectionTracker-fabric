/*
* This Kotlin object was inspired by the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.EntityUtils
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import kotlin.text.contains

object DeployableParser {

    @JvmStatic
    var buff: String = ""
    @JvmStatic
    var buffColor: String = ""
    @JvmStatic
    var remainingTime: String = ""
    @JvmStatic
    var deployablePos: BlockPos? = null
    @JvmStatic
    var isNear: Boolean = false

    private val MINING_DEPLOYABLE = listOf("Dwarven Lantern", "Mithril Lantern", "Titanium Lantern", "Glacite Lantern", "Will-o'-wisp")
    private val TIME_REGEX = Regex("""(\d+)s""")
    private var tickCounter = 0
    private var internalTimerTicks = 0
    private var trackedEntity: ArmorStand? = null

    fun onTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        val level = client.level ?: return
        val player = client.player ?: return

        tickCounter++
        if (tickCounter % 2 != 0) return

        var found: Pair<ArmorStand, String>? = null

        trackedEntity?.let {
            if (it.isAlive && level.getEntity(it.id) != null) {
                val name = it.customName?.string ?: ""
                val keyword = MINING_DEPLOYABLE.find { k -> name.contains(k, ignoreCase = true) }
                if (keyword != null) found = it to keyword
            }
        }

        if (found == null && deployablePos != null) {
            val candidates = EntityUtils.getArmorStandsAround(level, deployablePos!!, 1.0, 5.0)
            found = EntityUtils.findArmorStandByKeywords(candidates, MINING_DEPLOYABLE)
        }

        if (found == null) {
            val candidates = EntityUtils.getEntitiesInRange().asIterable()
            found = EntityUtils.findArmorStandByKeywords(candidates, MINING_DEPLOYABLE)
        }

        val inMineshaft = MiningStatsWidget.currentMiningIsland?.contains("Mineshaft", ignoreCase = true)

        if (found != null) {
            val (entity, detectedBuff) = found
            internalTimerTicks = 0
            trackedEntity = entity
            buff = detectedBuff
            buffColor = getDeployableColor(detectedBuff)
            deployablePos = entity.blockPosition()

            val entityName = entity.customName?.string ?: ""
            val timeMatch = TIME_REGEX.find(entityName)
            remainingTime = timeMatch?.value ?: ""

            val isMineshaftType = buff.equals("Glacite Lantern", ignoreCase = true) || buff.equals("Will-o'-wisp", ignoreCase = true)
            isNear = if (isMineshaftType && inMineshaft == true) true else player.distanceToSqr(entity) <= 900.0

            if ((remainingTime == "0s" || !entity.isAlive) && buff.isNotEmpty()) {
                if (remainingTime == "0s") notifyExpiration()
                reset()
            }
        } else {
            val isMineshaftDeployable = buff.equals("Glacite Lantern", ignoreCase = true) || buff.equals("Will-o'-wisp", ignoreCase = true)
            if (inMineshaft == true && isMineshaftDeployable && buff.isNotEmpty()) {
                isNear = true
                trackedEntity = null

                internalTimerTicks += 2
                if (internalTimerTicks >= 20) {
                    internalTimerTicks = 0
                    val seconds = remainingTime.removeSuffix("s").toIntOrNull() ?: 0
                    if (seconds > 0) {
                        remainingTime = "${seconds - 1}s"
                    } else {
                        notifyExpiration()
                        reset()
                    }
                }
            } else if (buff.isNotEmpty()) {
                reset()
            }
        }
    }

    private fun notifyExpiration() {
        val message = Component.literal("$buffColor$buff §cExpired!")
        RenderUtils.showTitle(message)
    }

    private fun getDeployableColor(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("dwarven") -> "§f"
            lower.contains("mithril") -> "§a"
            lower.contains("titanium") -> "§9"
            lower.contains("glacite") -> "§5"
            lower.contains("wisp") || lower.contains("will-o'-wisp") -> "§6"
            else -> ""
        }
    }

    @JvmStatic
    fun reset() {
        buff = ""
        buffColor = ""
        remainingTime = ""
        tickCounter = 0
        internalTimerTicks = 0
        deployablePos = null
        isNear = false
        trackedEntity = null
    }
}