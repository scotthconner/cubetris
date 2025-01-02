package com.scotthconner.cubetrisrebooted.cubetris.particle.stars;

import android.graphics.Color;

import com.scotthconner.cubetrisrebooted.lib.core.tween.ExponentialTweenFunction;
import com.scotthconner.cubetrisrebooted.lib.core.tween.LinearTweenFunction;
import com.scotthconner.cubetrisrebooted.lib.core.tween.TweenMode;
import com.scotthconner.cubetrisrebooted.lib.object.particle.IParticleEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.IParticleModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.KeyframedParticleInstanceColorModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.KeyframedParticleInstancePositionModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleInstanceTextureRotationModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleLifeSpanModifier;

/**
 * Created by scottc on 4/12/16.
 */
public class TwoStarMoveEmissionProgram implements IParticleEmissionProgram {
    private static long STAR_LIFE_MS = 1000;
    private static IParticleModifier TWO_STAR_ROTATION   = new ParticleInstanceTextureRotationModifier((float)Math.PI, 0.0f);

    // instance variables
    private static IParticleModifier mLifeSpan = new ParticleLifeSpanModifier(STAR_LIFE_MS);
    private int mCreateCount;

    // constructor
    public TwoStarMoveEmissionProgram() {
        mCreateCount = 0;
    }
    //////////////////////////////////////////////////////////////////

    // Emission Program Interface ////////////////////////////////////
    /**
     * This method is invoked for each particle in the system at least
     * once for allocation. The particle is assumed to be decorated with all initial
     * modifiers
     *
     * @return a newly allocated Particle Instance
     */
    public ParticleInstance createParticle() {
        mCreateCount += 1;
        ParticleInstance p = new ParticleInstance()
                       .withUVCoords(0.25f, 0.25f)
                       .withModifier(mLifeSpan)
                       .withModifier( new KeyframedParticleInstancePositionModifier()
                            .start(mCreateCount % 2 == 0 ? 0.55f : -0.55f, 0, mCreateCount % 2 == 0 ? 0 : -0.05f, 0, 3.0f, 0, STAR_LIFE_MS,
                                      ExponentialTweenFunction.getInstance(), TweenMode.EASE_OUT))
                       .withModifier( new KeyframedParticleInstanceColorModifier()
                            .start(Color.argb(0,255,255,0), Color.argb(255,255,255,0), STAR_LIFE_MS * 0.4f,
                                      LinearTweenFunction.getInstance(), TweenMode.EASE_IN)
                            .animate(Color.argb(0, 255, 255, 0), STAR_LIFE_MS * 0.60f,
                                      LinearTweenFunction.getInstance(), TweenMode.EASE_IN));
        p.setPointSize(80);
        return p;
    }

    /**
     * Takes a previously allocated ParticleInstance created from a #createParticle call,
     * and will allow you to reset it as you see it (clear, update, or reset the modifiers
     * on a given particle).
     *
     * @param particle the particle to reset.
     */
    public void resetParticle(ParticleInstance particle) {
        particle.reset();
    }

    /**
     * Returns the max number of particles this emission program should be able
     * to spawn within a single particle system.
     *
     * @return the max number of particles that should be allowed for one particle system instance.
     */
    public int getMaxParticleCount() { return 2; }

    /**
     * Returns an integer that represents the number of particles that should be spawned
     * per second of the particle system's lifespan.
     *
     * @return number of particles that should be spawned per second
     */
    public int getEmissionRate() { return 5; }

    /**
     * Returns the lifespan of the emitter, in millisecond clock time.
     *
     * @return the lifespan of the emitter in milliseconds.
     */
    public long getEmitterLifespan() { return 405; }

    /**
     * Determines if the particles always remain relative to the emitter's location,
     * or whether the particles can move freely from the emitter.
     *
     * @return true if the particles are always relative to the emitter.
     */
    public boolean isRelativePositioned() { return true; }
    //////////////////////////////////////////////////////////////////
}
