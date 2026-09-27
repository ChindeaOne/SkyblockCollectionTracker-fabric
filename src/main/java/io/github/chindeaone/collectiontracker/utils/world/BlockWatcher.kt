package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.ModLoader
import net.minecraft.client.Minecraft
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

object BlockWatcher {

    var blockBox: AABB? = null
    var miningBlockType: String = ""
    var foragingBlockType: String = ""
    @Volatile
    var precisionMiningBlockType: String = ""

    // Check the block the player is looking at
    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % 2L != 0L) return

        val level = client.level ?: return
        val hitResult = client.hitResult

        (hitResult as? BlockHitResult)?.let { blockHit ->
            val pos = blockHit.blockPos

            val block = level.getBlockState(pos).block

            blockBox = AABB(pos)

            updateBlockType(block)
            updatePrecisionMiningBlockType(block)
        } ?: run {
            precisionMiningBlockType = ""
            blockBox = null
        }
    }

    private fun updateBlockType(block: Block) {
        miningBlockType = MiningMapping.miningBlockTypePerBlock[block] ?: ""
        foragingBlockType = ForagingMapping.foragingBlockTypePerBlock[block] ?: ""
    }

    private fun updatePrecisionMiningBlockType(block: Block) {
        precisionMiningBlockType = when {
            miningBlockType in MiningMapping.precisionMiningBlockTypes -> miningBlockType
            IslandTracker.currentMiningIsland == "Mineshaft" && block in MiningMapping.mineshaftTungstenBlocks -> "dwarven_metals"
            else -> ""
        }
    }
}