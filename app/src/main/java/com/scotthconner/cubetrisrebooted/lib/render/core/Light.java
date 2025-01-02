package com.scotthconner.cubetrisrebooted.lib.render.core;

import android.graphics.Color;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;

/**
 * Created by scottc on 1/3/16.
 */
public class Light {
    public Vertex position;
    public int    color;
    public float  attenuation;
    public float  dimmer;

    public Light() {
        position = new Vertex();
        color    = Color.WHITE;
        attenuation = 0.001f;
        dimmer = 1.0f;
    }

    public Light withAttenuation(float a) {
        attenuation = a;
        return this;
    }

    public Light withDimmer(float d) {
        dimmer = d;
        return this;
    }
}
