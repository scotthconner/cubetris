package com.scotthconner.cubetrisrebooted.lib.core;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by scottc on 12/24/15.
 */
public class GameSurfaceView extends GLSurfaceView {
    public GameSurfaceView(Context context, GLSurfaceView.Renderer renderer) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        setPreserveEGLContextOnPause(true);

        // set the renderer to the activity itself to control lifecycle
        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setFocusable(true);
    }
}
