package com.azhi.mushoku.client.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Vanilla setPos updates only the current position; both ends must coincide at birth. */
@Mixin(Particle.class)
public interface ParticleOriginAccess {
    @Accessor("xo") void mushokuPreviousX(double x);
    @Accessor("yo") void mushokuPreviousY(double y);
    @Accessor("zo") void mushokuPreviousZ(double z);
}
