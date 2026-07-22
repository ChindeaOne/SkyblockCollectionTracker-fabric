package io.github.chindeaone.collectiontracker.mixins.chroma;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;reset()V"))
    private static void clearChromaUniforms(CallbackInfo ci) {
        ChromaRenderer.getChromaUniform().clear();
    }
}
