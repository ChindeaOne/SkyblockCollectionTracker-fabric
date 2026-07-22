package io.github.chindeaone.collectiontracker.mixins.chroma;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaText;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphRenderTypes.class)
public class GlyphTypesMixin {

    @Unique
    private Identifier sct$identifier;

    @ModifyReturnValue(method = {"createForColorTexture", "createForIntensityTexture"}, at = @At("RETURN"))
    private static GlyphRenderTypes ofMethods(GlyphRenderTypes original, @Local(argsOnly = true, name = "name") Identifier name) {
        ((GlyphTypesMixin) (Object) original).sct$identifier = name;
        return original;
    }

    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    private void sct$select(CallbackInfoReturnable<RenderType> cir) {
        if (ChromaText.getGlyphIsChroma()) {
            cir.setReturnValue(ChromaText.getChromaRenderType(this.sct$identifier));
        }
    }
}
