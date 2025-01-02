package com.scotthconner.cubetrisrebooted.cubetris.board;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.SceneObject;

/**
 * This class represents the cube, and will manage its own vertex buffer for use
 * by the CubeBoardRenderer. It does not have the capability to be rendered on its own.
 * Calling #render will render the cube's children.
 *
 * Created by scottc on 3/10/16.
 */
public class CubeInstance extends SceneObject {
    // (x, y, z), (nx, ny, nz), (r, g, b, a), (offset-x, offset-y, offset-z), (rx,ry,rz,a)
    public static int VERTEX_STRIDE = (3 + 3 + 4 + 3 + 4) * 4;

    // the float array that contains the vertex, normal, and color information
    private float[] mVertexBuffer;
    private float[] mReferenceBuffer;
    private int mVertexCount;
    private boolean mDirty;

    // movement
    private Vertex mVelocity;
    private Vertex mAcceleration;

    // rotation
    private float   mRotation;
    private float   mRotationSpeed; // degrees per second
    private Vertex mRotationAxis;

    // flash animation
    private long  mFlashElapsedMs;
    private long  mFlashDurationMs;

    // dim factor
    private float mDimTarget;
    private float mDimStart;
    private float mDimElapsed;
    private float mDimSpeed;

    public CubeInstance(float[] referenceBuffer) {
        // initialize the dimmer
        mDimTarget = 1.0f;
        mDimStart = 1.0f;
        mDimElapsed = 0.0f;
        mDimSpeed = 0.0f;

        // generate the vertex buffer and copy in the origin buffer
        mReferenceBuffer = referenceBuffer;
        mVertexBuffer =  mReferenceBuffer.clone();
        mDirty = false;
        mVertexCount = mVertexBuffer.length / (VERTEX_STRIDE/4);

        // default the mPosition to the origin
        mPosition = new Vertex();
        mVelocity = new Vertex();
        mAcceleration = new Vertex();

        // default the rotation to zero,
        mRotationAxis = new Vertex(0,1,0);
        mRotation = 0;
        mRotationSpeed = 0;

        // default the flash to off
        mFlashDurationMs = 0;
        mFlashElapsedMs = 0;
    }

    /**
     * Provides a velocity and acceleration to the cube
     *
     * @param xVelocity the x component velocity
     * @param yVelocity the y component velocity
     * @param zVelocity the z component velocity
     * @param xAccel    the x component acceleration
     * @param yAccel    the y component acceleration
     * @param zAccel    the z component acceleration
     */
    public void setTrajectory(float xVelocity, float yVelocity, float zVelocity, float xAccel, float yAccel, float zAccel) {
        mVelocity.x = xVelocity;
        mVelocity.y = yVelocity;
        mVelocity.z = zVelocity;
        mAcceleration.x = xAccel;
        mAcceleration.y = yAccel;
        mAcceleration.z = zAccel;
    }

    /**
     * Set the x y and z mPosition of the cube.
     *
     * @param x x mPosition
     * @param y y mPosition
     * @param z z mPosition
     */
    public void setPosition(float x, float y, float z) {
        mPosition.set(x, y, z);
        mDirty = true;
    }

    public void setDimTarget(float d, float dimSpeed) {
        mDimTarget = d;
        mDimSpeed = dimSpeed;
        mDimElapsed = 0;
    }

    /**
     * Sets the axis and speed of the cube's rotation
     *
     * @param x component of rotation axis
     * @param y component of rotation axis
     * @param z component of rotation axis
     * @param degrees degrees per second to spin on rotation axis
     */
    public void setRotation(float x, float y, float z, float degrees) {
        mRotationAxis.x = x;
        mRotationAxis.y = y;
        mRotationAxis.z = z;

        // dirty the buffer by setting the rotation axis
        for(int i = 0; i < mVertexCount; i++) {
            mVertexBuffer[VERTEX_STRIDE / 4 * i + 13] = mRotationAxis.x;
            mVertexBuffer[VERTEX_STRIDE / 4 * i + 14] = mRotationAxis.y;
            mVertexBuffer[VERTEX_STRIDE / 4 * i + 15] = mRotationAxis.z;
            mVertexBuffer[VERTEX_STRIDE / 4 * i + 16] = 0;
        }

        mDirty = true;
        mRotationSpeed = degrees;
    }

    @Override
    public boolean update(long msDelta) {
        float secondsDelta = (float)msDelta / 1000.0f;

        // move the cube based on its velocity
        if (mVelocity.x != 0 || mVelocity.y != 0 || mVelocity.z != 0) {
            mPosition.add(mVelocity.x * secondsDelta, mVelocity.y * secondsDelta, mVelocity.z * secondsDelta);
            mDirty = true;
        }

        // accelerate the velocity
        mVelocity.add(mAcceleration.x * secondsDelta,
                mAcceleration.y * secondsDelta,
                mAcceleration.z * secondsDelta);

        // rotate the cube based on speed
        if (mRotationSpeed != 0) {
            mRotation += mRotationSpeed * msDelta / 1000.0f;
            mDirty = true;
        }

        // this means we are in flashing mode
        if (mFlashElapsedMs != mFlashDurationMs) {
            // elapsed and duration are used to interpolate flash.
            mFlashElapsedMs += msDelta;

            if (mFlashElapsedMs >= mFlashDurationMs) {
                mFlashElapsedMs = 0;
                mFlashDurationMs = 0;
            }

            mDirty = true;
        }

        // update the dim
        if (mDimElapsed != mDimSpeed) {
            mDimElapsed += msDelta;

            if (mDimElapsed >= mDimSpeed) {
                mDimElapsed = 0;
                mDimSpeed = 0;
            }

            mDirty = true;
        }

        // update the children of the cube like a good scene object would
        updateChildren(msDelta);

        return !isDead();
    }

    @Override
    public boolean isDead() {
        return (mPosition.y < -10.0f);
    }

    @Override
    public void render(Camera camera) {
        renderChildren(camera);
    }

    /**
     * makes the cube begin to flash
     * @param millisecondDuration the number of milliseconds for the duration of the flash
     */
    public void flash(long millisecondDuration) {
        mFlashDurationMs = millisecondDuration;
        mFlashElapsedMs = 0;
    }

    public float[] getVertexBuffer() {
        calculateVertexBuffer();
        return mVertexBuffer;
    }

    private void calculateVertexBuffer() {
        if (mDirty) {
            float[] rotationBuffer = {0.0f, 0.0f, 0.0f, 1.0f};
            float[] vertexHolder   = {0.0f, 0.0f, 0.0f, 1.0f};

            float flashPercent = (mFlashDurationMs != mFlashElapsedMs) ?
                    (float)Math.sin((Math.PI - (Math.PI * ((float)mFlashElapsedMs/(float)mFlashDurationMs)))) : 0.0f;

            for(int x = 0; x < mVertexCount; x++) {
                // set the model offset
                mVertexBuffer[VERTEX_STRIDE / 4 * x + 10] = mPosition.x;
                mVertexBuffer[VERTEX_STRIDE / 4 * x + 11] = mPosition.y;
                mVertexBuffer[VERTEX_STRIDE / 4 * x + 12] = mPosition.z;

                // set the model rotation angle
                mVertexBuffer[VERTEX_STRIDE / 4 * x + 16] = mRotation;

                // update the color based on the flash percentage.. red, green, and blue
                mVertexBuffer[VERTEX_STRIDE/4 * x + 6] = mReferenceBuffer[VERTEX_STRIDE/4 * x + 6] +
                        (1.0f - mReferenceBuffer[VERTEX_STRIDE/4 * x + 6]) * flashPercent;
                mVertexBuffer[VERTEX_STRIDE/4 * x + 7] = mReferenceBuffer[VERTEX_STRIDE/4 * x + 7] +
                        (1.0f - mReferenceBuffer[VERTEX_STRIDE/4 * x + 7]) * flashPercent;
                mVertexBuffer[VERTEX_STRIDE/4 * x + 8] = mReferenceBuffer[VERTEX_STRIDE/4 * x + 8] +
                        (1.0f - mReferenceBuffer[VERTEX_STRIDE/4 * x + 8]) * flashPercent;
            }

            mDirty = false;
        }
    }

    public int getChildCount() { return mChildren.size(); }
}
