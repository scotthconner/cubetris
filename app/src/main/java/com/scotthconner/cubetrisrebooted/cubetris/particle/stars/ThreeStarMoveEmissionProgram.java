package com.scotthconner.cubetrisrebooted.cubetris.particle.stars;

import android.graphics.Color;

import com.scotthconner.cubetrisrebooted.lib.object.particle.IParticleEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.IParticleModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleInstanceTextureRotationModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleInstanceVelocityModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleLifeSpanModifier;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.ParticleLinearColorModifier;

/**
 * Created by scottc on 4/12/16.
 */
public class ThreeStarMoveEmissionProgram implements IParticleEmissionProgram {
    // two star modifiers
    private static IParticleModifier THREE_STAR_ROTATION   = new ParticleInstanceTextureRotationModifier(0.0f, (float)Math.PI * 1.0f);
    private static IParticleModifier THREE_STAR_VELOCITY_1 = new ParticleInstanceVelocityModifier(-1.0f, 3, 0, 0, 0, 0);
    private static IParticleModifier THREE_STAR_VELOCITY_2 = new ParticleInstanceVelocityModifier(0.0f, 3, 0, 0, 0, 0);
    private static IParticleModifier THREE_STAR_VELOCITY_3 = new ParticleInstanceVelocityModifier(1.0f, 3, 0, 0, 0, 0);
    private static IParticleModifier THREE_STAR_COLOR      = new ParticleLinearColorModifier(Color.argb(255, 255, 50, 0),
                                                                                              Color.argb(255, 255, 120, 255));

    // instance variables
    private IParticleModifier mLifeSpan;       // we will eventually want to reap the stars for re-use
    private int mCreateCount;

    // constructor
    public ThreeStarMoveEmissionProgram() {
        mLifeSpan  = new ParticleLifeSpanModifier(3000);
        mCreateCount = 0;
    }


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
                   .withModifier(THREE_STAR_COLOR)
                   .withModifier(THREE_STAR_ROTATION)
                   .withModifier(mCreateCount == 1 ? THREE_STAR_VELOCITY_1 :
                                     mCreateCount == 2 ? THREE_STAR_VELOCITY_2 : THREE_STAR_VELOCITY_3);
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
    public int getMaxParticleCount() { return 3; }

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
    public long getEmitterLifespan() { return 3; }

    /**
     * Determines if the particles always remain relative to the emitter's location,
     * or whether the particles can move freely from the emitter.
     *
     * @return true if the particles are always relative to the emitter.
     */
    public boolean isRelativePositioned() { return true; }
    //////////////////////////////////////////////////////////////////
}
