package com.scotthconner.cubetrisrebooted.cubetris.geometry;

import java.util.Vector;

/**
 * Holds all the geometry for the experience sky box, including the line grid.
 *
 * Created by scottc on 3/31/16.
 */
public class ExperienceSkyBox {
    public static final float ROOM_RADIUS = 15.0f;
    public static final int ROOM_HEIGHT = 20;
    public static final float ROOM_FLOOR = -0.52f;

    // GRID LINES //////////////////////////////////////////////////////
    public static int X     = 0;
    private static int Y     = 1;
    private static int Z     = 2;
    public static int RED   = 3;               // red component color for vertex
    private static int GREEN = 5;               // green component color for vertex
    private static int BLUE  = 6;               // blue component color for vertex
    private static int ALPHA = 7;
    public  static int VERTEX_FLOAT_COUNT = 8;

    private class GridLine {
        public float[] mVertexBuffer;
        public GridLine() {
                mVertexBuffer = new float[VERTEX_FLOAT_COUNT * 2];
        }

        public GridLine withStartPosition(float x, float y, float z) {
                mVertexBuffer[X] = x;
                mVertexBuffer[Y] = y;
                mVertexBuffer[Z] = z;
                return this;
        }

        public GridLine withEndPosition(float x, float y, float z) {
            mVertexBuffer[VERTEX_FLOAT_COUNT + X] = x;
            mVertexBuffer[VERTEX_FLOAT_COUNT + Y] = y;
            mVertexBuffer[VERTEX_FLOAT_COUNT + Z] = z;
            return this;
        }

        public GridLine withStartColor(float red, float green, float blue, float alpha) {
               mVertexBuffer[RED]   = red;
               mVertexBuffer[GREEN] = green;
               mVertexBuffer[BLUE]  = blue;
               mVertexBuffer[ALPHA] = alpha;
               return this;
        }

        public GridLine withEndColor(float red, float green, float blue, float alpha) {
            mVertexBuffer[VERTEX_FLOAT_COUNT + RED]   = red;
            mVertexBuffer[VERTEX_FLOAT_COUNT + GREEN] = green;
            mVertexBuffer[VERTEX_FLOAT_COUNT + BLUE]  = blue;
            mVertexBuffer[VERTEX_FLOAT_COUNT + ALPHA] = alpha;
            return this;
        }
    }
    ////////////////////////////////////////////////////////////////////

    // singleton instance /////////////////////////////////////////////
    private static ExperienceSkyBox mInstance = null;

    private float[]          mExperienceWalls; // the walls that take light
    private Vector<GridLine> mGridWalls;       // the grid vertexes for the walls
    private static int WALL_COUNT = 4;
    private static int WALL_GRID_WIDTH  = 2 * (int)ROOM_RADIUS;
    public static int GRID_LINE_COUNT  = WALL_COUNT * ((WALL_GRID_WIDTH) + ((int)ROOM_HEIGHT));
    private static int LINE_BUFFER_SIZE = VERTEX_FLOAT_COUNT * 2 * GRID_LINE_COUNT;

    public static ExperienceSkyBox getInstance() {
            if (null == mInstance) {
                    mInstance = new ExperienceSkyBox();
            }
            return mInstance;
    }
    ////////////////////////////////////////////////////////////////////

    private ExperienceSkyBox() {}

    public void init() {
        float r = 16.0f / 255.0f;
        float g = 16.0f / 255.0f;
        float b = 120.0f / 255.0f;

        mGridWalls = new Vector<>();
        // generate vertical lines
        for(int x = 0; x < ROOM_RADIUS * 0.75f * 2; x+=2) {
            // front wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f + x, ROOM_FLOOR + 0.1f, ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(-ROOM_RADIUS * 0.75f + x, ROOM_HEIGHT, ROOM_RADIUS * 0.75f).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // back wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f + x, ROOM_FLOOR + 0.1f, -ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(-ROOM_RADIUS * 0.75f + x, ROOM_HEIGHT, -ROOM_RADIUS * 0.75f).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // right wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(ROOM_RADIUS * 0.75f, ROOM_FLOOR, ROOM_RADIUS * 0.75f - x).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(ROOM_RADIUS * 0.75f, ROOM_HEIGHT, ROOM_RADIUS * 0.75f - x).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // left wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f, ROOM_FLOOR, ROOM_RADIUS * 0.75f - x).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(-ROOM_RADIUS * 0.75f, ROOM_HEIGHT, ROOM_RADIUS * 0.75f - x).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));
        }

        // generate horizontal lines
        for(int y = 0; y < ROOM_HEIGHT; y+=2) {
            // front wall
           mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, ROOM_RADIUS * 0.75f).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // back wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, -ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, -ROOM_RADIUS * 0.75f).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // right wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, -ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, ROOM_RADIUS * 0.75f).withEndColor(1.0f, 1.0f, 1.0f, 1.0f));

            // left wall
            mGridWalls.add(new GridLine()
                               .withStartPosition(-ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, -ROOM_RADIUS * 0.75f).withStartColor(1.0f, 1.0f, 1.0f, 1.0f)
                               .withEndPosition(-ROOM_RADIUS * 0.75f, ROOM_FLOOR + 0.1f + y, ROOM_RADIUS * 0.75f).withEndColor(1.0f,1.0f,1.0f,1.0f));
        }

        mExperienceWalls = new float[]{
                // back
                -ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,    //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, -ROOM_RADIUS,  //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,    //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_HEIGHT, -ROOM_RADIUS,   //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f,  // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, -ROOM_RADIUS,  //
                0.0f, 0.0f, 1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // left
                -ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, -ROOM_RADIUS,   //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,   //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,   //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // right
                ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,   //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_HEIGHT, -ROOM_RADIUS,   //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,   //
                -1.0f, 0.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                // front
                ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,   //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,  //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,  //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_HEIGHT, ROOM_RADIUS,   //
                0.0f, 0.0f, -1.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f,  // rotation

                // bottom
                -ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation

                -ROOM_RADIUS, ROOM_FLOOR, -ROOM_RADIUS,   //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                -ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
                ROOM_RADIUS, ROOM_FLOOR, ROOM_RADIUS,    //
                0.0f, 1.0f, 0.0f,       // n
                r, g, b, 1f,            // c
                0.0f, 0.0f, 0.0f,       // offset
                0.0f, 1.0f, 0.0f, 0.0f, // rotation
        };
    }

    public float[] getWallBuffer() {
        return mExperienceWalls;
    }

    public float[] calculateLineBuffer() {
        float[] b = new float[LINE_BUFFER_SIZE];
        int x = 0;
        for(GridLine line : mGridWalls) {
            System.arraycopy(line.mVertexBuffer, 0, b, VERTEX_FLOAT_COUNT * 2 * x, VERTEX_FLOAT_COUNT *2);
            x++;
        }
        return b;
    }
}
