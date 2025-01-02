package com.scotthconner.cubetrisrebooted.cubetris.particle.stars;

import com.scotthconner.cubetrisrebooted.lib.core.TextureManager;
import com.scotthconner.cubetrisrebooted.lib.object.particle.IParticleEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleEmitter;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleSystem;
import com.scotthconner.cubetrisrebooted.lib.object.particle.TexturedPointSpriteDefinition;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.AdditiveTransparencyBlendFunction;

/**
 * Displays 1, 2, or 3 stars in the fashion needed for each piece that is
 * committed to the board. Designed to be attached to the base scene and not
 * to be rotated with the board object.
 *
 * Created by scottc on 4/12/16.
 */
public class MoveStarsEffect extends ParticleSystem {
    private static int BUFFER_SIZE = 3;

    private class StarEffectBuffer {
        public int mActiveEmitterIndex;
        public ParticleEmitter[] mEmitters;

        // this changes the semantics of setParent, but fuck it. the parent passed in
        // will consume all emitters in the buffer directly when initializing.
        public StarEffectBuffer(ParticleSystem parent, IParticleEmissionProgram p) {
            mActiveEmitterIndex = 0;

            // allocate the buffer and create/add the emitter into the system
            mEmitters = new ParticleEmitter[BUFFER_SIZE];
            for(int x = 0; x < BUFFER_SIZE; x++) {
                mEmitters[x] = new ParticleEmitter(p);
                parent.addEmitter(mEmitters[x]);
            }
        }

        public ParticleEmitter getNextEmitter() {
            ParticleEmitter e = mEmitters[mActiveEmitterIndex];
            mActiveEmitterIndex = (mActiveEmitterIndex + 1) % BUFFER_SIZE;
            return e;
        }
    }

    // array of particle emitter buffers for each star move
    private StarEffectBuffer[] mStarEffectBuffers;

    public MoveStarsEffect() {
        super(
            // sprite texture and size information
            new TexturedPointSpriteDefinition()
                     .withBlendFunction(AdditiveTransparencyBlendFunction.getInstance())
                     .withGLTextureId(TextureManager.getInstance().getTextureID("particles"))
                     .withTextureWidth(128.0f).withPointSpriteWidth(32),
            // max particles
            3 * 3, // max_stars for three moves
            // z sorting for partial transparency
            true); // hopefully unnecessary

        // generate the emitter buffers
        mStarEffectBuffers = new StarEffectBuffer[] {
            new StarEffectBuffer(this, new OneStarMoveEmissionProgram()),
            new StarEffectBuffer(this, new TwoStarMoveEmissionProgram()),
            new StarEffectBuffer(this, new ThreeStarMoveEmissionProgram())
        };
    }

    public void fireStars(int starCount, float x, float y, float z) {
        ParticleEmitter emitter = mStarEffectBuffers[starCount-1].getNextEmitter();

        // set the emitter position to where we need to fire the stars
        emitter.setEmitterPosition(x, y, z);

        emitter.reset();
        emitter.start();
    }
}
