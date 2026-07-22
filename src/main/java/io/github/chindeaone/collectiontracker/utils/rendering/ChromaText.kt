package io.github.chindeaone.collectiontracker.utils.rendering

import io.github.chindeaone.collectiontracker.utils.world.CustomPipelines
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph.GlyphInstance
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.Identifier
import net.minecraft.util.Util

object ChromaText {
    val CHROMA_TEXT = TextColor(0xFFFFFF, "sct_chroma")

    @JvmStatic
    var glyphIsChroma = false

    private val CHROMA = Util.memoize { texture: Identifier ->
        RenderType(
            "sct_chroma",
            RenderSetup.builder(CustomPipelines.CHROMA_TEXT)
                .withTexture("Sampler0", texture)
                .createRenderSetup()
        )
    }

    @JvmStatic
    fun getChromaRenderType(texture: Identifier) = CHROMA.apply(texture)

    @JvmStatic
    fun isChromaGlyph(glyph: GlyphInstance): Boolean = glyph.style.color?.name == CHROMA_TEXT.name

    fun style(color: ChromaColour): Style {
        val style = if (color.timeForFullRotationInMillis > 0) {
            Style.EMPTY.withColor(CHROMA_TEXT)
        } else {
            Style.EMPTY.withColor(TextColor.fromRgb(color.getEffectiveColourRGB()))
        }

        return style
    }
}