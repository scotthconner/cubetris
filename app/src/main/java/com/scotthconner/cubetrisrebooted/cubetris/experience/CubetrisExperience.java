package com.scotthconner.cubetrisrebooted.cubetris.experience;

import android.graphics.Point;
import android.opengl.GLES20;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scotthconner.cubetrisrebooted.cubetris.board.CubeBoard;
import com.scotthconner.cubetrisrebooted.cubetris.board.CubeBoardRenderer;
import com.scotthconner.cubetrisrebooted.cubetris.board.ICubeBoardEventListener;
import com.scotthconner.cubetrisrebooted.cubetris.controllers.PlayerBoardController;
import com.scotthconner.cubetrisrebooted.cubetris.controllers.SceneLightController;
import com.scotthconner.cubetrisrebooted.cubetris.particle.drop.PieceDropEffect;
import com.scotthconner.cubetrisrebooted.cubetris.particle.stars.MoveStarsEffect;
import com.scotthconner.cubetrisrebooted.lib.core.LabeledSoundPool;
import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IPlayerController;
import com.scotthconner.cubetrisrebooted.lib.object.text.FontText;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.Light;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.AlphaTransparencyBlendFunction;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Font;

import java.util.Random;

/**
 * Encapsulates the logic for a single player's Cubetris experience. Controls game parameters,
 * rendering, viewport, and reference logic.
 *
 * Created by scottc on 2/4/16.
 */
public class CubetrisExperience implements IPlayerController, ICubeBoardEventListener {
    // Master Configuration ///////////////////////////////////
    public static class Config {
        // board size definition
        public int mSideWidth;
        public int mBoardHeight;

        // board renderer
        public CubeBoardRenderer mRenderer;

        // used to identify player and calculate viewport
        public int mPlayerId;
        public Point mScreenSize;

        public Config() {
            mScreenSize = new Point();
        }
    }
    ////////////////////////////////////////////////////////////

    // Scoring Structures //////////////////////////////////////
    private class MoveScoreCard {
        public boolean wasDrop;
        public boolean wasBrave;
        public boolean wasSlide;
        public boolean wasOnFire;
        public boolean wasCollision;
        public int lineCompleteCount;

        public MoveScoreCard() {
            reset();
        }

        public void reset() {
            wasDrop = false;
            wasOnFire = false;
            wasBrave = false;
            wasSlide = false;
            lineCompleteCount = 0;
            wasCollision = false;
        }

        public int calculateScore() {
            float multiplier = 1;
            int bonus = 0;

            int score = 100;
            if (wasCollision) {
                score = 0;
            }

            // dropping increases the multiplier
            if (wasDrop) {
                multiplier += .1;
            }
            if (wasBrave) {
                bonus += 50;
            }
            if (wasSlide) {
                multiplier += .5;
            }
            multiplier += lineCompleteCount;

            return (int) ((score + bonus) * multiplier);
        }

        public boolean wasEfficient() {
            return true;
        }
    }
    ////////////////////////////////////////////////////////////

    // configuration for this instance
    private Config  mConfig;
    private boolean mInitializedGL;
    private int     mPlayerCount;

    // 3D rendering scene and objects
    private Scene     mScene;          // this is the scene with the game board
    private Camera    mCamera;         // 3D camera used for game objects
    private Vertex mCameraPosition; // used for shaking 3D camera

    // 2d rendering scene and objects
    private Scene     mHUDScene;       // this holds all of the 2D sprites for the HUD
    private Camera    mHUDCamera;      // 2D camera used for sprites
    private FontText  mScoreText;      // displays the player's score

    // experience game state
    private CubeBoard mCubeBoard;      // the game board that holds the blocks
    private int       mScore;          // the experience's current score
    private int       mPieceCount;     // the experience's piece count
    private MoveScoreCard mMoveScoreCard;  // structure for describing each player move

    // camera effects
    private float mShakeTimeMs;
    private float mShakeElapsedMs;
    private float mShakeIntensity;
    private Random mRandom;

    // special effects
    private Light[] mActivePieceLights;             // the reference to the light we attach to the active piece.
    private Light   mActivePieceLight;              // reference to the next active piece
    private Light   mPreviousPieceLight;            // the reference to the previous piece
    private PieceDropEffect[] mDropEffects;         // we want to hold two of these so they don't overlap
    private PieceDropEffect mActiveDropEffect;      // which one is currently active? used for dust
    private int mDropEffectIndex;                   // which is the next one we are going grab for a drop?
    private MoveStarsEffect mMoveStarsEffect;       // contains all of the logic for firing move star animations
    private Vertex mActivePiecePosition;   // used as a buffer for many special effects on the active piece
    private static float DEFAULT_PIECE_ATTENUATION = 0.01f;
    private static float FLAME_PIECE_ATTENUATION = 0.0f;

    private float mDropTimer;

    // the object that controlls the board and the experience
    private IPlayerController mActiveController;
    private PlayerBoardController mPlayerBoardController;
    private SceneLightController mSceneLightController;

    /**
     * @param config a configuration object necessary to determine the environment of the player's
     *               experience.
     *
     */
    public CubetrisExperience(CubetrisExperience.Config config) {
        // here we go folks
        mConfig = config;
        mInitializedGL = false;
        mPlayerCount = mConfig.mPlayerId + 1; // off by one error, yay!
        mRandom = new Random();

        // initialize the camera shaker, and other timers
        mShakeTimeMs    = 0;
        mShakeElapsedMs = 0;
        mShakeIntensity = 0;
        mDropTimer      = 0;
        mPieceCount     = 0;

        // create tne player's cube board
        mCubeBoard = new CubeBoard(mConfig.mRenderer, mConfig.mSideWidth, mConfig.mBoardHeight);

        // set up the scoring structure
        mMoveScoreCard = new MoveScoreCard();

        // set up the scene and the camera
        mScene = new Scene();
        mHUDScene = new Scene();
        mCamera = new Camera();
        mHUDCamera = new Camera();
        mCameraPosition = new Vertex(0, 16.0f, 26.0f);
        mCamera.setPosition(mCameraPosition.x, mCameraPosition.y, mCameraPosition.z);
        mCamera.lookAt(0.0f, 7.0f, 0.0f);
        mScene.setSunPosition(0.0f, 20.5f, 25.0f);
        mScene.setAmbientFactor(0.22f);
        mScene.getSceneSun().attenuation = 0.0003999999f;

        // set up the active piece's light
        mActivePieceLights = new Light[] {
                new Light().withAttenuation(DEFAULT_PIECE_ATTENUATION),
                new Light().withAttenuation(DEFAULT_PIECE_ATTENUATION).withDimmer(0.0f)
        };
        mScene.addLight("active1", mActivePieceLights[0]);
        mScene.addLight("active2", mActivePieceLights[1]);
        mActivePieceLight = mActivePieceLights[0];
        mPreviousPieceLight = null;

        // create a controller instance and bind it to the player's board
        mPlayerBoardController = new PlayerBoardController(mCubeBoard);
        mSceneLightController = new SceneLightController(mScene);
        mActiveController = mPlayerBoardController;

        // prime the special effects
        mDropEffects = null;
        mMoveStarsEffect = null;
        mDropEffectIndex = 0;
        mActivePiecePosition = new Vertex();

        // set up the HUD
        mScoreText = (FontText)(new FontText(Font.getFont("blocks")))
            .withBlendFunction(AlphaTransparencyBlendFunction.getInstance())
            .withJustification(Font.TextJustification.JUSTIFY_CENTER)
            .withText("" + mScore)
            .withPosition(0, -mConfig.mScreenSize.y / 2.0f + 120, 0);
        mHUDScene.addRenderable(mScoreText);
    }

    public void initialize() {
        mScene.addRenderable(mCubeBoard);
        mCubeBoard.addBoardListener(this);
        mCubeBoard.testFill();
        mCubeBoard.start();
    }

    public void initializeGLThread() {
        // generate the drop effect array
        mDropEffects = new PieceDropEffect[] {
            new PieceDropEffect(),
            new PieceDropEffect()
        };

        // create the move stars object. we know we need to render these
        // last so it is a convienence that we are adding this to the scene
        // later, based on the scene's implementation of vector. techincally,
        // this could break later.
        mMoveStarsEffect = new MoveStarsEffect();
        mScene.addRenderable(mMoveStarsEffect);
    }

    public void incrementTotalPlayers() {
        mPlayerCount++;
    }

    /**
     * Will update everything related to this player's experience.
     *
     * @param msDelta change since last update
     */
    public void update(long msDelta) {
        // shake the camera if thats what we need to do
        if (mShakeTimeMs != 0) {
            mShakeElapsedMs += msDelta;
            if (mShakeElapsedMs >= mShakeTimeMs) {
                mShakeTimeMs = 0;
                mShakeElapsedMs = 0;
                mShakeIntensity = 0;
            }
        }

        // dim the previous active piece light, if there is one
        if (mPreviousPieceLight != null) {
            mPreviousPieceLight.dimmer -= (msDelta / 1000.0f);
            if (mPreviousPieceLight.dimmer < 0) {
                mPreviousPieceLight.dimmer = 0;
            }
        }

        // make the active piece light follow the active piece on the y axis
        mCubeBoard.calculateActivePieceAverageModelPosition(mActivePieceLight.position);
        mActivePieceLight.position.z += 2.0f; // oooh magic!
        mActivePieceLight.dimmer = 1.0f;

        // accumulate the drop time, if we are dropping. used for animations and scoring.
        if (mMoveScoreCard.wasDrop) {
            mDropTimer += msDelta;

            // if we've been dropping for a while, show some fire! instant for now.
            if (!mMoveScoreCard.wasOnFire) {
                mMoveScoreCard.wasOnFire = true;
                Log.d("CubetrisExperience", "update:: foom!");
                mActivePieceLight.attenuation = FLAME_PIECE_ATTENUATION;

                // grab the next drop effect
                mActiveDropEffect = mDropEffects[(mDropEffectIndex++) % mDropEffects.length];
                mActiveDropEffect.reset();
                mActiveDropEffect.startFlames(mCubeBoard.getDropCollisionCubeInstances());
            }
        }

        // update the scene, and all of its children
        mScene.update(msDelta);
    }

    /**
     * Will set the viewport and render the player's experience.
     */
    public void render() {
        // we need to ensure that we have the viewport for this player's experience
        setViewport();

        // initialize things that require the GL thread, since cubetris experiences
        // are spawned currently in the input thread.
        if (!mInitializedGL) {
            initializeGLThread();
            mInitializedGL = true;
        }

        if (mShakeTimeMs != 0) {
            mCamera.setPosition(mCameraPosition.x + mRandom.nextFloat() * mShakeIntensity,
                    mCameraPosition.y + mRandom.nextFloat() * mShakeIntensity,
                    mCameraPosition.z + mRandom.nextFloat() * mShakeIntensity);
        } else {
            mCamera.setPosition(mCameraPosition.x, mCameraPosition.y, mCameraPosition.z);
        }

        // render the 3d game experience
        mScene.render(mCamera);

        //render the 2d game experience
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mHUDScene.render(mHUDCamera);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    /**
     * Will establish the viewport for the experience.
     *
     * @param totalX the screen width  in pixels
     * @param totalY the screen height in pixels
     */
    public void establishScreenSize(int totalX, int totalY) {
        mConfig.mScreenSize.x = totalX;
        mConfig.mScreenSize.y = totalY;
    }

    /**
     * Set the established viewport during rendering. this ensures
     * a player's experience is rendered to the correct part of the screen.
     */
    private void setViewport() {
        int playerExperienceWidth = mConfig.mScreenSize.x / mPlayerCount;

        // the player's view port is divided vertically in a horizontal fashion
        // up to 4 players
        GLES20.glViewport(playerExperienceWidth * mConfig.mPlayerId, 0,
                playerExperienceWidth, mConfig.mScreenSize.y);

        mCamera.establishProjection(playerExperienceWidth, mConfig.mScreenSize.y);
        mHUDCamera.establishOrthoProjection(playerExperienceWidth, mConfig.mScreenSize.y);
    }

    private void shakeScreen(float intensity) {
        mShakeTimeMs = 650; // magic!
        mShakeElapsedMs = 0;
        mShakeIntensity = intensity;
    }

    // IPLAYER CONTROLLER INTERFACE ////////////////////////////////////////
    @Override
    public boolean processGenericMotionEvent(MotionEvent motionEvent) {
        return mActiveController.processGenericMotionEvent(motionEvent);
    }

    @Override
    public boolean processKeyDown(int keyCode, KeyEvent event) {
        // this will toggle between controller modes between play and pause
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            if (mActiveController == mPlayerBoardController) {
                mActiveController = mSceneLightController;
            } else {
                mActiveController = mPlayerBoardController;
            }
        }

        return mActiveController.processKeyDown(keyCode, event);
    }

    @Override
    public boolean processKeyUp(int keyCode, KeyEvent event) {
        return mActiveController.processKeyUp(keyCode, event);
    }
    // END IPLAYER CONTROLLER INTERFACE ////////////////////////////////////

    // ICUBEBOARDLISTENER //////////////////////////////////////////////////
    @Override
    public void onBoardRotate(CubeBoard cubeBoard) {
        LabeledSoundPool.getInstance().playSound("rotate", 0.40f);
    }

    @Override
    public void onPieceCommit(CubeBoard cubeBoard) {
        LabeledSoundPool.getInstance().playSound("piece", 0.45f);

        // determine if we were dropping and show the smoke
        if (mMoveScoreCard.wasDrop) {
            // pause drop flame special effects
            mActiveDropEffect.pauseFlames();

            // start the smoke on the collision cubes
            mActiveDropEffect.startDust();
            mActiveDropEffect = null;
        }

        // calculate and add the score
        mScore += mMoveScoreCard.calculateScore();

        // fire off the star animation, by calculating where the stars should be
        cubeBoard.getActivePiece().calculateAverageModelFacePosition(mActivePiecePosition);

        // if it was a bad move, you get zero stars.
        if ( cubeBoard.wasBadMove() ) {

        } else if ( mMoveScoreCard.wasEfficient()) {
            // if the move was efficient they get two stars
            mMoveStarsEffect.fireStars(2, mActivePiecePosition.x, mActivePiecePosition.y, mActivePiecePosition.z + 1.0f);
        } else {
            // otherwise its just one.
            mMoveStarsEffect.fireStars(1, mActivePiecePosition.x, mActivePiecePosition.y, mActivePiecePosition.z + 1.0f);
        }

        // update the score and reset the card
        mScoreText.setText("" + mScore);
        mMoveScoreCard.reset();

        // reset the drop timer
        mDropTimer = 0;

        // increase the piece count
        mPieceCount++;

        // toggle the active light we are manipulating
        mPreviousPieceLight = mActivePieceLight;
        mPreviousPieceLight.attenuation = DEFAULT_PIECE_ATTENUATION;
        mActivePieceLight = mActivePieceLights[mPieceCount % mActivePieceLights.length];
    }

    @Override
    public void onLineComplete(CubeBoard cubeBoard) {
        LabeledSoundPool.getInstance().playSound("line", 0.75f);
        mMoveScoreCard.lineCompleteCount = mCubeBoard.getLastMoveLineCompletionCount();
    }

    @Override
    public void onPieceDrop(CubeBoard cubeBoard) {
        LabeledSoundPool.getInstance().playSound("flame", 0.6f);
        mMoveScoreCard.wasDrop = true;
    }

    @Override
    public void onPieceSlide(CubeBoard cubeBoard) {
        mMoveScoreCard.wasSlide = true;
    }

    @Override
    public void onPieceHurry(CubeBoard cubeBoard) {

    }

    @Override
    public void onPieceMove(CubeBoard cubeBoard) {
        if(mMoveScoreCard.wasDrop) {
            mMoveScoreCard.wasBrave = true;
        }
    }

    @Override
    public void onPieceRotate(CubeBoard cubeBoard) {
        LabeledSoundPool.getInstance().playSound("spin", 0.30f);
    }

    @Override
    public void onBoardRotateBlock(CubeBoard cubeBoard) {
        mMoveScoreCard.wasCollision = true;
        LabeledSoundPool.getInstance().playSound("block", 0.40f);
        shakeScreen(3.0f);
    }
    // END ICUBEBOARDLISTENER //////////////////////////////////////////////
}
