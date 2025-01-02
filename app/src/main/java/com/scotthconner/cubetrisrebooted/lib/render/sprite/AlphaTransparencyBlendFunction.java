package com.scotthconner.cubetrisrebooted.lib.render.sprite;

import android.opengl.GLES20;

/**
 * Created by scottc on 2/22/16.
 */
public class AlphaTransparencyBlendFunction implements IBlendFunction {
    private static IBlendFunction mInstance = null;

    public static IBlendFunction getInstance() {
        if (null == mInstance) {
            mInstance = new AlphaTransparencyBlendFunction();
        }
        return mInstance;
    }

    @Override
    public void enable() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void disable() {
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}
