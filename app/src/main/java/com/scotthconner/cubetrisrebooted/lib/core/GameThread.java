package com.scotthconner.cubetrisrebooted.lib.core;

import com.scotthconner.cubetrisrebooted.lib.gamestate.IGameStateManager;

/**
 * Created by scottc on 12/24/15.
 */
public class GameThread extends Thread {
    private static final String TAG = GameThread.class.getSimpleName();

    private boolean running;
    private GameSurfaceView gameSurfaceView;
    private IGameStateManager gameStateManager;

    public GameThread(GameSurfaceView gameSurfaceView, IGameStateManager gameStateManager) {
        super();
        this.gameSurfaceView = gameSurfaceView;
        this.gameStateManager = gameStateManager;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        long lastFrameTime = System.currentTimeMillis();
        long elapsedFrameTime = 0;

        while (running) {
            // run the update function in the active game state, and notify
            // the manager if it is done.
            if(!gameStateManager.getActiveState().update(elapsedFrameTime)) {
                gameStateManager.terminateActiveState();
            }

            gameSurfaceView.requestRender();

            // calculate how long the frame took and store it for
            // the next frame's time differential
            elapsedFrameTime = System.currentTimeMillis() - lastFrameTime;
            lastFrameTime = System.currentTimeMillis();
        }
    }
}
