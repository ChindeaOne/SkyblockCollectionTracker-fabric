package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

object EntityUtils {

    fun getEntitiesInRange(): List<ArmorStand> {
        val mc = Minecraft.getInstance()
        if (!mc.isSameThread) return emptyList()

        val player = mc.player ?: return emptyList()
        val level = mc.level ?: return emptyList()
        val searchBox = player.boundingBox.inflate(30.0)

        return level.getEntitiesOfClass(ArmorStand::class.java, searchBox)
    }

    fun findArmorStandByKeywords(entities: List<ArmorStand>, keywords: List<String>): Pair<ArmorStand, String>? {
        for (entity in entities) {
            val name = entity.customName?.string ?: continue
            val keyword = keywords.find { name.contains(it, ignoreCase = true) } ?: continue
            return entity to keyword
        }
        return null
    }

    fun getArmorStandsAround(level: Level, pos: BlockPos, xz: Double, y: Double): List<ArmorStand> {
        val searchBox = AABB(pos).inflate(xz, y, xz)
        return level.getEntitiesOfClass(ArmorStand::class.java, searchBox)
    }
}