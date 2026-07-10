package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.StringUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ChatComponent.class, priority = 1001) // Lower priority for Skyhanni
public class ChatWeightMixin {
    @ModifyVariable(
            method = "addMessage",
            at = @At("HEAD"),
            argsOnly = true,
            name = "contents")
    private Component modifyVisualMessage(Component contents) {
        if (StringUtils.removeColor(contents.getString(), false).startsWith("[SCT]")) return contents;
        return ChatListener.farmingweightHandle(ChatListener.coleweightHandle(contents));
    }
}