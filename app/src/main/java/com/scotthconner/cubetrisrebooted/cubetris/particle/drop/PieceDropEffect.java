package com.scotthconner.cubetrisrebooted.cubetris.particle.drop;

import com.scotthconner.cubetrisrebooted.cubetris.board.CubeInstance;
import com.scotthconner.cubetrisrebooted.cubetris.experience.CubeParticleEmitter;
import com.scotthconner.cubetrisrebooted.cubetris.particle.drop.CommitSmokeEmissionProgram;
import com.scotthconner.cubetrisrebooted.cubetris.particle.drop.DropFlameEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.core.TextureManager;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleEmitter;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleSystem;
import com.scotthconner.cubetrisrebooted.lib.object.particle.TexturedPointSpriteDefinition;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.AdditiveTransparencyBlendFunction;

import java.util.Iterator;

/**
 *
 *
 * Created by scottc on 3/21/16.
 */
public class PieceDropEffect extends ParticleSystem {
    private CubeParticleEmitter[] mFlames;
    private CubeParticleEmitter[] mSmokes;

    public PieceDropEffect() {
        super(  // sprite texture and size information
                new TexturedPointSpriteDefinition()
                    .withBlendFunction(AdditiveTransparencyBlendFunction.getInstance())
                    .withGLTextureId(TextureManager.getInstance().getTextureID("particles"))
                    .withTextureWidth(128.0f).withPointSpriteWidth(32),
                // max particles
                (DropFlameEmissionProgram.getInstance().getMaxParticleCount() * 4) +
                (CommitSmokeEmissionProgram.getInstance().getMaxParticleCount() * 4),
                // z sorting for partial transparency
                true);

        mFlames = new CubeParticleEmitter[] {
                (new CubeParticleEmitter(DropFlameEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(DropFlameEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(DropFlameEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(DropFlameEmissionProgram.getInstance()).withOffset(0, -0.6f, 0))
        };

        mSmokes = new CubeParticleEmitter[] {
                (new CubeParticleEmitter(CommitSmokeEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(CommitSmokeEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(CommitSmokeEmissionProgram.getInstance()).withOffset(0, -0.6f, 0)),
                (new CubeParticleEmitter(CommitSmokeEmissionProgram.getInstance()).withOffset(0, -0.6f, 0))
        };

        // add the emitters to this system.
        for(ParticleEmitter e : mFlames) { addEmitter(e); }
        for(ParticleEmitter e : mSmokes) { addEmitter(e); }
    }

    /**
     * Starts the flames on the given cubes.
     * @param cubes the cubes that are exposed downward from the board.
     */
    public void startFlames(Iterator<CubeInstance> cubes) {
        int flameCount = 0;
        while(cubes.hasNext()) {
            CubeInstance c = cubes.next();

            // see if this flame was actually attached to something,
            // and then attempt to remove it.
            if (null == mParent) {
                // set the cube parent to the first one we find, so the system
                // will be rendered within this cube's stage of the scene graph
                c.addChild(this.withParent(c));
            }

            // now with the cube parent set, attach and start emitter to the cube in question
            mFlames[flameCount].withFollowCube(c).start();

            // increment the counter so we grab another emitter
            flameCount++;
        }
    }

    public void pauseFlames() {
        for(ParticleEmitter emitter : mFlames) { emitter.pause(); }
    }

    public void startDust() {
        int dustCount = 0;
        for(CubeParticleEmitter emitter : mFlames) {
            if (emitter.getFollowCube() != null) {
                mSmokes[dustCount].reset();
                mSmokes[dustCount].withFollowCube(emitter.getFollowCube()).start();
                dustCount++;
            }
        }
    }

    @Override
    public void reset() {
        // detach the particle system from its parent cube
        if (null != mParent) {
            mParent.removeChild(this.withParent(null));
        }
        super.reset();
    }
}
