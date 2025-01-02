package com.scotthconner.cubetrisrebooted.cubetris.gamestates;

import android.graphics.Point;
import android.opengl.GLES20;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scotthconner.cubetrisrebooted.R;
import com.scotthconner.cubetrisrebooted.cubetris.board.CubeBoard;
import com.scotthconner.cubetrisrebooted.cubetris.board.CubeBoardRenderer;
import com.scotthconner.cubetrisrebooted.cubetris.experience.CubetrisExperience;
import com.scotthconner.cubetrisrebooted.cubetris.geometry.CubeLibrary;
import com.scotthconner.cubetrisrebooted.cubetris.geometry.ExperienceSkyBox;
import com.scotthconner.cubetrisrebooted.lib.core.LabeledSoundPool;
import com.scotthconner.cubetrisrebooted.lib.core.TextureManager;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IGameState;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IPlayerController;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IPlayerControllerGenerator;
import com.scotthconner.cubetrisrebooted.lib.gamestate.PlayerControllerPool;
import com.scotthconner.cubetrisrebooted.lib.object.text.FPSCounter;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.AlphaTransparencyBlendFunction;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Font;

import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by scottc on 12/24/15.
 */
public class ArcadeGameState implements IGameState {
    private static final int MAX_PLAYERS = 4;

    private static final int SIDE_WIDTH = 7;
    private static final int BOARD_HEIGHT = 18;

    private boolean mDebug;
    private boolean mPaused;
    private Point mScreenSize;

    // the player controllers and their experiences
    private PlayerControllerPool mPlayerControllerPool;

    // the scene that encompasses all player experiences, and common renderables
    Scene mScene;
    Camera mCamera;
    FPSCounter mFPSCounter;

    // the cube board renderer, shared between experiences
    CubeBoardRenderer mCubeBoardRenderer;

    //////////////////////////////////////////////////////
    // Constructor
    //////////////////////////////////////////////////////
    public ArcadeGameState(boolean debug) {
        mScreenSize = new Point();

        mScene = new Scene();
        mCamera = new Camera();

        // add the frame counter if we are in debug
        mDebug = debug;
        mPaused = false;

        // load all of the sounds needed
        LabeledSoundPool.getInstance().loadSound("rotate", R.raw.turn);
        LabeledSoundPool.getInstance().loadSound("piece", R.raw.piece);
        LabeledSoundPool.getInstance().loadSound("line", R.raw.win);
        LabeledSoundPool.getInstance().loadSound("flame", R.raw.flame);
        LabeledSoundPool.getInstance().loadSound("block", R.raw.slap);
        LabeledSoundPool.getInstance().loadSound("spin", R.raw.spin);
        LabeledSoundPool.getInstance().loadMusic("alpha", R.raw.technical_journey, 0.8f);

        mPlayerControllerPool = new PlayerControllerPool( new IPlayerControllerGenerator() {
            public IPlayerController spawnPlayerController() {
                if (mPlayerControllerPool.getControllerCount() < MAX_PLAYERS) {
                    if (mPlayerControllerPool.getControllerCount() == 0 ) {
                        // start the show TODO: music
                        // LabeledSoundPool.getInstance().startMusic("alpha", false, true);
                    }
                    Log.d("ArcadeGameState", "player controller bound");

                    // configure this player's experience
                    CubetrisExperience.Config expConfig = new CubetrisExperience.Config();
                    expConfig.mSideWidth = SIDE_WIDTH;
                    expConfig.mBoardHeight = BOARD_HEIGHT;
                    expConfig.mPlayerId = mPlayerControllerPool.getControllerCount();
                    expConfig.mScreenSize.x = mScreenSize.x;
                    expConfig.mScreenSize.y = mScreenSize.y;
                    expConfig.mRenderer = mCubeBoardRenderer;

                    // go through existing player experience and let them know
                    // another player has joined.
                    Iterator<IPlayerController> experiences = mPlayerControllerPool.iterator();
                    while (experiences.hasNext()) {
                        ((CubetrisExperience) experiences.next()).incrementTotalPlayers();
                    }

                    CubetrisExperience ce = new CubetrisExperience(expConfig);
                    ce.initialize();
                    return ce;
                }

                return null;
            }
        });
    }

    @Override
    public boolean update(long timeDelta) {
        if (!mPaused) {
            synchronized (mPlayerControllerPool) {
                Iterator<IPlayerController> experiences = mPlayerControllerPool.iterator();
                while (experiences.hasNext()) {
                    ((CubetrisExperience) experiences.next()).update(timeDelta);
                }
            }
        }

        // update the shared scene as well
        mScene.update(timeDelta);

        return true;
    }

    private boolean init = false;

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized(mPlayerControllerPool) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Iterator<IPlayerController> experiences = mPlayerControllerPool.iterator();
            while (experiences.hasNext()) {
                ((CubetrisExperience) experiences.next()).render();
            }
        }

        // render the shared scene
        GLES20.glViewport(0, 0, mScreenSize.x, mScreenSize.y);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mScene.render(mCamera);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void cleanUp() {
        LabeledSoundPool.getInstance().release();
    }

    @Override
    public boolean processGenericMotionEvent(MotionEvent motionEvent) {
        synchronized (mPlayerControllerPool) {
            return mPlayerControllerPool.processGenericMotionEvent(motionEvent);
        }
    }

    @Override
    public boolean processKeyDown(int keyCode, KeyEvent event) {
        // see if this is a pause event
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            LabeledSoundPool.getInstance().playSound("line", 0.65f);

            // process the keydown for menu
            synchronized (mPlayerControllerPool) {
                mPlayerControllerPool.processKeyDown(keyCode, event);
            }

            mPaused = !mPaused;
            return true;
        }

        synchronized (mPlayerControllerPool) {
            return mPlayerControllerPool.processKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean processKeyUp(int keyCode, KeyEvent event) {
        synchronized (mPlayerControllerPool) {
            return mPlayerControllerPool.processKeyUp(keyCode, event);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // pre-load the textures we will need
        TextureManager.getInstance().loadTexture(R.drawable.particles, "particles");

        // load the combination font
        Font f = Font.load(R.raw.blocks, "blocks");
        if (mDebug) {
            mFPSCounter = (FPSCounter) (new FPSCounter(f)
                    .withBlendFunction(AlphaTransparencyBlendFunction.getInstance())
                    .withJustification(Font.TextJustification.JUSTIFY_RIGHT));
            mScene.addRenderable(mFPSCounter);
        }

        // initialize the cube instance buffers, we will need them!
        CubeLibrary.getInstance().init();
        ExperienceSkyBox.getInstance().init();

        // after init of cube library: create the universal cube board renderer
        mCubeBoardRenderer = new CubeBoardRenderer(
                CubeBoard.getBoardWidth(SIDE_WIDTH) * BOARD_HEIGHT  // the size of the board
                        + CubeBoard.MAX_EXTRA_CUBES                 // cubes for line completion
                        + 4);                                       // the active piece
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        mScreenSize.x = i;
        mScreenSize.y = i1;

        // set up the shared scene camera
        mCamera.establishOrthoProjection(mScreenSize.x, mScreenSize.y);
        if (mDebug) {
            mFPSCounter.withPosition(mScreenSize.x / 2, mScreenSize.y / 2, 0);
        }

        synchronized(mPlayerControllerPool) {
            Iterator<IPlayerController> experiences = mPlayerControllerPool.iterator();
            while (experiences.hasNext()) {
                ((CubetrisExperience) experiences.next()).establishScreenSize(i, i1);
            }
        }
    }
}
