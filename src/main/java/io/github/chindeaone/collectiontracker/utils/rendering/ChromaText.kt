package io.github.chindeaone.collectiontracker.utils.rendering

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
    val PREFIX_TEXT  = TextColor(0xFFFFFE, "sct_prefix")
    const val PREFIX_COLOR_RGB = 0xFFFFFE

    @JvmStatic
    var prefixGlyph = false
    @JvmStatic
    var chromaGlyph = false

    private val CHROMA = Util.memoize { texture: Identifier ->
        RenderType(
            "sct_chroma",
            RenderSetup.builder(CustomPipelines.CHROMA_TEXT)
                .withTexture("Sampler0", texture)
                .createRenderSetup()
        )
    }

    private val PREFIX_GRADIENT = Util.memoize { texture: Identifier ->
        RenderType(
            "sct_prefix",
            RenderSetup.builder(CustomPipelines.PREFIX_GRADIENT_TEXT)
                .withTexture("Sampler0", texture)
                .createRenderSetup()
        )
    }

    @JvmStatic
    fun checkGlyph(glyph: GlyphInstance) {
        prefixGlyph = glyph.style.color?.name == PREFIX_TEXT.name
        chromaGlyph = glyph.style.color?.name == CHROMA_TEXT.name
    }

    @JvmStatic
    fun getChromaRenderType(texture: Identifier) = CHROMA.apply(texture)

    @JvmStatic
    fun getPrefixGradientRenderType(texture: Identifier) = PREFIX_GRADIENT.apply(texture)

    @JvmStatic
    fun isChromaGlyph(glyph: GlyphInstance): Boolean {
        val color = glyph.style.color
        return when (color?.name) {
            CHROMA_TEXT.name, PREFIX_TEXT.name -> true
            else -> color?.value == PREFIX_COLOR_RGB
        }
    }

    @JvmStatic
    fun isPrefixGradientGlyph(glyph: GlyphInstance): Boolean {
        val color = glyph.style.color
        return color?.name == PREFIX_TEXT.name || color?.value == PREFIX_COLOR_RGB
    }

    fun style(color: ChromaColour): Style {
        return if (color.timeForFullRotationInMillis > 0) {
            Style.EMPTY.withColor(CHROMA_TEXT)
        } else {
            Style.EMPTY.withColor(TextColor.fromRgb(color.getEffectiveColourRGB()))
        }
    }

    fun prefixStyle(): Style {
        return Style.EMPTY.withColor(PREFIX_TEXT)
    }
}