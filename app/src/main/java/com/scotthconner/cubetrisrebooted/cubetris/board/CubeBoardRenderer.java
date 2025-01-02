package com.scotthconner.cubetrisrebooted.cubetris.board;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.scotthconner.cubetrisrebooted.cubetris.geometry.CubeLibrary;
import com.scotthconner.cubetrisrebooted.cubetris.geometry.ExperienceSkyBox;
import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.Light;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderHelper;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderProgramLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;

/**
 * Contains the vertex buffer object for the cube board.
 *
 * Created by scottc on 3/12/16.
 */
public class CubeBoardRenderer {
    private int mMaxCubeCount;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mLineBuffer;
    private int mVBOID[];
    private float[] mvm;
    private float[] mModelMatrix;

    // program and shader handles for cubes and walls
    private int renderProgram;
    private int vertexHandle;   // attribute
    private int colorHandle;    // attribute
    private int normalHandle;   // attribute
    private int offsetHandle;   // attribute
    private int rotationHandle; // attribute
    private int mvpHandle;
    private int mvmHandle;
    private int lightHandle;
    private int lightHandle2;
    private int lightHandle3;
    private int ambientHandle;
    private int attenuationHandle;
    private int attenuationHandle2;
    private int attenuationHandle3;
    private int lightDimmerHandle;
    private int lightDimmerHandle2;
    private int lightDimmerHandle3;
    private int modelMatrixHandle;

    // program and shander handles for lines
    private int lineRenderProgram;
    private int lineVertexHandle;
    private int lineColorHandle;
    private int lineMVPHandle;
    private int lineModelMatrixHandle;

    public CubeBoardRenderer(int maxCubeCount) {
        mMaxCubeCount = maxCubeCount;
        mVBOID = new int[1];
        mvm = new float[16];
        mModelMatrix = new float[16];

        // vertex buffer for walls
        ByteBuffer bb = ByteBuffer.allocateDirect(
                mMaxCubeCount * (CubeLibrary.getInstance().getRandomCubeBuffer().length) * ShaderHelper.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();

        // vertex buffer for lines
        float[] lineBuffer = ExperienceSkyBox.getInstance().calculateLineBuffer();
        ByteBuffer lb = ByteBuffer.allocateDirect(lineBuffer.length * ShaderHelper.BYTES_PER_FLOAT);
        lb.order(ByteOrder.nativeOrder());
        mLineBuffer = bb.asFloatBuffer();
        mLineBuffer.position(0);
        mLineBuffer.put(lineBuffer);
        mLineBuffer.position(0);

        // create the VBO for the wall
        mVBOID = new int[2];
        GLES20.glGenBuffers(2, mVBOID, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * 4,
                               mVertexBuffer, GLES20.GL_DYNAMIC_DRAW);

        // create the VBO for the lines
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mLineBuffer.capacity() * 4,
                               mLineBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // grab the cube and wall shader handles
        renderProgram  = ShaderProgramLibrary.getInstance().getProgram("color-light");
        mvpHandle      = GLES20.glGetUniformLocation(renderProgram, "uMVPMatrix");
        vertexHandle   = GLES20.glGetAttribLocation(renderProgram, "aPosition");
        colorHandle    = GLES20.glGetAttribLocation(renderProgram, "aColor");
        normalHandle   = GLES20.glGetAttribLocation(renderProgram, "aNormal");
        offsetHandle   = GLES20.glGetAttribLocation(renderProgram, "aModelOffset");
        rotationHandle = GLES20.glGetAttribLocation(renderProgram, "aRotation");

        mvmHandle = GLES20.glGetUniformLocation(renderProgram, "uMVMatrix");
        ambientHandle = GLES20.glGetUniformLocation(renderProgram, "uAmbientFactor");

        lightHandle = GLES20.glGetUniformLocation(renderProgram, "uLightPos");
        lightHandle2 = GLES20.glGetUniformLocation(renderProgram, "uLightPos2");
        lightHandle3 = GLES20.glGetUniformLocation(renderProgram, "uLightPos3");

        attenuationHandle = GLES20.glGetUniformLocation(renderProgram, "uLightAttenuation");
        attenuationHandle2 = GLES20.glGetUniformLocation(renderProgram, "uLightAttenuation2");
        attenuationHandle3 = GLES20.glGetUniformLocation(renderProgram, "uLightAttenuation3");

        lightDimmerHandle = GLES20.glGetUniformLocation(renderProgram, "uLightDimmer");
        lightDimmerHandle2 = GLES20.glGetUniformLocation(renderProgram, "uLightDimmer2");
        lightDimmerHandle3 = GLES20.glGetUniformLocation(renderProgram, "uLightDimmer3");

        modelMatrixHandle = GLES20.glGetUniformLocation(renderProgram, "uModelMatrix");

        // grab the shader for the grid
        lineRenderProgram = ShaderProgramLibrary.getInstance().getProgram("color");
        lineMVPHandle     = GLES20.glGetUniformLocation(lineRenderProgram, "uMVPMatrix");
        lineVertexHandle  = GLES20.glGetAttribLocation(lineRenderProgram, "aPosition");
        lineColorHandle   = GLES20.glGetAttribLocation(lineRenderProgram, "aColor");
        lineModelMatrixHandle = GLES20.glGetUniformLocation(lineRenderProgram, "uModelMatrix");
    }

    public void render(Camera camera, CubeBoard cb) {
        Scene scene = cb.getScene();
        CubeInstance[][] cubeBoard = cb.getCubeInstances();
        Iterator<CubeInstance> extras = cb.getExtraCubes();
        CubeBoardPiece activePiece = cb.getActivePiece();
        mVertexBuffer.position(0);

        // pull all of the vertex data into the buffer for board
        int boardTriangleCount = 0;
        int fallingTriangleCount = 0;
        int activePieceTriangleCount = 0;
        for(int x = 0; x < cubeBoard.length; x++) {
            for(int y = 0; y < cubeBoard[x].length; y++) {
                if (null != cubeBoard[x][y]) {
                    mVertexBuffer.put(cubeBoard[x][y].getVertexBuffer());
                    boardTriangleCount += 24; // 24 triangles per cube
                }
            }
        }

        // also go ahead and add the experience walls to the board triangle count
        float[] wallBuffer = ExperienceSkyBox.getInstance().getWallBuffer();
        mVertexBuffer.put(wallBuffer);
        boardTriangleCount += 10;

        // pull in the extra cubes too
        while(extras.hasNext()) {
            mVertexBuffer.put(extras.next().getVertexBuffer());
            fallingTriangleCount += 24; // 24 triangles per cube
        }

        // determine if there is an active piece and add those triangles
        if (null != activePiece) {
            float[][] activeCubes = activePiece.getVertexBuffers();
            for(int x = 0; x < activeCubes.length; x++) {
                mVertexBuffer.put(activeCubes[x]);
                activePieceTriangleCount += 24;
            }
        }
        mVertexBuffer.position(0);

        // load the program, and the vertex buffer object
        GLES20.glUseProgram(renderProgram);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[0]);

        // enable and copy in the vertex buffer object
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0,
                                  (boardTriangleCount + activePieceTriangleCount + fallingTriangleCount) * 3 * CubeInstance.VERTEX_STRIDE,
                                  mVertexBuffer);

        // pass in the model vertex information
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glVertexAttribPointer(vertexHandle, 3,
                GLES20.GL_FLOAT, false, CubeInstance.VERTEX_STRIDE, 0);

        // pass in the vertex normal information
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, 3,
                GLES20.GL_FLOAT, false, CubeInstance.VERTEX_STRIDE, 12);

        // pass in the vertex color information,
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                                        GLES20.GL_FLOAT, false, CubeInstance.VERTEX_STRIDE, 24);

        // pass in the vertex offset information,
        GLES20.glEnableVertexAttribArray(offsetHandle);
        GLES20.glVertexAttribPointer(offsetHandle, 3,
                                        GLES20.GL_FLOAT, false, CubeInstance.VERTEX_STRIDE, 40);

        // pass in the vertex offset information,
        GLES20.glEnableVertexAttribArray(rotationHandle);
        GLES20.glVertexAttribPointer(rotationHandle, 4,
                                        GLES20.GL_FLOAT, false, CubeInstance.VERTEX_STRIDE, 52);

        // calculate and pass in the model view matrix for per pixel lighting
        Matrix.multiplyMM(mvm, 0, camera.getViewMatrix(), 0, camera.getCurrentModelMatrix(), 0);
        GLES20.glUniformMatrix4fv(mvmHandle, 1, false, mvm, 0);
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, camera.getCurrentModelMatrix(), 0);

        // set the light position based on eye space for the sun
        Vertex slp = scene.getSceneSun().position;
        float[] lpes = {0f,0f,0f,1.0f};
        float[] lightPositionWorldSpace = {slp.x, slp.y, slp.z, 1.0f};
        Matrix.multiplyMV(lpes, 0, camera.getViewMatrix(), 0, lightPositionWorldSpace, 0);
        GLES20.glUniform3f(lightHandle, lpes[0], lpes[1], lpes[2]);
        GLES20.glUniform1f(attenuationHandle, scene.getSceneSun().attenuation);
        GLES20.glUniform1f(lightDimmerHandle, scene.getSceneSun().dimmer);

        // set the light position of the active piece, translated to the board space
        Light activePieceLight = scene.getLight("active1");
        slp = activePieceLight.position;
        float[] lightPositionWorldSpace2 = {slp.x, slp.y, slp.z, 1.0f};
        Matrix.multiplyMV(lpes, 0, camera.getViewMatrix(), 0, lightPositionWorldSpace2, 0);
        GLES20.glUniform3f(lightHandle2, lpes[0], lpes[1], lpes[2]);
        GLES20.glUniform1f(attenuationHandle2, activePieceLight.attenuation);
        GLES20.glUniform1f(lightDimmerHandle2, activePieceLight.dimmer);

        // set the light position of the active piece, do the same for the previous
        activePieceLight = scene.getLight("active2");
        slp = activePieceLight.position;
        float[] lightPositionWorldSpace3 = {slp.x, slp.y, slp.z, 1.0f};
        Matrix.multiplyMV(lpes, 0, camera.getViewMatrix(), 0, lightPositionWorldSpace3, 0);
        GLES20.glUniform3f(lightHandle3, lpes[0], lpes[1], lpes[2]);
        GLES20.glUniform1f(attenuationHandle3, activePieceLight.attenuation);
        GLES20.glUniform1f(lightDimmerHandle3, activePieceLight.dimmer);

        // set the scene's ambient factor
        GLES20.glUniform1f(ambientHandle, scene.getAmbientFactor());

        // set the model view projection matrix for final rendering
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, camera.calculateMVP(), 0);

        // draw the damn thing
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, boardTriangleCount * 3);

        // hack: this assumes that the client has pushed the rotation onto the model stack
        // this prevents from having to push and pop more than once to ensure proper
        // transparency rendering.
        camera.popModelState();

        // draw the active piece if there is one
        if (activePieceTriangleCount > 0) {
            // calculate and pass in the model view matrix for per pixel lighting
            Matrix.multiplyMM(mvm, 0, camera.getViewMatrix(), 0, camera.getCurrentModelMatrix(), 0);
            GLES20.glUniformMatrix4fv(mvmHandle, 1, false, mvm, 0);
            GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, camera.getCurrentModelMatrix(), 0);

            // set the model view projection matrix for final rendering
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, camera.calculateMVP(), 0);

            // draw the active piece triangles
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, boardTriangleCount * 3, (fallingTriangleCount + activePieceTriangleCount) * 3);
        }

        // Disable vertex array
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(offsetHandle);
        GLES20.glDisableVertexAttribArray(rotationHandle);
    }

    public void renderLineGrid(Camera camera, CubeBoard cb) {
        // load the program, and the vertex buffer object
        GLES20.glUseProgram(lineRenderProgram);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBOID[1]);

        // set the model view projection matrix for final rendering
        GLES20.glUniformMatrix4fv(lineMVPHandle, 1, false, camera.calculateMVP(), 0);
        GLES20.glUniformMatrix4fv(lineModelMatrixHandle, 1, false, camera.getCurrentModelMatrix(), 0);

        // pass in the line vertex information
        GLES20.glEnableVertexAttribArray(lineVertexHandle);
        GLES20.glVertexAttribPointer(lineVertexHandle, 3,
                                        GLES20.GL_FLOAT, false, ExperienceSkyBox.VERTEX_FLOAT_COUNT * 4, 0);

        // pass in the line vertex color information,
        GLES20.glEnableVertexAttribArray(lineColorHandle);
        GLES20.glVertexAttribPointer(lineColorHandle, 4,
                                        GLES20.GL_FLOAT, false, ExperienceSkyBox.VERTEX_FLOAT_COUNT * 4,
                                        ExperienceSkyBox.RED * 4);

        // Draw the lines
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, ExperienceSkyBox.GRID_LINE_COUNT * 2);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(lineVertexHandle);
        GLES20.glDisableVertexAttribArray(lineColorHandle);
    }
}
