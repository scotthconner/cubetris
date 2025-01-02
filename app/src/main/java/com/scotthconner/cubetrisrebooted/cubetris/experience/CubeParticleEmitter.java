package com.scotthconner.cubetrisrebooted.cubetris.experience;

import com.scotthconner.cubetrisrebooted.cubetris.board.CubeInstance;
import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.object.particle.IParticleEmissionProgram;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleEmitter;

/**
 * Attaches a particle emitter to a cube, and will update the position.
 *
 * Created by scottc on 3/22/16.
 */
public class CubeParticleEmitter extends ParticleEmitter {
    CubeInstance mFollowCube;
    Vertex mOffset;

    public CubeParticleEmitter(IParticleEmissionProgram emissionProgram) {
        super(emissionProgram);
        mOffset = new Vertex();
    }

    @Override
    public void update(long msDelta) {
        if (null != mFollowCube ) {
            Vertex attachedPosition = mFollowCube.getPosition();
            mPosition.x = attachedPosition.x + mOffset.x;
            mPosition.y = attachedPosition.y + mOffset.y;
            mPosition.z = attachedPosition.z + mOffset.z;
        }
        super.update(msDelta);
    }

    @Override
    public void reset() {
        mFollowCube = null;
        super.reset();
    }

    public CubeParticleEmitter withFollowCube(CubeInstance cube) {
        mFollowCube = cube;
        return this;
    }

    public CubeParticleEmitter withOffset(float x, float y, float z) {
        mOffset.set(x, y, z);
        return this;
    }

    public CubeInstance getFollowCube() {
        return mFollowCube;
    }
}
