package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB

object EntityUtils {

    fun getArmorStandsAroundPlayer(): List<ArmorStand> {
        val level = MinecraftUtils.level ?: return emptyList()
        val pos = MinecraftUtils.player?.position() ?: return emptyList()

        val radius = 30.0
        val searchBox = AABB(
            pos.x - radius, pos.y - radius, pos.z - radius,
            pos.x + radius, pos.y + radius, pos.z + radius
        )

        return level.getEntitiesOfClass(ArmorStand::class.java, searchBox).filter { it.distanceToSqr(pos) < radius * radius }
    }

    fun isEntityInPlayerRange(entity: ArmorStand): Boolean {
        val pos = MinecraftUtils.player?.position() ?: return false
        return entity.position().distanceToSqr(pos) < 30.0 * 30.0
    }
}