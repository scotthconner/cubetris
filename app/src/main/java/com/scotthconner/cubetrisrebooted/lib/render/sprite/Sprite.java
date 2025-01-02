package com.scotthconner.cubetrisrebooted.lib.render.sprite;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderHelper;
import com.scotthconner.cubetrisrebooted.lib.render.shader.ShaderProgramLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Sprite is an instance that contains a texture reference, the geometry,
 * reference to the shader program, the UV coordinates, and size of a sprite image.
 *
 * Should be initalized only once per definition. Sprites can be used by multiple renderables,
 * and can be rendered to the screen via reference with input positions.
 *
 * Created by scottc on 2/18/16.
 */
public class Sprite {
    private static final int COORDS_PER_VERTEX     =                                 3;
    private static final int ELEMENTS_PER_UV_COORD =                                 2;

    /**
     * Stores the texture definition of the sprite.
     */
    public static class Definition {
        public int mTextureId; // OpenGL Texture ID.
        public float uvStartX;  // the texture coordinate x start position
        public float uvStartY;  // the texture coordinate y start position
        public float uvEndX;    // the texture coordinate x end position
        public float uvEndY;    // the texture coordinate y end position
        public float mSizeX;    // the pixel width of the sprite
        public float mSizeY;    // the pixel height of the sprite

        public boolean mCentered; // centered on its size or not, otherwise left bottom corner justified
    }

    Definition mSpriteDefinition;

    // geometry information
    private static short[] mDrawOrder = {0, 1, 2, 0, 3, 1}; // what order to draw the triangles
    private float[] mVertices;                              // the x,y,z of each vertex
    private float[] mUVs;                                   // U and V texture coords for each vertex

    // hardware buffers
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mUVBuffer;
    private ShortBuffer mDrawOrderBuffer;

    // shader handles
    private int mProgramHandle;
    private int mMVPHandle;
    private int mVertexHandle;
    private int mUVHandle;
    private int mTextureHandle;

    /**
     *
     * @param def definition of the sprite's texture and its coordinates
     */
    public Sprite(Definition def) {
        mUVs = new float[8];       // 2 components for 4 corners
        mSpriteDefinition = def;

        // create the verticies, centered around 0,0
        if( def.mCentered ) {
            mVertices = new float[]{
                    -mSpriteDefinition.mSizeX / 2.0f, -mSpriteDefinition.mSizeY / 2.0f, 0,
                    mSpriteDefinition.mSizeX / 2.0f, mSpriteDefinition.mSizeY / 2.0f, 0,
                    -mSpriteDefinition.mSizeX / 2.0f, mSpriteDefinition.mSizeY / 2.0f, 0,
                    mSpriteDefinition.mSizeX / 2.0f, -mSpriteDefinition.mSizeY / 2.0f, 0
            };
        } else {
            mVertices = new float[]{
                    0, -mSpriteDefinition.mSizeY, 0,
                    mSpriteDefinition.mSizeX, 0, 0,
                    0, 0, 0,
                    mSpriteDefinition.mSizeX, -mSpriteDefinition.mSizeY, 0
            };
        }

        // also generate the UV coordinates
        mUVs = new float[]{
                mSpriteDefinition.uvStartX, mSpriteDefinition.uvEndY,
                mSpriteDefinition.uvEndX, mSpriteDefinition.uvStartY,
                mSpriteDefinition.uvStartX, mSpriteDefinition.uvStartY,
                mSpriteDefinition.uvEndX, mSpriteDefinition.uvEndY
        };

        // generate the buffers for OpenGL as necessary.
        ByteBuffer bb = ByteBuffer.allocateDirect(mVertices.length * ShaderHelper.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mVertices);
        mVertexBuffer.position(0);

        ByteBuffer uvbb = ByteBuffer.allocateDirect(mUVs.length * ShaderHelper.BYTES_PER_FLOAT);
        uvbb.order(ByteOrder.nativeOrder());
        mUVBuffer = uvbb.asFloatBuffer();
        mUVBuffer.put(mUVs);
        mUVBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawOrder.length * ShaderHelper.BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        mDrawOrderBuffer = dlb.asShortBuffer();
        mDrawOrderBuffer.put(mDrawOrder);
        mDrawOrderBuffer.position(0);

        mProgramHandle = ShaderProgramLibrary.getInstance().getProgram("texture");
        mMVPHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        mVertexHandle  = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        mUVHandle   = GLES20.glGetAttribLocation(mProgramHandle, "aTextCoord");
        mTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, "sTexture");
    }

    /**
     * Renders the sprite to the x and y location within the camera's model space.
     *
     * @param camera the camera you want to render with
     * @param x the x coordinate position of the sprite
     * @param y the y coordinate position of the sprite
     * @param z the z coordinate position of the sprite
     */
    public void render(Camera camera, float x, float y, float z) {
        // create a simple transformation matrix
        float[] scratch = camera.scratchMatrix;
        Matrix.setIdentityM(scratch, 0);
        Matrix.translateM(scratch, 0, x, y, z);

        // render the sprite with the full model view projection from the camera
        render(camera.calculateMVP(scratch));
    }

    /**
     * Raw render functon that takes a fully formed model view projection matrix.
     *
     * @param mvpMatrix full transformation matrix, float[16]
     */
    public void render(float[] mvpMatrix) {
        // use the program
        GLES20.glUseProgram(mProgramHandle);

        // put the vertices into the program
        GLES20.glEnableVertexAttribArray(mVertexHandle);
        GLES20.glVertexAttribPointer(mVertexHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        // put in the uv mappings for the texture
        GLES20.glEnableVertexAttribArray(mUVHandle);
        GLES20.glVertexAttribPointer(mUVHandle, ELEMENTS_PER_UV_COORD,
                GLES20.GL_FLOAT, false, 0, mUVBuffer);

        // set the model view projection matrix for final rendering
        GLES20.glUniformMatrix4fv(mMVPHandle, 1, false, mvpMatrix, 0);

        // bind the texture to the one we've loaded, in the first slot
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteDefinition.mTextureId);
        GLES20.glUniform1i(mTextureHandle, 0);

        // draw the actual geometry using the draw order and shader
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawOrderBuffer);

        // release the program handle bindings
        GLES20.glDisableVertexAttribArray(mVertexHandle);
        GLES20.glDisableVertexAttribArray(mUVHandle);
        GLES20.glDisableVertexAttribArray(mMVPHandle);
    }
}
