package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

object CustomPipelines {
    lateinit var LINE_THROUGH_WALLS: RenderPipeline

    fun register() {
        LINE_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/line_through_walls"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build()
        )
    }
}