package io.github.chindeaone.collectiontracker.util.world

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object BlockWatcher {

    var blockId : String = ""
        private set
    var miningBlockType: String = ""
        private set

    // Check the block the player is looking at
    fun onClientTick(client: Minecraft) {
        val hitResult = client.hitResult

        if (hitResult != null && hitResult.type == HitResult.Type.BLOCK) {
            val blockHit = hitResult as BlockHitResult
            val pos = blockHit.blockPos

            val state = client.level?.getBlockState(pos) ?: return
            val block = state.block

            blockId = BuiltInRegistries.BLOCK.getKey(block).toString()

            updateMiningBlockType(blockId)
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
}