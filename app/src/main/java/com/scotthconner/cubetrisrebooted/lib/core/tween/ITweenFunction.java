package com.scotthconner.cubetrisrebooted.lib.core.tween;

/**
 * Created by scottc on 4/21/16.
 */
public interface ITweenFunction {
    float easeIn(float t, float b, float c, float d);
    float easeOut(float t, float b, float c, float d);
    float easeInOut(float t, float b, float c, float d);
}
