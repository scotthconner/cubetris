package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import android.graphics.Color;

import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

/**
 * Save for use across particles.
 *
 * Created by scottc on 3/6/16.
 */
public class ParticleLinearColorModifier implements IParticleModifier {
    private static int RED   = ParticleInstance.RED;
    private static int GREEN = ParticleInstance.GREEN;
    private static int BLUE  = ParticleInstance.BLUE;
    private static int ALPHA = ParticleInstance.ALPHA;

    private int mColorStart;
    private int mColorEnd;

    public ParticleLinearColorModifier(int colorStart, int colorEnd) {
        mColorStart = colorStart;
        mColorEnd   = colorEnd;
    }

    @Override
    public void update(ParticleInstance particle, long msDelta) {
        float lifeExpended = particle.getPercentLifeExpended();

        particle.setColor(
                (Color.alpha(mColorStart) + (Color.alpha(mColorEnd) - Color.alpha(mColorStart)) * lifeExpended) / 255.0f,
                (Color.red(mColorStart) + (Color.red(mColorEnd) - Color.red(mColorStart)) * lifeExpended) / 255.0f,
                (Color.green(mColorStart) + (Color.green(mColorEnd) - Color.green(mColorStart)) * lifeExpended) / 255.0f,
                (Color.blue(mColorStart) + (Color.blue(mColorEnd) - Color.blue(mColorStart)) * lifeExpended) / 255.0f);
    }

    @Override
    public void reset(ParticleInstance particle) {
        // modifier is stateless
    }
}
