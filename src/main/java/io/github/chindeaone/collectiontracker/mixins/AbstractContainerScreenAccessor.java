package io.github.chindeaone.collectiontracker.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Invoker("renderBg")
    void invokeDrawGuiContainerBackgroundLayer_sct(GuiGraphics context, float deltaTicks, int mouseX, int mouseY);

    @Invoker("getHoveredSlot")
    Slot invokeGetHoveredSlot(double mouseX, double mouseY);

    @Accessor("hoveredSlot")
    Slot getHoveredSlotField();

}
