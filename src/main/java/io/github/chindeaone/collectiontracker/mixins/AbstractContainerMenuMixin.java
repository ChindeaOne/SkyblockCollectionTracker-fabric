package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.CommissionKeybinds;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Inject(method = "setItem", at = @At("TAIL"))
    private void onSetItem(int slot, int stateId, ItemStack itemStack, CallbackInfo ci) {
        CommissionKeybinds.onSlotUpdated(
                (AbstractContainerMenu)(Object)this,
                slot,
                itemStack
        );
    }
}
