package com.scotthconner.cubetrisrebooted.lib.core.tween;

/**
 * Created by scottc on 4/21/16.
 */
public class LinearTweenFunction implements ITweenFunction {
    private static LinearTweenFunction mInstance = null;
    private LinearTweenFunction() { }
    public static LinearTweenFunction getInstance() {
        if (null == mInstance) {
            mInstance = new LinearTweenFunction();
        }
        return mInstance;
    }

    public float easeIn (float t,float b , float c, float d) {
        return c*t/d + b;
    }

    public float easeOut (float t,float b , float c, float d) {
        return c*t/d + b;
    }

    public float easeInOut (float t,float b , float c, float d) {
        return c*t/d + b;
    }
}
