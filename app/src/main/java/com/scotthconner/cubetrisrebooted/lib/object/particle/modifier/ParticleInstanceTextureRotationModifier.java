package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

import java.util.Random;

/**
 * Best if used for individual particles.
 *
 * Created by scottc on 3/6/16.
 */
public class ParticleInstanceTextureRotationModifier implements IParticleModifier {
    private static int R = ParticleInstance.R;
    private float mRadiansPerSecond;            // the radians per second it should rotate each time
    private float mVarianceRadians;             // the natural variance between resets
    private float mActiveRadiansPerSecond;      // the current radians per second
    private Random random;

    /**
     *
     * @param radiansPerSecond the radians per second the particle texture will rotate
     * @param variance the variance in radians per second between resets from the set radians per second
     */
    public ParticleInstanceTextureRotationModifier(float radiansPerSecond, float variance) {
        mRadiansPerSecond = radiansPerSecond;
        mVarianceRadians = variance;
        random = new Random();
        generateNewRotationSpeed();
    }

    @Override
    public void update(ParticleInstance particle, long msDelta) {
        particle.setRotation(particle.getRotation() + (mActiveRadiansPerSecond * msDelta / 1000.0f));
    }

    @Override
    public void reset(ParticleInstance particle) {
        // modifier holds its random state
        generateNewRotationSpeed();
    }

    private void generateNewRotationSpeed() {
        // this actually changes state for the modifier
        mActiveRadiansPerSecond = mRadiansPerSecond - (mVarianceRadians / 2.0f) +
                mVarianceRadians * random.nextFloat();
    }
}
