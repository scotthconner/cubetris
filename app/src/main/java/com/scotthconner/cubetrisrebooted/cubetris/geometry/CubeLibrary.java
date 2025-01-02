package com.scotthconner.cubetrisrebooted.cubetris.geometry;

import android.graphics.Color;
import android.graphics.Point;

import java.util.Random;

/**
 * Used to as a memory manager for gaining static references to initialized cube geometries.
 * This allows CubetrisExperience objects to share the same cube vertex buffers across players.
 *
 * Created by scottc on 3/12/16.
 */
public class CubeLibrary {
    // Cube Vertex Definitions /////////////////////////////////////////
    public static final Point[] CUBE_COLORS = {
            new Point(Color.rgb(150, 10, 0), Color.rgb(255,255,255)),   // red
            new Point(Color.rgb(4,70,0), Color.rgb(255,255,255)),       // green
            new Point(Color.rgb(11,80,156), Color.rgb(255,255,255)),    // blue
            new Point(Color.rgb(104,0,133), Color.rgb(255,255,255))     // purple
    };
    ////////////////////////////////////////////////////////////////////

    // singleton instance /////////////////////////////////////////////
    private static CubeLibrary mInstance = null;

    private float[][] mCubes;
    private Random mRandom;

    public static CubeLibrary getInstance() {
        if (null == mInstance) {
            mInstance = new CubeLibrary();
        }
        return mInstance;
    }
    ////////////////////////////////////////////////////////////////////

    /**
     * Goes through each of the cube color definitions and generates
     * the vertex buffer objects for them.
     */
    public void init() {
        for(int x = 0; x < CUBE_COLORS.length; x++) {
            Point cubeColor = CUBE_COLORS[x];
            mCubes[x] = allocateOriginBuffer(cubeColor.x, cubeColor.y);
        }
    }

    /**
     * Returns a Random Cube vertex buffer
     * @return a reference to a cube vertex buffer object
     */
    public float[] getRandomCubeBuffer() {
        return mCubes[mRandom.nextInt(CUBE_COLORS.length)];
    }

    private CubeLibrary() {
        mCubes  = new float[CUBE_COLORS.length][];
        mRandom = new Random();
    }

    private float[] allocateOriginBuffer(int color, int mColor) {
        float r = Color.red(color) / 255.0f;
        float g = Color.green(color) / 255.0f;
        float b = Color.blue(color) / 255.0f;

        float mr = Color.red(mColor) / 255.0f;
        float mg = Color.green(mColor) / 255.0f;
        float mb = Color.green(mColor) / 255.0f;

        return new float[]{
                // front
                0.0f, 0.0f, -0.5f,   // 0
                0.0f, 0.0f, -1.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, -0.5f, // 1
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, -0.5f,  // 4
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, -0.5f, -0.5f,  // 2
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, -0.5f, // 1
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.0f, -0.5f,   // 0
                0.0f, 0.0f, -1.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.0f, 0.0f, -0.5f,   // 0
                0.0f, 0.0f, -1.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, -0.5f,   // 3
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, -0.5f,  // 2
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, -0.5f,  // 4
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, -0.5f,   // 3
                0.0f, 0.0f, -1.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.0f, -0.5f,   // 0
                0.0f, 0.0f, -1.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // left
                -0.5f, 0.0f, 0.0f,   // 7
                -1.0f, 0.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, 0.5f,  // 8
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, 0.5f,   // 5
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, -0.5f, -0.5f, // 9
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, 0.5f,  // 8
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.0f, 0.0f,   // 7
                -1.0f, 0.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, -0.5f,  // 6
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, -0.5f, // 9
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.0f, 0.0f,   // 7
                -1.0f, 0.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, 0.5f,   // 5
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, -0.5f,  // 6
                -1.0f, 0.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.0f, 0.0f,   // 7
                -1.0f, 0.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // right
                0.5f, -0.5f, 0.5f,   // 11
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, -0.5f,  // 10
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.0f, 0.0f,    // 12
                1.0f, 0.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, 0.5f, 0.5f,    // 14
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, 0.5f,   // 11
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.0f, 0.0f,    // 12
                1.0f, 0.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, 0.5f, -0.5f,   // 13
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, 0.5f,    // 14
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.0f, 0.0f,    // 12
                1.0f, 0.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, -0.5f, -0.5f,  // 10
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, -0.5f,   // 13
                1.0f, 0.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.0f, 0.0f,    // 12
                1.0f, 0.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // top
                0.5f, 0.5f, -0.5f,   // 16
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, -0.5f,  // 15
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.5f, 0.0f,    // 17
                0.0f, 1.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, 0.5f, 0.5f,    // 19
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, -0.5f,   // 16
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.5f, 0.0f,    // 17
                0.0f, 1.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, 0.5f,   // 18
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, 0.5f,    // 19
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.5f, 0.0f,    // 17
                0.0f, 1.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, 0.5f, -0.5f,  // 15
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, 0.5f,   // 18
                0.0f, 1.0f, 0.0f,    // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, 0.5f, 0.0f,    // 17
                0.0f, 1.0f, 0.0f,    // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // bottom
                -0.5f, -0.5f, 0.5f,  // 23
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, -0.5f, // 20
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, -0.5f, 0.0f,   // 22
                0.0f, -1.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, -0.5f, 0.5f,   // 24
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, 0.5f,  // 23
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, -0.5f, 0.0f,   // 22
                0.0f, -1.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.5f, -0.5f, -0.5f,  // 21
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, 0.5f,   // 24
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, -0.5f, 0.0f,   // 22
                0.0f, -1.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -0.5f, -0.5f, -0.5f, // 20
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, -0.5f,  // 21
                0.0f, -1.0f, 0.0f,   // n
                r, g, b, 1.0f,       // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.0f, -0.5f, 0.0f,   // 22
                0.0f, -1.0f, 0.0f,   // n
                mr, mg, mb, 1f,      // c
                0.0f, 0.0f, 0.0f,    // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // back
                0.0f, 0.0f, 0.5f,   // 27
                0.0f, 0.0f, 1.0f,   // n
                mr, mg, mb, 1f,     // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, 0.5f,  // 28
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, 0.5f, // 26
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.0f, 0.0f, 0.5f,   // 27
                0.0f, 0.0f, 1.0f,   // n
                mr, mg, mb, 1f,     // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, -0.5f, 0.5f, // 26
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, 0.5f,  // 25
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.0f, 0.0f, 0.5f,   // 27
                0.0f, 0.0f, 1.0f,   // n
                mr, mg, mb, 1f,     // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, -0.5f, 0.5f,  // 25
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, 0.5f,   // 29
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                0.0f, 0.0f, 0.5f,   // 27
                0.0f, 0.0f, 1.0f,   // n
                mr, mg, mb, 1f,     // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                0.5f, 0.5f, 0.5f,   // 29
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -0.5f, 0.5f, 0.5f,  // 28
                0.0f, 0.0f, 1.0f,   // n
                r, g, b, 1.0f,      // c
                0.0f, 0.0f, 0.0f,   // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
        };
    }
}
