package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.world.Renderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRenderMixin {

    @Inject(method = "close", at = @At("HEAD"))
    private void sct$onGameRendererClose(CallbackInfo ci) {
        Renderer.close();
    }
}
