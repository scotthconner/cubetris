package com.scotthconner.cubetrisrebooted.cubetris.activity;

import android.opengl.GLES20;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scotthconner.cubetrisrebooted.cubetris.gamestates.ArcadeGameState;
import com.scotthconner.cubetrisrebooted.lib.core.GLHelper;
import com.scotthconner.cubetrisrebooted.lib.core.GameSurfaceView;
import com.scotthconner.cubetrisrebooted.lib.core.GameThread;
import com.scotthconner.cubetrisrebooted.lib.core.LabeledSoundPool;
import com.scotthconner.cubetrisrebooted.lib.core.TextureManager;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IGameState;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IGameStateManager;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Font;

import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends ActionBarActivity implements IGameStateManager {
    private static final String TAG = MainActivity.class.getSimpleName();

    // OpenGL Surface
    GameSurfaceView glGameSurface;

    // structure that maintains the game states
    private Stack<IGameState> gameStates;

    // Controls the game loop logic and rendering
    private GameThread thread;

    ///////////////////////////////////////////////////////////////
    // Activity Callbacks
    ///////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup the games application context
        TextureManager.getInstance().setApplicationContext(getApplicationContext());
        Font.setContext(getApplicationContext());

        // set up the GL Surface
        glGameSurface = new GameSurfaceView(this, this);
        setContentView(glGameSurface);

        // initialize the sound interfaces
        LabeledSoundPool.getInstance().setApplicationContext(getApplicationContext());

        // create the splash game state and push it onto the state stack
        gameStates = new Stack<IGameState>();
        gameStates.push(new ArcadeGameState(true));

        // start the game loop
        thread = new GameThread(glGameSurface, this);
        thread.setRunning(true);
        thread.start();
        Log.d(TAG, "MainActivity::onCreate(): GameSurfaceView added.");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MainActivity::onDestroy(): Destroying activity...");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Clearing GameState Stack");
        while (!gameStates.isEmpty()) {
            terminateActiveState();
        }

        Log.d(TAG, "MainActivity::onStop(): Stopping activity...");

        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try again.
                Log.d(TAG, "Surface destroyed, waiting for game loop thread to join.");
            }
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        thread.setRunning(true);
        glGameSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        thread.setRunning(false);
        glGameSurface.onPause();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gameStates.peek().processGenericMotionEvent(event) ||
                super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        return gameStates.peek().processKeyDown(keyCode, event) ||
                super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return gameStates.peek().processKeyUp(keyCode, event) ||
                super.onKeyUp(keyCode, event);
    }
    ///////////////////////////////////////////////////////////////
    // End Activity Callbacks
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // IGameStateManager Implementation
    ///////////////////////////////////////////////////////////////
    public IGameState getActiveState() {
        return gameStates.peek();
    }

    public void terminateActiveState() {
        gameStates.pop().cleanUp();
        if (gameStates.empty()) {
            this.finish();
        }
    }
    ///////////////////////////////////////////////////////////////
    // End IGameStateManager Implementation
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // GLSurfaceView.Renderer Implementation
    ///////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d("MainActivity", "GLSurfaceView.Renderer::onSurfaceCreated");

        // enable alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // respect the Z axis so stuff works
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // load library shaders
        GLHelper.loadDefaultShaders(getApplicationContext());

        gameStates.peek().onSurfaceCreated(gl10, eglConfig);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        Log.d("MainActivity", "GLSurfaceView.Renderer::onSurfaceChanged");
        GLES20.glViewport(0, 0, i, i1);
        gameStates.peek().onSurfaceChanged(gl10, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gameStates.peek().onDrawFrame(gl10);
    }
    ///////////////////////////////////////////////////////////////
    // End GLSurfaceView.Renderer Implementation
    ///////////////////////////////////////////////////////////////
}
