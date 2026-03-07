package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.gui.overlays.AbstractOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        for (AbstractOverlay overlay: OverlayManager.all()) {
            if (overlay.shouldRender() && OverlayManager.isCollectionOverlay(overlay)) {
                if(overlay.isHovered(i,j)) {
                    ci.cancel();
                }
            }
        }
    }
}
