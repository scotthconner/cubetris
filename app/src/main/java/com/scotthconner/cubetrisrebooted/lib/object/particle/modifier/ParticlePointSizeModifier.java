package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

/**
 * Safe for us across particles
 *
 * Created by scottc on 3/6/16.
 */
public class ParticlePointSizeModifier implements IParticleModifier {
    private float mSizeStart;
    private float mDifference;

    public ParticlePointSizeModifier(float startSize, float endSize) {
        mSizeStart  = startSize;
        mDifference = endSize - mSizeStart;
    }

    public void update(ParticleInstance particle, long msDelta) {
        particle.setPointSize(mSizeStart + (mDifference * particle.getPercentLifeExpended()));
    }

    public void reset(ParticleInstance particle) { particle.setPointSize(mSizeStart); }
}
