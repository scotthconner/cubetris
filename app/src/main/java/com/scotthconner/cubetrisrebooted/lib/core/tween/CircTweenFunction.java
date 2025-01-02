package com.scotthconner.cubetrisrebooted.lib.core.tween;

/**
 * Created by scottc on 4/21/16.
 */
public class CircTweenFunction implements ITweenFunction {
    private static CircTweenFunction mInstance = null;
    private CircTweenFunction() { }
    public static CircTweenFunction getInstance() {
        if (null == mInstance) {
            mInstance = new CircTweenFunction();
        }
        return mInstance;
    }

    public float easeIn (float t,float b , float c, float d) {
        return -c * ((float)Math.sqrt(1 - (t/=d)*t) - 1) + b;
    }

    public float easeOut (float t,float b , float c, float d) {
        return c * (float)Math.sqrt(1 - (t=t/d-1)*t) + b;
    }

    public float easeInOut (float t,float b , float c, float d) {
        if ((t/=d/2) < 1) return -c/2 * ((float)Math.sqrt(1 - t*t) - 1) + b;
        return c/2 * ((float)Math.sqrt(1 - (t-=2)*t) + 1) + b;
    }
}
