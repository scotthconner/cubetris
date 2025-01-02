package com.scotthconner.cubetrisrebooted.lib.core.tween;

/**
 * Created by scottc on 4/21/16.
 */
public class CubicTweenFunction implements ITweenFunction {
    private static CubicTweenFunction mInstance = null;
    private CubicTweenFunction() { }
    public static CubicTweenFunction getInstance() {
        if (null == mInstance) {
            mInstance = new CubicTweenFunction();
        }
        return mInstance;
    }

    public float easeIn (float t,float b , float c, float d) {
        return c*(t/=d)*t*t + b;
    }

    public float easeOut (float t,float b , float c, float d) {
        return c*((t=t/d-1)*t*t + 1) + b;
    }

    public float easeInOut (float t,float b , float c, float d) {
        if ((t/=d/2) < 1) return c/2*t*t*t + b;
        return c/2*((t-=2)*t*t + 2) + b;
    }

}
