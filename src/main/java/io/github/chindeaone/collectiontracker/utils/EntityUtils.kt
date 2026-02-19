package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

object EntityUtils {

    fun getEntitiesInRange(): Sequence<Entity> {
        val mc = Minecraft.getInstance()
        if (!mc.isSameThread) return emptySequence()

        val player = mc.player ?: return emptySequence()
        val level = mc.level ?: return emptySequence()
        val searchBox = player.boundingBox.inflate(30.0)

        return level.entitiesForRendering().asSequence().filter { entity ->
            entity is ArmorStand &&
                    entity.boundingBox.intersects(searchBox) &&
                    player.hasLineOfSight(entity) &&
                    isLookingAt(player, entity)
        }
    }

    fun findArmorStandByKeywords(entities: Iterable<Entity>, keywords: List<String>): Pair<ArmorStand, String>? {
        for (entity in entities) {
            if (entity is ArmorStand) {
                val name = entity.customName?.string ?: continue
                val keyword = keywords.find { name.contains(it, ignoreCase = true) } ?: continue
                return entity to keyword
            }
        }
        return null
    }

    fun getArmorStandsAround(level: Level, pos: BlockPos, xz: Double, y: Double): List<ArmorStand> {
        val searchBox = AABB(pos).inflate(xz, y, xz)
        return level.getEntitiesOfClass(ArmorStand::class.java, searchBox)
    }

    private fun isLookingAt(player: Entity, target: Entity, threshold: Double = 0.5): Boolean {
        val lookVec = player.lookAngle
        val toTargetVec = target.position().subtract(player.eyePosition).normalize()
        return lookVec.dot(toTargetVec) > threshold
    }
}