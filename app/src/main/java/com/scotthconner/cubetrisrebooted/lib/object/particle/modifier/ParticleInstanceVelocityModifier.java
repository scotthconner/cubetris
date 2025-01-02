package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

import java.util.Random;

/**
 * Best if used on individual particles.
 *
 * Has a variant angle vector and magnitude for current velocity, and can be generated psuedo-randomly
 * for each instance of the particle it is attached to. Current has a constant acceleration across
 * life spans.
 *
 * Created by scottc on 3/6/16.
 */
public class ParticleInstanceVelocityModifier implements IParticleModifier {
    private static int X = ParticleInstance.X;
    private static int Y = ParticleInstance.Y;
    private static int Z = ParticleInstance.Z;

    private Random mRandom;

    // configuration for angle and magnitude of velocity vector
    private float mX;
    private float mXVariance;
    private float mY;
    private float mYVariance;
    private float mZ;
    private float mZVariance;

    // current modifier state
    private Vertex mVelocity;
    private Vertex mAcceleration;

    public ParticleInstanceVelocityModifier(float x, float y, float z, float vx, float vy, float vz) {
        mRandom = new Random();
        mX = x;
        mY = y;
        mZ = z;
        mXVariance = vx;
        mYVariance = vy;
        mZVariance = vz;

        // current lifespan state.
        mVelocity = new Vertex();
        mAcceleration = new Vertex();

        generateNewVelocity();
    }

    public ParticleInstanceVelocityModifier withAcceleration(float x, float y, float z) {
        if (null == mAcceleration) {
            mAcceleration = new Vertex(x, y, z);
        } else {
            mAcceleration.set(x, y, z);
        }
        return this;
    }

    @Override
    public void update(ParticleInstance particle, long msDelta) {
        Vertex p = particle.getPosition();
        float second = msDelta / 1000.0f;

        // update the position with the velocity
        particle.setPosition(p.x + mVelocity.x * second,
                p.y + mVelocity.y * second,
                p.z + mVelocity.z * second);

        // update the velocity with the acceleration
        mVelocity.x += mAcceleration.x * second;
        mVelocity.y += mAcceleration.y * second;
        mVelocity.z += mAcceleration.z * second;
    }

    @Override
    public void reset(ParticleInstance particle) {
        // we have state here.
        generateNewVelocity();
    }

    private void generateNewVelocity() {
        mVelocity.x = mX - (mXVariance / 2.0f) + mXVariance * mRandom.nextFloat();
        mVelocity.y = mY - (mYVariance / 2.0f) + mYVariance * mRandom.nextFloat();
        mVelocity.z = mZ - (mZVariance / 2.0f) + mZVariance * mRandom.nextFloat();
    }
}
