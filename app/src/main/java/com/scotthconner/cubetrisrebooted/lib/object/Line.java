package com.scotthconner.cubetrisrebooted.lib.object;

import android.graphics.Color;
import android.opengl.GLES20;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.IRenderable;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderHelper;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderProgramLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by scottc on 2/28/16.
 */
public class Line implements IRenderable {
    Scene mScene;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer mVertexBuffer;
    protected int mProgramHandle;
    protected int mPositionHandle;
    protected int mColorHandle;
    protected int mMVPHandle;

    private float mLineCoords[];

    // Set color with red, green, blue and alpha (opacity) values
    private float mColor[];

    public Line(Vertex start, Vertex end, int color) {
        // set up the coordinates and the color
        mLineCoords = new float[]{
                start.x, start.y, start.z,
                end.x, end.y, end.z
        };
        mColor = new float[] { Color.red(color), Color.green(color),
                Color.blue(color), Color.alpha(color) };

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(mLineCoords.length * ShaderHelper.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mLineCoords);
        mVertexBuffer.position(0);

        mProgramHandle = ShaderProgramLibrary.getInstance().getProgram("standard");
        mMVPHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        mPositionHandle  = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vColor");
    }

    public void cleanup() {
        mColor = null;
        mLineCoords = null;
        mVertexBuffer = null;
    }

    public void setPosition(float v0, float v1, float v2, float v3, float v4, float v5) {
        mLineCoords[0] = v0;
        mLineCoords[1] = v1;
        mLineCoords[2] = v2;
        mLineCoords[3] = v3;
        mLineCoords[4] = v4;
        mLineCoords[5] = v5;
        mVertexBuffer.put(mLineCoords);
        mVertexBuffer.position(0);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        mColor[0] = red;
        mColor[1] = green;
        mColor[2] = blue;
        mColor[3] = alpha;
    }

    @Override
    public boolean update(long msDelta) {
        return true;
    }

    @Override
    public void render(Camera camera) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgramHandle);

        // Enable a handle to the line vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the line coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPHandle, 1, false, camera.calculateMVP(), 0);

        // Draw the line
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    @Override
    public void setScene(Scene scene) {
        mScene = scene;
    }

    @Override
    public Scene getScene() {
        return mScene;
    }
}
