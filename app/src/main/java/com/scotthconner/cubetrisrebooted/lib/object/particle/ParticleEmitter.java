package com.scotthconner.cubetrisrebooted.lib.object.particle;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;

import java.util.ArrayDeque;

/**
 * Controlls the emission of particles via a given program, and will hold all of the available
 * particles for emission. Shares ownership of ParticleInstances with a parent ParticleSystem.
 *
 * Created by scottc on 3/21/16.
 */
public class ParticleEmitter {
    // PUBLIC CONSTANTS ////////////////////////////////////////////
    public static long INFINITE_LIFE = -666;
    public static int  INSTANT_EMISSION = -1;
    ////////////////////////////////////////////////////////////////

    // INSTANCE VARIABLES //////////////////////////////////////////
    // reference to the particle system owner. can only be set once
    ParticleSystem mParentSystem;

    // program used to decorate and generate particles
    IParticleEmissionProgram mEmissionProgram;

    // used as a pool of previously allocated particle instances
    private final ArrayDeque<ParticleInstance> mAvailableParticles;

    // emission state
    private long    mEmitterLife;  // the emission duration in milliseconds
    private boolean mIsStarted;    // have we started emitting?
    private boolean mIsPaused;     // have we paused emitting?

    // emission computed variables
    private float mParticlesPerMs;
    private float mParticleAccumulator;

    // emitter location
    protected Vertex mPosition;
    ////////////////////////////////////////////////////////////////

    // PUBLIC INTERFACE METHODS ////////////////////////////////////
    public ParticleEmitter(IParticleEmissionProgram emissionProgram) {
        // set the initial state
        mEmissionProgram = emissionProgram;
        mParentSystem = null;
        mPosition = new Vertex(0,0,0);

        int maxParticles = mEmissionProgram.getMaxParticleCount();

        // create the particle pools and initialize the counters
        mAvailableParticles = new ArrayDeque<>(maxParticles);
        mParticlesPerMs = mEmissionProgram.getEmissionRate() / 1000.0f;

        // fill the free particle pool with empty particles, with proper parent references
        for(int x = 0; x < maxParticles; x++) {
            mAvailableParticles.add(mEmissionProgram.createParticle().withParentEmitter(this));
        }

        // resets everything to an initial state
        reset();
    }

    public void update(long msDelta) {
        // don't do anything if we are not a live emitter, or we are instant as there is no purpose
        if (!mIsStarted) return;

        synchronized(mAvailableParticles) {
            // only do these things if the emitter isn't paused and still has life or an infinite lifespan
            if (isEmitting()) {
                // just got a little older...
                mEmitterLife -= msDelta;

                // accumulate the number of particles we need to create per second
                mParticleAccumulator += msDelta * mParticlesPerMs;

                // create a new particle for everything we've accumulated so far
                // as long as there is a free particle instance
                while (mParticleAccumulator >= 1.0f) {
                    if (!mAvailableParticles.isEmpty()) {
                        // grab the next available particle and reset it
                        ParticleInstance p = mAvailableParticles.pop();
                        mEmissionProgram.resetParticle(p);

                        // if we are relatively positioned to the emitter, then start at 0,0,0, otherwise
                        // set it relative to the pos
                        if (mEmissionProgram.isRelativePositioned()) {
                            p.setPosition(0, 0, 0);
                        } else {
                            p.setPosition(mPosition.x, mPosition.y, mPosition.z);
                        }

                        // add the active particle to the system, this is where ownership transfers
                        mParentSystem.addActiveParticle(p);
                    }

                    // decrement accumulator down so we will stop trying to create them.
                    mParticleAccumulator -= 1;
                }
            }
        }
    }

    /**
     * Restarts the emitters lifespan, and clears all other stateful changes except particle
     * instances. Emitted particles are not currently re-claimed in this implementation.
     */
    public void reset() {
        mIsStarted = false;
        mIsPaused = false;
        mParticleAccumulator = 0;
        mEmitterLife = mEmissionProgram.getEmitterLifespan();
    }

    /**
     * Starts the emitter
     */
    public void start() {
        mIsStarted = true;
        mIsPaused = false;
    }

    /**
     * simply pauses emission
     */
    public void pause() {
        mIsPaused = true;
    }

    /**
     * @return true if the emitter is in a state where it is actively emitting particles
     */
    public boolean isEmitting() {
        return !mIsPaused && (mEmitterLife > 0 || mEmissionProgram.getEmitterLifespan() == INFINITE_LIFE);
    }

    /**
     * Sets the emitter position, relative to render context.
     *
     * @param x x pos of the emitter
     * @param y y pos of the emitter
     * @param z z pos of the emitter
     */
    public void setEmitterPosition(float x, float y, float z) {
        mPosition.x = x;
        mPosition.y = y;
        mPosition.z = z;
    }

    /**
     *
     * @return Direct vertex object that describes the position of the emitter.
     */
    public Vertex getEmitterPosition() {
        return mPosition;
    }

    /**
     * Lets the particles know how they need to set their location based on the rendering mode.
     *
     * @return true if all of the particle's positions are stored relatively to their emitter's position
     */
    public boolean isRelativePositioned() {
        return mEmissionProgram.isRelativePositioned();
    }

    /**
     * When a particle instead is dead, the particle can add itself back to the pool
     * by calling #reap() on itself, which will re-reference it back to the emitter's free list.
     * @param p the particle that is ready to be used again
     */
    public void addAvailableParticle(ParticleInstance p) {
        mAvailableParticles.add(p);
    }

    /**
     * Done once when an emitter is added to a particle system. Should only
     * be called once. Beware.
     * @param parent the parent particle system that is calling into this function.
     */
    public void setParentSystem(ParticleSystem parent) {
        mParentSystem = parent;
    }
    ////////////////////////////////////////////////////////////////
}
