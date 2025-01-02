package com.scotthconner.cubetrisrebooted.lib.object.particle;

import com.scotthconner.cubetrisrebooted.lib.render.sprite.IBlendFunction;

/**
 * Used as an immutable object to describe the texture bounds of a point sprite
 *
 * Created by scottc on 3/14/16.
 */
public class TexturedPointSpriteDefinition {
    private int     mGLTextureId;
    private float   mTextureWidth;
    private float   mPointSpriteWidth;
    private IBlendFunction mBlendFunction;

    public TexturedPointSpriteDefinition() {}

    // Access /////////////////////////////////////////////////////////////
    public int getGLTextureId()       { return mGLTextureId;  }

    public float getTextureWidth()      { return mTextureWidth; }

    public float getPointSpriteWidth() { return mPointSpriteWidth; }

    public IBlendFunction getBlendFunction() { return mBlendFunction; }
    ///////////////////////////////////////////////////////////////////////

    // Builder ////////////////////////////////////////////////////////////
    public TexturedPointSpriteDefinition withGLTextureId(int textureId) {
        mGLTextureId = textureId;
        return this;
    }

    public TexturedPointSpriteDefinition withTextureWidth(float textureWidth) {
        mTextureWidth = textureWidth;
        return this;
    }

    public TexturedPointSpriteDefinition withPointSpriteWidth(float pointSpriteWidth) {
        mPointSpriteWidth = pointSpriteWidth;
        return this;
    }

    public TexturedPointSpriteDefinition withBlendFunction(IBlendFunction blendFunction) {
        mBlendFunction = blendFunction;
        return this;
    }
    ///////////////////////////////////////////////////////////////////////
}
