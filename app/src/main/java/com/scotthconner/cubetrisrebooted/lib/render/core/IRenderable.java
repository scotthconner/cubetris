package com.scotthconner.cubetrisrebooted.lib.render.core;

/**
 *
 *
 * Created by scottc on 12/26/15.
 */
public interface IRenderable {

    /**
     * update
     *
     * Renderables can use update to change anything about their rendering needs, or
     * used to keep track of their transformations between frame renders.
     *
     * @param msDelta the millisecond count since the last update.
     *
     * @return true if the object is still valid, false if its essentially ready for clean up
     */
    boolean update(long msDelta);

    /**
     * render
     *
     * Usually called within onDrawFrame within a GLSurfaceView, will draw itself
     * to the screen.
     */
    void render(Camera camera);

    /**
     * setScene
     *
     * Giving the renderable a reference to the scene is the only way for it
     * to know how to render itself based on the scene around it. This will do
     * nothing if the scene is already set, and is a permanent attribute once set.
     */
    void setScene(Scene scene);

    /**
     * getScene
     *
     * Any object that is handling the renderable may need a reference to the scene.
     *
     * @return
     */
    Scene getScene();

    /**
     * Do any resource or other cleanup that needs to be done.
     */
    void cleanup();
}
