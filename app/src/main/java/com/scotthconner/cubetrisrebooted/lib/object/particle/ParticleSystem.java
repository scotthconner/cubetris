package com.scotthconner.cubetrisrebooted.lib.object.particle;

import android.opengl.GLES20;

import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.SceneObject;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderProgramLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
 * Holds all of the assets needed to render a particle system to the screen, and directly
 * integrates to the scene as a renderable. Takes in an emission program that generates and manages
 * the configuration of the active particles themselves.
 *
 * Created by scottc on 3/2/16.
 */
public class ParticleSystem extends SceneObject implements Comparator<ParticleInstance>  {
    // internal properties and state for this system
    private Vector<ParticleEmitter>        mEmitters;
    private final Vector<ParticleInstance> mActiveParticles;
    private int                            mMaxParticles;
    private boolean                        mRequiresSorting;
    private TexturedPointSpriteDefinition  mPointSpriteDefinition;

    // internal reference for the camera to sort the particles, used as part of the comparator
    private Camera mRenderCamera;

    // position buffer information
    private FloatBuffer mParticleBuffer;

    // shader handles
    private int     mProgramHandle;     // shader program id
    private int[]   mVBOID;             // buffer ID for our VBO

    private int mTextureHandle;         // shader texture handle
    private int mMVPHandle;             // shader mvp matrix handle
    private int mUVHandle;              // shader texture coordinate handles

    private int mPositionHandle;        // shader point sprite location handle
    private int mPointSizeHandle;       // shader point size handle
    private int mTextureSizeHandle;     // point size of the texture, since its a square
    private int mColorHandle;           // vertex information handle
    private int mRotationHandle;        // point sprite rotation handle

    /**
     * Creates a particle system with a given particle buffer size.
     *
     * @param def the point sprite texture definition, the texture, width, and point sprite width
     * @param maxParticles the max number of particles capable of being rendered by this system.
     * @param zSort do the particles require relative z-sorting for the camera?
     */
    public ParticleSystem(TexturedPointSpriteDefinition def, int maxParticles, boolean zSort) {
        super();
        mVBOID = new int[1];
        mEmitters = new Vector<>();
        mPointSpriteDefinition = def;
        mMaxParticles = maxParticles;
        mRequiresSorting = zSort;

        // create the particle pools and initialize the counters
        mActiveParticles = new Vector<>(mMaxParticles);

        // positions of the point sprites
        ByteBuffer bb = ByteBuffer.allocateDirect(maxParticles * ParticleInstance.PARTICLE_BUFFER_SIZE * 4);
        bb.order(ByteOrder.nativeOrder());
        mParticleBuffer = bb.asFloatBuffer();
        mParticleBuffer.position(0);

        // grab the point sprite shader program
        mProgramHandle = ShaderProgramLibrary.getInstance().getProgram("point-sprite");

        // grab the program uniform handles
        mMVPHandle         = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        mTextureHandle     = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");
        mTextureSizeHandle = GLES20.glGetUniformLocation(mProgramHandle, "uTexturePointSize");

        // grab the particle attribute handles
        mPositionHandle  = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        mColorHandle     = GLES20.glGetAttribLocation(mProgramHandle, "aColor");
        mPointSizeHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPointSize");
        mRotationHandle  = GLES20.glGetAttribLocation(mProgramHandle, "aTextureRotation");
        mUVHandle        = GLES20.glGetAttribLocation(mProgramHandle, "aTextCoord");

        // generate a vertex buffer object ID
        GLES20.glGenBuffers(1, mVBOID, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mParticleBuffer.capacity() * 4,
                mParticleBuffer, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // reset the particle
        reset();
    }

    @Override
    public boolean update(long msDelta) {
        // update each of the emitters.
        for(ParticleEmitter emitter : mEmitters) {
            emitter.update(msDelta);
        }

        synchronized(mActiveParticles) {
            // update each of the active particles, sending each dead one
            // back to their owner emitter
            Iterator<ParticleInstance> i = mActiveParticles.iterator();
            while(i.hasNext()) {
                ParticleInstance p = i.next();
                p.update(msDelta);
                if (p.isDead()) {
                    p.reap();   // send the particle back to its emitter's free pool
                    i.remove(); // remove the particle reference from the active pool
                }
            }
        }

        // the particle system is never invalid.
        return true;
    }

    @Override
    public void render(Camera camera) {
        // do nothing if we have no active particles to render
        if (mActiveParticles.size() == 0) return;

        float[] mvp = camera.calculateMVP();
        mRenderCamera = camera;

        mParticleBuffer.position(0);
        synchronized(mActiveParticles) {
            // sort the particles from back to front if the emission program wants it
            if (mRequiresSorting) { Collections.sort(mActiveParticles, this); }

            // copy each particle into the buffer
            for (ParticleInstance particle : mActiveParticles) {
                mParticleBuffer.put(particle.getVertexBuffer(), 0, ParticleInstance.PARTICLE_BUFFER_SIZE);
            }
        }
        mParticleBuffer.position(0);

        // set the blending mode
        if (null != mPointSpriteDefinition.getBlendFunction()) {
            mPointSpriteDefinition.getBlendFunction().enable();
        }

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgramHandle);

        // set the model view projection matrix
        GLES20.glUniformMatrix4fv(mMVPHandle, 1, false, mvp, 0);

        // bind the texture to the 0 slot
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPointSpriteDefinition.getGLTextureId());
        GLES20.glUniform1i(mTextureHandle, 0);

        // inform the shader how big the texture size is (needs to be square)
        GLES20.glUniform1f(mTextureSizeHandle, mPointSpriteDefinition.getTextureWidth());

        // enable and copy in the vertex buffer object
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[0]);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mActiveParticles.size() * ParticleInstance.PARTICLE_BUFFER_SIZE * 4,
                mParticleBuffer);

        // configure the position vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                ParticleInstance.PARTICLE_BUFFER_SIZE * 4, 0);

        // configure the color vertex attribute array
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
                ParticleInstance.PARTICLE_BUFFER_SIZE * 4, ParticleInstance.RED * 4);

        // configure the scale vertex attribute array
        GLES20.glEnableVertexAttribArray(mPointSizeHandle);
        GLES20.glVertexAttribPointer(mPointSizeHandle, 1, GLES20.GL_FLOAT, false,
                ParticleInstance.PARTICLE_BUFFER_SIZE * 4, ParticleInstance.S * 4);

        // configure the texture rotation attribute array
        GLES20.glEnableVertexAttribArray(mRotationHandle);
        GLES20.glVertexAttribPointer(mRotationHandle, 1, GLES20.GL_FLOAT, false,
                ParticleInstance.PARTICLE_BUFFER_SIZE * 4, ParticleInstance.R * 4);

        // configure the uv coordinate attribute array
        GLES20.glEnableVertexAttribArray(mUVHandle);
        GLES20.glVertexAttribPointer(mUVHandle, 2, GLES20.GL_FLOAT, false,
                ParticleInstance.PARTICLE_BUFFER_SIZE * 4, ParticleInstance.U * 4);

        // Draw the point sprites
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mActiveParticles.size());

        // Disable vertex array
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mPointSizeHandle);
        GLES20.glDisableVertexAttribArray(mRotationHandle);
        GLES20.glDisableVertexAttribArray(mUVHandle);

        // disable the blending mode
        if (null != mPointSpriteDefinition.getBlendFunction()) {
            mPointSpriteDefinition.getBlendFunction().disable();
        }
    }

    /**
     * Add an emitter to the particle system
     *
     * @param emitter add ownership of this emitter to the particle system.
     * @returns the object that was passed in.
     */
    public ParticleEmitter addEmitter(ParticleEmitter emitter) {
        mEmitters.add(emitter);
        emitter.setParentSystem(this);

        return emitter;
    }

    /**
     * An emitter can add an active particle to this pool, but its best if that emitter
     * belongs to this pool.
     *
     * @param p the active particle reference to add to rendering and updating
     */
    public void addActiveParticle(ParticleInstance p) {
        synchronized(mActiveParticles) {
            mActiveParticles.add(p);
        }
    }

    public void reset() {
        // clear the active pool and put them in the available pools
        synchronized(mActiveParticles) {
            if (!mActiveParticles.isEmpty()) {
                for(ParticleInstance p : mActiveParticles) { p.reap(); }
            }
        }

        // reset each of the emitters
        for(ParticleEmitter emitter : mEmitters) {
            emitter.reset();
        }

        mActiveParticles.clear();
    }

    public void clearEmitters() { mEmitters.clear(); }

    public void deleteVertexBuffer() { GLES20.glDeleteBuffers(1, mVBOID, 0); }

    public void cleanup() {}

    @Override
    public int compare(ParticleInstance p1, ParticleInstance p2) {
        float[] cameraPos = mRenderCamera.getTransformedEyePosition();

        // calculate distances from each particle to the camera
        float[] p1b = p1.getVertexBuffer();
        float xdiff = cameraPos[0] - p1b[ParticleInstance.X];
        float ydiff = cameraPos[1] - p1b[ParticleInstance.Y];
        float zdiff = cameraPos[2] - p1b[ParticleInstance.Z];
        double p1d = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));

        float[] p2b = p2.getVertexBuffer();
        xdiff = cameraPos[0] - p2b[ParticleInstance.X];
        ydiff = cameraPos[1] - p2b[ParticleInstance.Y];
        zdiff = cameraPos[2] - p2b[ParticleInstance.Z];
        double p2d = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));

        // the one that is further away is "first"
        if (p1d > p2d) {
            return -1;
        } else if (p1d == p2d) {
            return 0;
        }
        return 1;
    }
}
