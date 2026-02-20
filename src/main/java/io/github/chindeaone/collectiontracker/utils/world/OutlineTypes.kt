package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object OutlineTypes {
    lateinit var LINE_THROUGH_WALLS: RenderType

    fun init() {
        CustomPipelines.register()
        val setup = RenderSetup.builder(CustomPipelines.LINE_THROUGH_WALLS)
            .bufferSize(1536)
            .createRenderSetup()

        LINE_THROUGH_WALLS = RenderType.create("line_through_walls", setup)
    }
}