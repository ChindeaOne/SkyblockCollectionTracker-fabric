package io.github.chindeaone.collectiontracker.utils.world

//? if = 1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
//? } else {
/*import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import java.util.OptionalDouble
*///? }

object OutlineTypes {
    lateinit var LINE_THROUGH_WALLS: RenderType

    fun init() {
        CustomPipelines.register()
        //? if = 1.21.11 {
        val setup = RenderSetup.builder(CustomPipelines.LINE_THROUGH_WALLS)
            .bufferSize(1536)
            .createRenderSetup()
            LINE_THROUGH_WALLS = RenderType.create("line_through_walls", setup)
        //? } else {
        /*val state = RenderType.CompositeState.builder()
            .setLineState(RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .createCompositeState(false)

        LINE_THROUGH_WALLS = RenderType.create(
            "line_through_walls",
            1536,
            CustomPipelines.LINE_THROUGH_WALLS,
            state
        )
        *///? }
    }
}