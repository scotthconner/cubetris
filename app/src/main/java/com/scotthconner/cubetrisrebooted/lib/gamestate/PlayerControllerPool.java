package com.scotthconner.cubetrisrebooted.lib.gamestate;

import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by scottc on 1/23/16.
 */
public class PlayerControllerPool implements IPlayerController {
    // holds the object that spawns new player controllers
    IPlayerControllerGenerator mPlayerControllerGenerator;

    // holds device IDs and the player controllers
    private HashMap<Integer, IPlayerController> mPlayerControllers;

    public PlayerControllerPool(IPlayerControllerGenerator generator) {
        mPlayerControllers = new HashMap();
        mPlayerControllerGenerator = generator;
    }

    public int getControllerCount() {
        return mPlayerControllers.size();
    }

    public Iterator<IPlayerController> iterator() {
        return mPlayerControllers.values().iterator();
    }

    /**
     * processGenericMotionEvent
     *
     * Callback used for game controller analog stick input.
     *
     * @param motionEvent motion event coming in from controller.
     * @return true if it was handled
     */
    public boolean processGenericMotionEvent(MotionEvent motionEvent) {
        IPlayerController controller = getPlayerController(motionEvent.getDeviceId());
        if (controller != null) {
            return controller.processGenericMotionEvent(motionEvent);
        }

        return false;
    }

    /**
     * processKeyDown
     *
     * Callback used for game controller key input
     *
     * @param keyCode the keycode of the event
     * @param event   the event structure
     * @return true if it was handled
     */
    public boolean processKeyDown(int keyCode, KeyEvent event) {
        IPlayerController controller = getPlayerController(event.getDeviceId());
        if (controller != null) {
            return controller.processKeyDown(keyCode, event);
        }

        return false;
    }

    @Override
    public boolean processKeyUp(int keyCode, KeyEvent event) {
        IPlayerController controller = getPlayerController(event.getDeviceId());
        if (controller != null) {
            return controller.processKeyUp(keyCode, event);
        }

        return false;
    }

    /**
     *
     * Will return the player's controller for that device ID. If the device ID
     * hasn't been seen before, it will ask the controller generator to spawn one.
     *
     * @param deviceId the device ID from the input event
     * @return the player  controller for that device ID.
     */
    private IPlayerController getPlayerController(int deviceId) {
        // if it is already here, just return it.
        if (mPlayerControllers.containsKey(deviceId)) {
            return mPlayerControllers.get(deviceId);
        }

        // call out to the generator to get a new one, and then put it in the map
        IPlayerController newPlayer = mPlayerControllerGenerator.spawnPlayerController();
        mPlayerControllers.put(deviceId, newPlayer);
        return newPlayer;
    }
}
