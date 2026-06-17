package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.Optional

object CustomPipelines {
    lateinit var LINE_THROUGH_WALLS: RenderPipeline
    lateinit var HIGHLIGHT: RenderPipeline

    fun register() {
        LINE_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/line_through_walls"))
                .withDepthStencilState(Optional.empty())
                .build()
        )
        HIGHLIGHT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/highlight"))
                .withDepthStencilState(Optional.of(DepthStencilState(
                    CompareOp./*? if 26.2 {*/ /*GREATER_THAN_OR_EQUAL *//*?} else {*/ LESS_THAN_OR_EQUAL /*?}*/,
                    false)))
                .build()
        )
    }
}