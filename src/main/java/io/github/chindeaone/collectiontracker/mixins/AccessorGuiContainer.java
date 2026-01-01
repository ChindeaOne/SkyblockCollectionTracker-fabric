package io.github.chindeaone.collectiontracker.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AccessorGuiContainer {

    @Invoker("renderBg")
    void invokeDrawGuiContainerBackgroundLayer_sct(GuiGraphics context, float deltaTicks, int mouseX, int mouseY);
}
