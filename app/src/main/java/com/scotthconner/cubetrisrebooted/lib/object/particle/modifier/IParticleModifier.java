package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

/**
 * Particle Modifiers are attached to the particle for the lifetime of the particle,
 * unless the particle gets #clearModifiers called on it by the emission program. When
 * a particle is getting re-used it is #reset to some base configuration.
 *
 * Created by scottc on 3/6/16.
 */
public interface IParticleModifier {
    public void update(ParticleInstance particle, long msDelta);
    public void reset(ParticleInstance particle);
}
