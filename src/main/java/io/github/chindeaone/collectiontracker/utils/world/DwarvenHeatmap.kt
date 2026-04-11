package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.systems.RenderSystem
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks

object DwarvenHeatmap {

    private val blockList = setOf(
        "minecraft:brown_terracotta", // umber
        "minecraft:smooth_red_sandstone", // umber
        "minecraft:terracotta", // umber
        "minecraft:infested_cobblestone", // tungsten
        "minecraft:clay" // tungsten
    )

    private val excludedBlocks = setOf(
        "minecraft:infested_cobblestone", // tungsten
        "minecraft:brown_terracotta" // umber
    )

    private val blockPriority = mapOf(
        "minecraft:clay" to 2, // tungsten
        "minecraft:infested_cobblestone" to 1, // tungsten
        "minecraft:smooth_red_sandstone" to 3, // umber
        "minecraft:terracotta" to 2, // umber
        "minecraft:brown_terracotta" to 1 // umber
    )

    fun render (context: WorldRenderContext) {
        if (!RenderSystem.isOnRenderThread()) return
        if (!HypixelUtils.isOnSkyblock) return
        if (!ConfigAccess.isHeatmapEnabled() || MiningStatsWidget.currentMiningIsland != "Dwarven Mines") return

        val camera = context.worldState().cameraRenderState
        val buffers = context.consumers()

        val world = Minecraft.getInstance().level ?: return
        val player = Minecraft.getInstance().player ?: return
        val playerPos = player.blockPosition()

        for (x in playerPos.x - 7..playerPos.x + 7) {
            for (y in playerPos.y - 1..playerPos.y + 7) {
                for (z in playerPos.z - 7..playerPos.z + 7) {
                    val checkPos = BlockPos(x, y, z)

                    if (!isBlockExposed(world, checkPos)) continue
                    if (!isTrackedBlock(world, checkPos)) continue

                    val score = calculateBlockScore(world, checkPos)
                    if (score > 0) {
                        val (r, g, b) = priorityColor(score)
                        BlockOutline.renderBlockHighlight(buffers, checkPos, camera, r, g, b)
                    }
                }
            }
        }
    }

    private fun isTrackedBlock(world: ClientLevel, pos: BlockPos): Boolean {
        val block = world.getBlockState(pos).block
        val key = BuiltInRegistries.BLOCK.getKey(block)
        val blockName = key.toString()
        return blockName in blockList && blockName !in excludedBlocks
    }

    private fun calculateBlockScore(world: ClientLevel, pos: BlockPos): Int {
        var score = 0
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    val checkPos = pos.offset(x, y, z)
                    val block = world.getBlockState(checkPos).block
                    val key = BuiltInRegistries.BLOCK.getKey(block)
                    val blockName = key.toString()
                    if (blockName in blockList && isBlockExposed(world, checkPos)) score += blockPriority.getOrDefault(blockName, 0)
                }
            }
        }
        return score
    }

    private fun priorityColor(score: Int): Triple<Float, Float, Float> {
        return when {
            score >= 19 -> Triple(4f / 255f, 4f / 255f, 4f / 255f) // black
            score >= 14 -> Triple(49f / 255f, 41f / 255f, 165f / 255f) // dark blue
            score >= 8 -> Triple(104f / 255f, 210f / 255f, 249f /255f) // blue
            score >= 4 -> Triple(145f / 255f , 23f / 255f , 23f / 255f )  // red
            else       -> Triple(20f / 255f, 90f / 255f, 38f / 255f )  // green
        }
    }

    private fun isBlockExposed(world: ClientLevel, pos: BlockPos): Boolean {
        if (world.getBlockState(pos).block == Blocks.BEDROCK) return false // ignore bedrock

        fun isNotSolid(pos: BlockPos): Boolean {
            val state = world.getBlockState(pos)
            return state.isAir || state.block == Blocks.SNOW || state.block == Blocks.LIGHT_GRAY_CARPET
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