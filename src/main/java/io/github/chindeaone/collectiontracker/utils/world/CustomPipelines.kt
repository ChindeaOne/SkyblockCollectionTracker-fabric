package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import net.minecraft.client.renderer.RenderPipelines
//? if = 1.21.11 {
import net.minecraft.resources.Identifier
//? } else {
/*import net.minecraft.resources.ResourceLocation
*///? }

object CustomPipelines {
    lateinit var LINE_THROUGH_WALLS: RenderPipeline

    fun register() {
        LINE_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            //? if = 1.21.11 {
                 .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/line_through_walls"))
            //? } else {
                /*.withLocation(ResourceLocation.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/line_through_walls"))
                *///?}
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build()
        )
    }
}