package com.scotthconner.cubetrisrebooted.lib.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by scottc on 2/15/16.
 */
public class TextureManager {
    // singleton instance
    private static TextureManager mTextureManager = null;

    // texture manager state
    private Context mApplicationContext;
    private HashMap<String, Integer> mTextureIds = null;

    /**
     *
     * @return static instance to the texture manager
     */
    public static TextureManager getInstance() {
        if (null == mTextureManager) {
            mTextureManager = new TextureManager();
        }
        return mTextureManager;
    }

    /**
     * Necessary before any loading can occur
     * @param cxt the context of the application from the activity
     */
    public void setApplicationContext(Context cxt) {
        mApplicationContext = cxt;
    }

    /**
     * Loads a texture and will provide the open GL id for it.
     *
     * @param resourceId the resource ID of the texture file itself.
     * @param label the 'name' of the texture, can be used to get texture IDs from another context
     * @return the texture ID to be used when binding a texture to a unit in a shader
     */
    public int loadTexture(final int resourceId, String label) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        GLHelper.checkGLError("glGenTextures");

        if (textureHandle[0] != 0) {
            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(), resourceId);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLHelper.checkGLError("glBindTexture");

            // Set filtering
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLHelper.checkGLError("textureParameter MIN filter");
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLHelper.checkGLError("textureParameter MAG filter");

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLHelper.checkGLError("texImage2D");

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLHelper.checkGLError("glBindTexture unbind");

            Log.d("TextureManager", "loaded " + label + " with resource ID: " + resourceId);
        } else {
            throw new RuntimeException("Error loading texture.");
        }

        // store the mapping and return the id
        mTextureIds.put(label, textureHandle[0]);
        return textureHandle[0];
    }

    /**
     * Using the label from the time of loading, find a texture ID.
     *
     * @param label the label passed in when calling loadTexture
     * @return an integer that can be used for gl shaders for textures
     */
    public int getTextureID(String label) {
        return mTextureIds.get(label).intValue();
    }

    /**
     * Will delete all of the textures currently loaded.
     */
    public void clearTextures() {
        Iterator<Integer> i = mTextureIds.values().iterator();
        while(i.hasNext()) {
            int[] id = {i.next().intValue()};
            GLES20.glDeleteTextures(1, id, 0);
        }
        mTextureIds.clear();
    }

    /**
     * Clears a single texture from GL memory.
     * @param label the string used when loading the texture
     */
    public void clearTexture(String label) {
        int[] id = {mTextureIds.get(label).intValue()};
        GLES20.glDeleteTextures(1, id, 0);
        mTextureIds.remove(label);
    }

    /**
     * Test to determine if a texture with a given label has been loaded.
     *
     * @param label the string label used to define the string
     * @return true if the label exists, false otherwise
     */
    public boolean isLoaded(String label) {
        return mTextureIds.containsKey(label);
    }

    /**
     * Only to be used by TextureManager::getInstance()
     */
    private TextureManager() {
        mTextureIds = new HashMap<String, Integer>();
    }
}
