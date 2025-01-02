package com.scotthconner.cubetrisrebooted.lib.object.particle;

/**
 * This interface interacts with the particle system to help generate the unique particles
 * of the system.
 *
 * Created by scottc on 3/6/16.
 */
public interface IParticleEmissionProgram {
    /**
     * Determines if the particles always remain relative to the emitter's location,
     * or whether the particles can move freely from the emitter.
     *
     * @return true if the particles are always relative to the emitter.
     */
    boolean isRelativePositioned();

    /**
     * Returns the max number of particles this emission program should be able
     * to spawn within a single particle system.
     *
     * @return the max number of particles that should be allowed for one particle system instance.
     */
    int getMaxParticleCount();

    /**
     * Returns the lifespan of the emitter, in millisecond clock time.
     *
     * @return the lifespan of the emitter in milliseconds.
     */
    long getEmitterLifespan();

    /**
     * Returns an integer that represents the number of particles that should be spawned
     * per second of the particle system's lifespan.
     *
     * @return number of particles that should be spawned per second
     */
    int getEmissionRate();

    /**
     * This method is invoked for each particle in the system at least
     * once for allocation. The particle is assumed to be decorated with all initial
     * modifiers
     *
     * @return a newly allocated Particle Instance
     */
    ParticleInstance createParticle();

    /**
     * Takes a previously allocated ParticleInstance created from a #createParticle call,
     * and will allow you to reset it as you see it (clear, update, or reset the modifiers
     * on a given particle).
     *
     * @param particle the particle to reset.
     */
    void resetParticle(ParticleInstance particle);
}
