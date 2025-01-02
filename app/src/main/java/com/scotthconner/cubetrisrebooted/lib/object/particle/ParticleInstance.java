package com.scotthconner.cubetrisrebooted.lib.object.particle;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.object.particle.modifier.IParticleModifier;

import java.util.ArrayDeque;

/**
 * Holds the buffer data, and the state for a given particle. Used by the particle emitter,
 * and animated by a set of IParticleModifiers. Particles by default have infinite life.
 *
 * Created by scottc on 3/6/16.
 */
public class ParticleInstance {
    // Buffer Index /////////////////////////////////////////////////////////////////
    public static int X     = 0;               // x position
    public static int Y     = 1;               // y position
    public static int Z     = 2;               // z position
    public static int R     = 3;               // texture rotation in radians
    public static int RED   = 4;               // red component color for vertex
    public static int GREEN = 5;               // green component color for vertex
    public static int BLUE  = 6;               // blue component color for vertex
    public static int ALPHA = 7;               // alpha component color for vertex
    public static int S     = 8;               // gl point size
    public static int U     = 9;               // u coord inside of the texture
    public static int V     = 10;              // v coord inside of the texture
    public static int PARTICLE_BUFFER_SIZE = 11;

    // data that is passed to the graphics card, so final x, y, z, rotation
    // and gl point sile are all stored in this array.
    private float[] mBufferData;
    private ParticleEmitter mEmitter;
    private Vertex mPosition;
    //////////////////////////////////////////////////////////////////////////////////

    // particle attribute data, sharable for all IParticleModifiers //////////////////
    public static int INFINITE_LIFE = -666;  // totally creepy
    public long mLifeSpan;                   // total life span of this particle in milliseconds
    public long mLife;                       // how much of that life is still there

    // list of particle modifiers attached to the instance
    public ArrayDeque<IParticleModifier> mModifiers;
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * Allocates the particle float buffer and its list of modifiers.
     */
    public ParticleInstance() {
        mPosition = new Vertex();
        mBufferData = new float[PARTICLE_BUFFER_SIZE];
        mModifiers = new ArrayDeque<>();

        mLifeSpan = INFINITE_LIFE;
        mLife = INFINITE_LIFE;
        mEmitter = null;
    }

    /**
     * Primes the vertex buffer for the renderer
     */
    public void calculateVertexBuffer() {
        // if its absolute, then it is what it is
        mBufferData[X] = mPosition.x;
        mBufferData[Y] = mPosition.y;
        mBufferData[Z] = mPosition.z;

        // if its relative, the position is the position added to the emitter position
        if (mEmitter.isRelativePositioned()) {
            Vertex v = mEmitter.getEmitterPosition();
            mBufferData[X] += v.x;
            mBufferData[Y] += v.y;
            mBufferData[Z] += v.z;
        }
    }

    public float[] getVertexBuffer() {
        return mBufferData;
    }

    /**
     * Will clear all of the modifiers currently attached to this particle.
     */
    public void clearModifiers() {
        mModifiers.clear();
    }

    /**
     * Reset all of the modifiers attached to the particle in preparation
     * for the next life-cycle.
     */
    public void reset() {
        for(IParticleModifier modifier : mModifiers) {
            modifier.reset(this);
        }
    }

    /**
     * Helpful for changing particle attributes over its life time. Does not give
     * the ability to infer the scalar life time or span values, however.
     *
     * @return the life of the particle expressed in a percentage of its lifespan
     */
    public float getPercentLifeExpended() { return (float)(mLifeSpan - mLife) / (float)mLifeSpan; }

    /**
     * Attach a modifier to this particle. The modifier can be shared across
     * multiple particles, or be unique for this particle. Attaching a modifier
     * will reset it.
     *
     * @param modifier the modifier you want to add
     * @return a reference to this object for build chaining
     */
    public ParticleInstance withModifier(IParticleModifier modifier) {
        modifier.reset(this);
        mModifiers.add(modifier);
        return this;
    }

    /**
     * Sets the parent emitter. Hope you know what you're doing by doing this.
     * @param emitter the emitter you want to own this particle
     * @return the object itself
     */
    public ParticleInstance withParentEmitter(ParticleEmitter emitter) {
        mEmitter = emitter;
        return this;
    }

    /**
     * Run the per-frame logic of all modifiers for this particle.
     */
    public void update(long msDelta) {
        // update the particle with all of the modifiers
        for(IParticleModifier modifier : mModifiers) {
            modifier.update(this, msDelta);
        }
        calculateVertexBuffer();
    }

    /**
     * Determines if the particle is ready to be reaped for re-use
     * by its emitter.
     *
     * @return true if it is dead, false if it still has life
     */
    public boolean isDead() { return mLife <= 0 && (mLifeSpan != INFINITE_LIFE); }

    /**
     * Send this particle back to the emitter for future availability. Essentially says that
     * the particle is dead and ready to go.
     */
    public void reap() { mEmitter.addAvailableParticle(this); }

    /**
     * Sets the logical position of the particle. Does not alter buffer.
     *
     * @param x x pos
     * @param y y pos
     * @param z z pos
     */
    public void setPosition(float x, float y, float z) { mPosition.set(x, y, z); }

    /**
     * Returns the logical position of the particle.
     *
     * @return the vertex reference of the position of the particle.
     */
    public Vertex getPosition() { return mPosition; }

    /**
     * Sets the GL_POINT size when rendering this particle
     * @param s the size of the point, in render pixels.
     */
    public void setPointSize(float s) { mBufferData[S] = s; }

    /**
     *
     * @return the particle rotation in radians
     */
    public float getRotation() { return mBufferData[R]; }

    /**
     * Sets the particle rotation
     *
     * @param r the rotation in radians to set the particle to
     */
    public void setRotation(float r) { mBufferData[R] = r; }

    /**
     * Sets the color of this particle.
     *
     * @param a alpha, 0 - 1.0f
     * @param r red,   0 - 1.0f
     * @param g green, 0 - 1.0f
     * @param b blue,  0 - 1.0f
     */
    public void setColor(float a, float r, float g, float b) {
        mBufferData[ALPHA] = a;
        mBufferData[RED]   = r;
        mBufferData[GREEN] = g;
        mBufferData[BLUE]  = b;
    }

    /**
     * Set the UV coords of the point sprite
     *
     * @param u u coord
     * @param v v coord
     * @return self reference
     */
    public ParticleInstance withUVCoords(float u, float v) {
        mBufferData[U] = u;
        mBufferData[V] = v;
        return this;
    }
}
