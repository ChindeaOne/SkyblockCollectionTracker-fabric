package io.github.chindeaone.collectiontracker.mixins;

//? if 26.2
//import io.github.chindeaone.collectiontracker.utils.rendering.ChromaRenderer;
import io.github.chindeaone.collectiontracker.utils.rendering.WorldRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRenderMixin {

    @Inject(method = "close", at = @At("HEAD"))
    private void sct$onGameRendererClose(CallbackInfo ci) {
        WorldRenderer.close();
    }

    // Luna the savior for this one
    //? if 26.2 {
    /*@Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;endFrame()V",
                    shift = At.Shift.AFTER
            )
    )
    private static void sct$clearChromaUniforms(CallbackInfo ci) {
         ChromaRenderer.clearChromaUniforms();
    }
    *///?}
}
