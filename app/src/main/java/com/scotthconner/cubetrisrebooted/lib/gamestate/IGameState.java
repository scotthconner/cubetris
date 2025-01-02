package com.scotthconner.cubetrisrebooted.lib.gamestate;

import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by scottc on 12/24/15.
 */
public interface IGameState extends GLSurfaceView.Renderer {
    /**
     * update
     *
     * Update is called once per frame, with the difference between
     * frames in milliseconds is passed in for frame-independent movement.
     *
     * @param timeDelta milliseconds since the last time update was called.
     * @return true if the game state is still considered valid, otherwise cleanup will be called,
     *         and the state will terminate.
     */
    public boolean update(long timeDelta);

    /**
     * cleanUp
     *
     * Any actions that need to be taken to properly clean up resources used
     * by this gamestate will need to implement this method.
     */
    public void cleanUp();

    /**
     * processGenericMotionEvent
     *
     * Callback used for game controller analog stick input.
     *
     * @param motionEvent motion event coming in from controller.
     * @return true if it was handled
     */
    public boolean processGenericMotionEvent(MotionEvent motionEvent);

    /**
     * processKeyDown
     *
     * Callback used for game controller key input
     *
     * @param keyCode the keycode of the event
     * @param event   the event structure
     * @return true if it was handled
     */
    public boolean processKeyDown(int keyCode, KeyEvent event);

    /**
     * processKeyUp
     *
     * Callback used for game controller key input
     *
     * @param keyCode the keycode of the event
     * @param event   the event structure
     * @return true if it was handled
     */
    public boolean processKeyUp(int keyCode, KeyEvent event);
}
