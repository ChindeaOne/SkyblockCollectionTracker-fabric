package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.utils.world.PrecisionMining;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void onCreateParticle(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, CallbackInfoReturnable<Particle> ci) {
        PrecisionMining.handleParticles(options, x, y, z);

        // prevent particles from rendering client-side
        if (ConfigAccess.isPrecisionMiningHighlightEnabled() && (options.getType() == ParticleTypes.CRIT || options.getType() == ParticleTypes.HAPPY_VILLAGER)) {
            ci.setReturnValue(null);
            ci.cancel();
        }
    }
}
