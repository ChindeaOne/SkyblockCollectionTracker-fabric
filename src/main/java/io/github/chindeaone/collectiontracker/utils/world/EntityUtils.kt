package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object EntityUtils {

    fun getArmorStandsAroundPlayer(level: Level, pos: Vec3, radius: Double): List<ArmorStand> {
        val searchBox = AABB(
            pos.x - radius, pos.y - radius, pos.z - radius,
            pos.x + radius, pos.y + radius, pos.z + radius
        )

        return level.getEntitiesOfClass(ArmorStand::class.java, searchBox)
            .filter { it.distanceToSqr(pos) <= radius * radius }
    }
}