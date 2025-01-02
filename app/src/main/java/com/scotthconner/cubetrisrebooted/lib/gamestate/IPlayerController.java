package com.scotthconner.cubetrisrebooted.lib.gamestate;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by scottc on 1/23/16.
 */
public interface IPlayerController {
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
