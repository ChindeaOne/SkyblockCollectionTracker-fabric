package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object OutlineTypes {
    lateinit var LINE_THROUGH_WALLS: RenderType
    lateinit var HIGHLIGHT: RenderType

    fun init() {
        CustomPipelines.register()

        val setup = RenderSetup.builder(CustomPipelines.LINE_THROUGH_WALLS)
            /*? if < 26.2 */ .bufferSize(1536) 
            .createRenderSetup()
        LINE_THROUGH_WALLS = RenderType.create("line_through_walls", setup)

        val highlightSetup = RenderSetup.builder(CustomPipelines.HIGHLIGHT)
            /*? if < 26.2 */ .bufferSize(1536) 
            .createRenderSetup()
        HIGHLIGHT = RenderType.create("highlight", highlightSetup)
    }
}