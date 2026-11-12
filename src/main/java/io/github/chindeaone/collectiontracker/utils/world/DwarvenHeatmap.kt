package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.enableHeatmap
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object DwarvenHeatmap {

    private data class HeatmapHighlight(val pos: BlockPos, val color: Int)

    private val trackedBlocks = setOf(
        Blocks.DYED_TERRACOTTA.brown,
        Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.CLAY
    )

    private var cachedHighlights: List<HeatmapHighlight> = emptyList()

    fun onClientTick() {
        if (ModLoader.clientTicks % 4L != 0L) return

        if (!enableHeatmap || !ScoreboardUtils.isColdStatRelevant()) {
            if (cachedHighlights.isNotEmpty()) cachedHighlights = emptyList()
            return
        }

        val world = MinecraftUtils.level ?: return
        val player = MinecraftUtils.player ?: return
        val playerPos = player.blockPosition()

        val mutablePos = BlockPos.MutableBlockPos()
        val list = mutableListOf<HeatmapHighlight>()

        for (x in playerPos.x - 7..playerPos.x + 7) {
            for (y in playerPos.y - 1..playerPos.y + 7) {
                for (z in playerPos.z - 7..playerPos.z + 7) {
                    mutablePos.set(x, y, z)

                    val block = world.getBlockState(mutablePos).block

                    if (block !in trackedBlocks) continue
                    if (!isBlockExposed(world, mutablePos)) continue

                    val blockColor = priorityColor(block)
                    list.add(HeatmapHighlight(mutablePos.immutable(), blockColor))
                }
            }
        }
        cachedHighlights = list
    }

    fun render(context: LevelRenderContext) {
        val camera = context.levelState().cameraRenderState

        for ((pos, color) in cachedHighlights) {
            BlockOutline.renderBlockHighlight(pos, camera, color)
        }
    }

    private fun priorityColor(block: Block): Int {
        return when (block) {
            Blocks.SMOOTH_RED_SANDSTONE, Blocks.CLAY -> Colors.DARK_GREEN.color
            Blocks.DYED_TERRACOTTA.brown -> Colors.GREEN.color
            else -> Colors.GREEN.color
        }
    }

    private fun isBlockExposed(world: ClientLevel, pos: BlockPos): Boolean {
        fun isNotSolid(pos: BlockPos): Boolean {
            val state = world.getBlockState(pos)
            return state.isAir || state.block == Blocks.SNOW || state.block == Blocks.CARPET.lightGray
        }

        if (isNotSolid(pos.above())) return true
        if (isNotSolid(pos.below())) return true
        if (isNotSolid(pos.north())) return true
        if (isNotSolid(pos.south())) return true
        if (isNotSolid(pos.east())) return true
        if (isNotSolid(pos.west())) return true

        return false
    }
}