package io.github.chindeaone.collectiontracker.utils.world

import com.google.gson.JsonObject
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object WaypointsUtils {

    private val waypointCategories = mutableMapOf<String, List<Pair<String, BlockPos>>>()
    var currentCategory: String? = null
    var currentIndex = 0

    enum class MineshaftTypes {
        TOPA_1,
        TOPA_2,
        SAPP_1,
        SAPP_2,
        AMET_1,
        AMET_2,
        AMBE_1,
        AMBE_2,
        JADE_1,
        JADE_2,
        TITA_1,
        UMBE_1,
        TUNG_1,
        FAIR_1,
        RUBY_1,
        RUBY_2,
        RUBY_C,
        ONYX_1,
        ONYX_2,
        ONYX_C,
        AQUA_1,
        AQUA_2,
        AQUA_C,
        CITR_1,
        CITR_2,
        CITR_C,
        PERI_1,
        PERI_2,
        PERI_C,
        JASP_1,
        JASP_C,
        OPAL_1,
        OPAL_C;
    }

    @JvmStatic
    fun setWaypoints(data: JsonObject) {
        waypointCategories.clear()
        data.keySet().forEach { categoryName ->
            val obj = data.getAsJsonObject(categoryName)
            val sortedPos = obj.keySet().map { key ->
                    val pos = obj.getAsJsonObject(key)
                    val blockPos = BlockPos(
                        pos.get("x").asInt,
                        pos.get("y").asInt,
                        pos.get("z").asInt
                    )
                    Pair(key, blockPos)
                }
            waypointCategories[categoryName] = sortedPos
        }
    }

    fun selectCategory(category: String) {
        if (currentCategory == category) return
        if (waypointCategories.containsKey(category)) {
            currentCategory = category
            currentIndex = 0
        }
    }

    fun updateCurrentIndex() {
        val category = currentCategory ?: return
        val list = waypointCategories[category] ?: return
        val shouldCycle = category == ConfigAccess.getPureOresRoutes().type

        while (currentIndex < list.size && isPlayerNear(list[currentIndex].second)) {
            currentIndex++

            if (shouldCycle && currentIndex >= list.size) {
                currentIndex = 0
            }
        }
    }

    private fun isPlayerNear(pos: BlockPos): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val targetVec = Vec3.atCenterOf(pos)
        return player.position().closerThan(targetVec, 3.0)
    }

    fun getWaypointsForCategory(category: String): List<Pair<String, BlockPos>> =
        waypointCategories[category] ?: emptyList()

    fun reset() {
        currentIndex = 0
    }
}