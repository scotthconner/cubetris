package com.scotthconner.cubetrisrebooted.lib.render.core;

import android.opengl.Matrix;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;

import java.util.Stack;

/**
 * Created by scottc on 12/26/15.
 */
public class Camera {
    private final float[]  projectionMatrix;
    private final float[]  viewMatrix;
    private final float[]  modelViewProjectionMatrix;
    public  final float[]  scratchMatrix;
    private final float[]  modelScratchMatrix;
    private final float[]  transformScratch;
    private Stack<float[]> modelMatrixStack;

    // position, eye, and up vector
    private Vertex position;
    private Vertex eye;
    private Vertex up;
    private float[] transformedPosition;

    public Camera() {
        modelMatrixStack = new Stack<>();
        projectionMatrix = new float[16];
        scratchMatrix = new float[16];
        modelScratchMatrix = new float[16];
        transformScratch = new float[16];
        viewMatrix = new float[16];
        modelViewProjectionMatrix = new float[16];

        position = new Vertex();
        eye = new Vertex();
        up = new Vertex(0, 1.0f, 0);
        transformedPosition = new float[]{0,0,0,0};

        // start with an identity matrix
        float[] identity = new float[16];
        Matrix.setIdentityM(identity, 0);
        modelMatrixStack.push(identity);
    }

    public void establishProjection(int width, int height) {
        float ratio = (float)width / (float)height;

        // create a projection matrix from device screen geometry
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, ratio, 1, 100.0f);
        calculateViewMatrix();
    }

    public void establishOrthoProjection(int width, int height) {
        float ratio = (float)width / (float)height;
        // create an orthographic projection matrix from device screen geometry
        Matrix.orthoM(projectionMatrix, 0, -width / 2.0f, width / 2.0f, -height / 2.0f, height / 2.0f, -1, 1);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 1,
                0, 0, 0,
                up.x, up.y, up.z);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        calculateTransformedCameraPosition();
        calculateViewMatrix();
    }

    private void calculateTransformedCameraPosition() {
        float[] p = {position.x, position.y, position.z, 0};

        // calculate the inverse
        Matrix.invertM(transformScratch, 0, getCurrentModelMatrix(), 0);

        Matrix.multiplyMV(transformedPosition, 0, transformScratch, 0, p, 0);
    }

    public float[] getTransformedEyePosition() {
        return transformedPosition;
    }

    public Vertex getPosition() {
        return position;
    }

    public void lookAt(float x, float y, float z) {
        eye.set(x, y, z);
        calculateViewMatrix();
    }

    public void update(long timeDelta) {

    }

    public float[] getMVPMatrix() {
        return modelViewProjectionMatrix;
    }

    public float[] getViewMatrix() { return viewMatrix; }

    public float[] getCurrentModelMatrix() { return modelMatrixStack.peek(); }

    public void pushModelState(float[] modelMatrixState) {
        float[] newState = new float[16];
        Matrix.multiplyMM(newState, 0, modelMatrixStack.peek(), 0, modelMatrixState, 0);
        modelMatrixStack.push(newState);
        calculateTransformedCameraPosition();
    }

    public void popModelState() {
        modelMatrixStack.pop();
        calculateTransformedCameraPosition();
    }

    public float[] calculateMVP(float[] modelMatrix) {
        // calculate the model view matrix
        Matrix.multiplyMM(modelScratchMatrix, 0, getCurrentModelMatrix(), 0, modelMatrix, 0);
        Matrix.multiplyMM(scratchMatrix, 0, getViewMatrix(), 0, modelScratchMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, scratchMatrix, 0);

        return getMVPMatrix();
    }

    public float[] calculateMVP() {
        Matrix.multiplyMM(scratchMatrix, 0, getViewMatrix(), 0, getCurrentModelMatrix(), 0);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, scratchMatrix, 0);

        return getMVPMatrix();
    }

    private void calculateViewMatrix() {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, position.x, position.y, position.z,
                eye.x, eye.y, eye.z,
                up.x, up.y, up.z);
    }
}
