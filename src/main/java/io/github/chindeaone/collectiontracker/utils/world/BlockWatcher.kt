package io.github.chindeaone.collectiontracker.utils.world

import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

object BlockWatcher {

    var blockId : String = ""
        private set
    var blockBox: AABB? = null
    @JvmStatic
    var miningBlockType: String = ""
        private set
    @JvmStatic
    var foragingBlockType: String = ""
        private set
    @Volatile
    var precisionMiningBlockType: String = ""
        private set

    // Check the block the player is looking at
    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        val hitResult = client.hitResult

        (hitResult as? BlockHitResult)?.let { blockHit ->
            val pos = blockHit.blockPos

            val state = client.level?.getBlockState(pos) ?: return
            val block = state.block

            blockBox = AABB(pos)

            blockId = BuiltInRegistries.BLOCK.getKey(block).toString()

            updateMiningBlockType(blockId)
            updateForagingBlockType(blockId)
            precisionMiningBlockType(blockId)
        } ?: run {
            precisionMiningBlockType = ""
            blockBox = null
        }
    }

    private fun updateMiningBlockType(type: String) {
        val detectedBlock = MiningMapping.miningBlockPerType.entries.find { entry ->
            entry.value.contains(type)
        }?.key

        if (detectedBlock != null) {
            miningBlockType = detectedBlock
        }
    }

    private fun updateForagingBlockType(type: String) {
        val detectedBlock = ForagingMapping.foragingBlockPerType.entries.find { entry ->
            entry.value.contains(type)
        }?.key

        if (detectedBlock != null) {
            foragingBlockType = detectedBlock
        }
    }

    private fun precisionMiningBlockType(type: String) {
        val allowedKeys = setOf("ores", "pure_ores", "dwarven_metals")

        val detectedBlock = MiningMapping.miningBlockPerType.entries.find { (key, values) ->
            key in allowedKeys && values.any { it == type }
        }?.key

        precisionMiningBlockType = detectedBlock ?: ""
    }
}