package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object DwarvenHeatmap {

    private data class HeatmapHighlight(val pos: BlockPos, val r: Float, val g: Float, val b: Float)

    private val trackedBlocks = setOf(
        Blocks.BROWN_TERRACOTTA,
        Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.TERRACOTTA,
        Blocks.INFESTED_COBBLESTONE,
        Blocks.CLAY
    )

    private val badBlocks = setOf(
        Blocks.INFESTED_COBBLESTONE,
        Blocks.TERRACOTTA
    )

    private var cachedHighlights: List<HeatmapHighlight> = emptyList()

    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % 4L != 0L) return

        if (!ConfigAccess.isHeatmapEnabled() || !ScoreboardUtils.isColdStatRelevant()) {
            if (cachedHighlights.isNotEmpty()) cachedHighlights = emptyList()
            return
        }

        val world = client.level ?: return
        val player = client.player ?: return
        val playerPos = player.blockPosition()

        val mutablePos = BlockPos.MutableBlockPos()
        val list = mutableListOf<HeatmapHighlight>()

        for (x in playerPos.x - 7..playerPos.x + 7) {
            for (y in playerPos.y - 1..playerPos.y + 7) {
                for (z in playerPos.z - 7..playerPos.z + 7) {
                    mutablePos.set(x, y, z)

                    val state = world.getBlockState(mutablePos)
                    val block = state.block

                    if (block !in trackedBlocks || block in badBlocks) continue
                    if (!isBlockExposed(world, mutablePos)) continue

                    val (r, g, b) = priorityColor(block)
                    list.add(HeatmapHighlight(mutablePos.immutable(), r, g ,b))
                }
            }
        }
        cachedHighlights = list
    }

    fun render (context: LevelRenderContext) {
        val camera = context.levelState().cameraRenderState

        for (highlight in cachedHighlights) {
            BlockOutline.renderBlockHighlight(highlight.pos, camera, highlight.r, highlight.g, highlight.b)
        }
    }

    private fun priorityColor(block: Block): Triple<Float, Float, Float> {
        return when (block) {
            Blocks.SMOOTH_RED_SANDSTONE, Blocks.CLAY -> Triple(0f / 255f, 100f / 255f, 0f / 255f)
            Blocks.BROWN_TERRACOTTA -> Triple(144f / 255f, 238f / 255f, 144f / 255f)
            else -> Triple(0f / 255f, 255f / 255f, 0f / 255f)
        }
    }

    private fun isBlockExposed(world: ClientLevel, pos: BlockPos): Boolean {
        if (world.getBlockState(pos).block == Blocks.BEDROCK) return false // ignore bedrock

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