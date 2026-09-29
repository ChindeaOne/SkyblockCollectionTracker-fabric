package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.enableHeatmap
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.awt.Color

object DwarvenHeatmap {

    private data class HeatmapHighlight(val pos: BlockPos, val color: Color)

    private val trackedBlocks = setOf(
        Blocks./*? if 26.2 {*/ /*DYED_TERRACOTTA.brown *//*?} else {*/ BROWN_TERRACOTTA /*?}*/,
        Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.CLAY
    )

    private var cachedHighlights: List<HeatmapHighlight> = emptyList()

    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % 4L != 0L) return

        if (!enableHeatmap || !ScoreboardUtils.isColdStatRelevant()) {
            if (cachedHighlights.isNotEmpty()) cachedHighlights = emptyList()
            return
        }

        val world = client.level ?: return
        val player = client.player ?: return
        val playerPos = player.blockPosition()

        val mutablePos = BlockPos.MutableBlockPos()
        val list = mutableListOf<HeatmapHighlight>()

        for (x in playerPos.x - 5..playerPos.x + 5) {
            for (y in playerPos.y - 1..playerPos.y + 6) {
                for (z in playerPos.z - 5..playerPos.z + 5) {
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

        for (highlight in cachedHighlights) {
            BlockOutline.renderBlockHighlight(highlight.pos, camera, highlight.color)
        }
    }

    private fun priorityColor(block: Block): Color {
        return when (block) {
            Blocks.SMOOTH_RED_SANDSTONE, Blocks.CLAY -> Color(0, 100, 0)
            Blocks./*? if 26.2 {*/ /*DYED_TERRACOTTA.brown *//*?} else {*/ BROWN_TERRACOTTA /*?}*/ -> Color(144, 238, 144)
            else -> Color(0, 255, 0)
        }
    }

    private fun isBlockExposed(world: ClientLevel, pos: BlockPos): Boolean {
        fun isNotSolid(pos: BlockPos): Boolean {
            val state = world.getBlockState(pos)
            return state.isAir || state.block == Blocks.SNOW || state.block == Blocks./*? if 26.2 {*/ /*CARPET.lightGray *//*?} else {*/ LIGHT_GRAY_CARPET /*?}*/
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