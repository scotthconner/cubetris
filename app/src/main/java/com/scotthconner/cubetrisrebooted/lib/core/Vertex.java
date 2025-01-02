package com.scotthconner.cubetrisrebooted.lib.core;

/**
 * Created by scottc on 12/26/15.
 */
public class Vertex {
    public float x, y, z;

    public Vertex() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }
}
