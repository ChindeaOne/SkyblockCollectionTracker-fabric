package io.github.chindeaone.collectiontracker.mixins.chroma;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable.Styled;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph.GlyphInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Font.PreparedTextBuilder.class)
public class TextBuilderMixin {

    @Inject(
            method = "visit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font$GlyphVisitor;acceptGlyph(Lnet/minecraft/client/gui/font/TextRenderable$Styled;)V"
            )
    )
    private void sct$checkGlyph(CallbackInfo ci, @Local(name = "glyph") Styled drawable) {
        if (drawable instanceof GlyphInstance glyph) {
            ChromaText.isChromaGlyph(glyph);
        }
    }
}
