package com.scotthconner.cubetrisrebooted.lib.core.tween;

/**
 * Created by scottc on 4/21/16.
 */
public class QuadTweenFunction implements ITweenFunction {
    private static QuadTweenFunction mInstance = null;
    private QuadTweenFunction() { }
    public static QuadTweenFunction getInstance() {
        if (null == mInstance) {
            mInstance = new QuadTweenFunction();
        }
        return mInstance;
    }

    public float easeIn (float t,float b , float c, float d) {
        return c*(t/=d)*t + b;
    }

    public float easeOut (float t,float b , float c, float d) {
        return -c *(t/=d)*(t-2) + b;
    }

    public float easeInOut (float t,float b , float c, float d) {
        if ((t/=d/2) < 1) return c/2*t*t + b;
        return -c/2 * ((--t)*(t-2) - 1) + b;
    }
}
