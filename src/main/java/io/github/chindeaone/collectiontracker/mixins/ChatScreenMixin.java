package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.gui.overlays.AbstractOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        for (AbstractOverlay overlay: OverlayManager.all()) {
            if (overlay.shouldRender() && OverlayManager.isCollectionOverlay(overlay)) {
                if(overlay.isHovered(mouseX, mouseY)) {
                    ci.cancel();
                }
            }
        }
    }
}
