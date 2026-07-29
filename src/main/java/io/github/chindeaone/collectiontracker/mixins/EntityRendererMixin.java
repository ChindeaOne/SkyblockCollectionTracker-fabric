package io.github.chindeaone.collectiontracker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightUtils;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @ModifyReturnValue(
            method = "getNameTag",
            at = @At("RETURN")
    )
    private Component addRankToNameTag(Component original, @Local(argsOnly = true, name = "entity") Entity entity) {
        if (!HypixelUtils.isOnSkyblock()) return original;
        if (!(entity instanceof Player player)) return original;

        String playerName = player.getName().getString();

        MutableComponent result = original.copy();

        if (ConfigAccess.isColeweightRankInNameTag()) {
            Component rankComponent = ColeweightUtils.getRankComponent(playerName);
            if (rankComponent != null) {
                result.append(" ").append(rankComponent);
            }

        }

        if (ConfigAccess.isFarmingweightRankInNameTag()) {
            Component rankComponent = FarmingweightUtils.getRankComponent(playerName);
            if (rankComponent != null) {
                result.append(" ").append(rankComponent);
            }
        }

        return result;
    }
}
