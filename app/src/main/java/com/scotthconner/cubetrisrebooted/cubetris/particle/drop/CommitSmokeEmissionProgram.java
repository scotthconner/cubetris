package com.scotthconner.cubetrisrebooted.cubetris.particle.drop;

import android.graphics.Color;

import com.scotthconner.cubetrisrebooted.lib.object.particle.IParticleEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.IParticleModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleInstanceTextureRotationModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleInstanceVelocityModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleLifeSpanModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleLinearColorModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticlePointSizeModifier;

/**
 * Flame used by the active piece after it has begun to drop.
 *
 * Created by scottc on 3/14/16.
 */
public class CommitSmokeEmissionProgram implements IParticleEmissionProgram {
    // Singleton Instance ////////////////////////////////////////////
    private static CommitSmokeEmissionProgram mInstance = null;

    // instance variables
    IParticleModifier mLifeSpan;
    IParticleModifier mColor;
    IParticleModifier mPointSize;

    // singleton accessor
    public static CommitSmokeEmissionProgram getInstance() {
        if (mInstance == null) {
            mInstance = new CommitSmokeEmissionProgram();
        }
        return mInstance;
    }

    // constructor
    private CommitSmokeEmissionProgram() {
        mLifeSpan  = new ParticleLifeSpanModifier(1200);
        mColor     = new ParticleLinearColorModifier(Color.argb(255, 100, 100, 100), Color.argb(0, 50, 50, 50));
        mPointSize = new ParticlePointSizeModifier(100, 0);
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
        return new ParticleInstance()
                .withUVCoords(0.0f, 0.75f)
                .withModifier(mLifeSpan)
                .withModifier(mColor)
                .withModifier(mPointSize)
                .withModifier(new ParticleInstanceTextureRotationModifier(0.0f, (float)Math.PI * 3.0f))
                .withModifier(new ParticleInstanceVelocityModifier(0, 0, 0, 5.0f, 0, 5.0f)
                        .withAcceleration(0, -0.25f, 0));
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
    public int getMaxParticleCount() { return 200; }

    /**
     * Returns an integer that represents the number of particles that should be spawned
     * per second of the particle system's lifespan.
     *
     * @return number of particles that should be spawned per second
     */
    public int getEmissionRate() { return 10000; }

    /**
     * Returns the lifespan of the emitter, in millisecond clock time.
     *
     * @return the lifespan of the emitter in milliseconds.
     */
    public long getEmitterLifespan() { return 740; }

    /**
     * Determines if the particles always remain relative to the emitter's location,
     * or whether the particles can move freely from the emitter.
     *
     * @return true if the particles are always relative to the emitter.
     */
    public boolean isRelativePositioned() { return true; }
    //////////////////////////////////////////////////////////////////
}
