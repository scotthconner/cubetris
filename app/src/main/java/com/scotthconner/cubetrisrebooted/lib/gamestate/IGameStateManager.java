package com.scotthconner.cubetrisrebooted.lib.gamestate;

import android.opengl.GLSurfaceView;

/**
 * Created by scottc on 12/24/15.
 */
public interface IGameStateManager extends GLSurfaceView.Renderer {
    /**
     * getActiveState()
     *
     * @return the active game state, properly initaited
     */
    public IGameState getActiveState();

    /**
     * terminateActiveState
     *
     * Calls cleanup on the active state, and then get's rid of it. If there are no
     * more states in the stack, it should close the application.
     */
    public void terminateActiveState();
}
