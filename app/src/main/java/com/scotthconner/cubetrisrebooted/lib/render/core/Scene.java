package com.scotthconner.cubetrisrebooted.lib.render.core;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by scottc on 12/26/15.
 *
 * Scene
 *
 * A scene has a camera, and can hold a list of renderables that it shows. It is self
 * contained.
 */
public class Scene {
    // used to hold active renderables.
    private final Vector<IRenderable> renderables;

    // one supported internal light
    private HashMap<String, Light> mLights;
    private float mAmbientFactor;

    public Scene() {
        renderables = new Vector<>();
        mLights = new HashMap<>();
        mLights.put("sun", new Light());
        mAmbientFactor = 0;
    }

    public void setAmbientFactor(float f) {
        mAmbientFactor = f;
    }

    public float getAmbientFactor() { return mAmbientFactor; }

    public void setSunPosition(float x, float y, float z) {
        Light sun = mLights.get("sun");
        sun.position.x = x;
        sun.position.y = y;
        sun.position.z = z;
    }

    public Light getSceneSun() {
        return mLights.get("sun");
    }

    public Light getLight(String lightName) {
        return mLights.get(lightName);
    }

    public void addLight(String lightName, Light l) {
        mLights.put(lightName, l);
    }

    public void addRenderable(IRenderable r) {
        r.setScene(this);
        synchronized (renderables) {
            renderables.add(r);
        }
    }

    public void removeRenderable(IRenderable r) {
        synchronized (renderables) {
            renderables.remove(r);
        }
    }

    public void update(long msDelta) {
        synchronized (renderables) {
            // update all of the objects
            Iterator<IRenderable> i = renderables.iterator();
            while (i.hasNext()) {
                IRenderable r = i.next();
                if(!r.update(msDelta)) {
                    i.remove();
                    r.cleanup();
                }
            }
        }
    }

    public void render(Camera camera) {
        synchronized (renderables) {
            Iterator iter = renderables.iterator();
            for(IRenderable renderable : renderables ){
                renderable.render(camera);
            }
        }
    }
}
