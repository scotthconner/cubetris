package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

/**
 * Created by scottc on 3/6/16.
 */
public class ParticleLifeSpanModifier implements IParticleModifier {
    private long mLifeSpanMs;

    public ParticleLifeSpanModifier(long lifespanMs) {
        mLifeSpanMs = lifespanMs;
    }

    public void update(ParticleInstance particle, long msDelta) {
        particle.mLife -= msDelta;
    }

    public void reset(ParticleInstance particle) {
        particle.mLifeSpan = mLifeSpanMs;
        particle.mLife = mLifeSpanMs;
    }
}
