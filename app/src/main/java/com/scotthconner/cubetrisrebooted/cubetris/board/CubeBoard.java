package com.scotthconner.cubetrisrebooted.cubetris.board;

import android.graphics.Point;
import android.opengl.Matrix;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.cubetris.geometry.CubeLibrary;
import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.object.Line;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.SceneObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

/**
 * Created by scottc on 1/21/16.
 *
 * CubeBoard
 *
 * Represents the state of a player's board. Controls board rules.
 */
public class CubeBoard extends SceneObject {
    // Constants
    private static int mSideCount            = 4;    // the number of "sides" the board has
    private static float mBoardRotationSpeed = 0.230f; // how fast the board rotates the required degrees
    private static float[] mFaceRotations = { 0.0f, 90.0f, 180.0f, 270.0f};
    public static int MAX_EXTRA_CUBES = 100;
    private static Vertex[] mFaceNormals = {
            new Vertex(0,0, 1),
            new Vertex(1,0,0),
            new Vertex(0,0,-1),
            new Vertex(-1,0,0)
    };

    // listener stuff
    private interface IListenerStrategy {
        void fire(ICubeBoardEventListener listener, CubeBoard cb);
    }
    private static IListenerStrategy mBoardRotateStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onBoardRotate(cb);
        }
    };
    private static IListenerStrategy mPieceCommitStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceCommit(cb);
        }
    };
    private static IListenerStrategy mLineCompleteStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onLineComplete(cb);
        }
    };
    private static IListenerStrategy mPieceDropStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceDrop(cb);
        }
    };
    private static IListenerStrategy mRotateBlockStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onBoardRotateBlock(cb);
        }
    };
    private static IListenerStrategy mPieceHurryStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceHurry(cb);
        }
    };
    private static IListenerStrategy mPieceSlideStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceSlide(cb);
        }
    };
    private static IListenerStrategy mPieceMoveStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceMove(cb);
        }
    };
    private static IListenerStrategy mPieceRotateStrategy = new IListenerStrategy() {
        public void fire(ICubeBoardEventListener listener, CubeBoard cb) {
            listener.onPieceRotate(cb);
        }
    };

    // scene reference
    CubeBoardRenderer mRenderer;
    Vector<ICubeBoardEventListener> mListeners = null;

    // board dimensions
    private int mSideWidth;                   // the number of squares wide of each side
    private int mBoardHeight;                 // the height of the board itself.
    private int mBoardWidth;                  // each side shares slots with it's two adjacent sides

    // board state
    private CubeInstance[][]     mBoard; // the matrix of cubes itself
    private Vector<Point> mCommitLocations;    // the array that holds the board positions the last piece committed to
    private boolean mWasBadMove;               // used as a way to determine if the player boned themselves
    private HashMap<String, String> mPathLocations; // used as a way to trace through the board for bad moves
    private Vector<CubeInstance> mExtraCubes;  // need references for the renderer to do in one call

    private int mActiveFace;                   // which face is facing the player?
    private float mBoardModelRotation;         // holds the board y axis rotation for the cubes
    private int mLastMoveLineCompletionCount;  // rather self explainatory

    // active piece, controlled via PlayerBoardController
    private CubeBoardPiece mActivePiece;       // the active player's piece

    // used for animations
    private int mTargetFace;
    private float mElapsedRotationTime;

    // effects for the board scene
    //private Line[] mBoardLines;
    private float[] mModelMatrix;
    private boolean mInitGL;

    /**
     *
     * Creates an instance of a CubeBoard.
     *
     * @param sideWidth   the width of one side of the board. both the far left and far right space
     *                    on the board will be "shared" with it's adjacent side.
     * @param boardHeight the height of the board, and each side.
     */
    public CubeBoard(CubeBoardRenderer renderer, int sideWidth, int boardHeight) {
        mModelMatrix = new float[16];
        mRenderer = renderer;
        mListeners = new Vector<>();
        mInitGL = false;
        mWasBadMove = false;

        mActivePiece = null;
        mSideWidth   = sideWidth;
        mBoardHeight = boardHeight;
        // the sideWidth is multiplied by the number of sides, and we subtract
        // one "shared" board space for each side
        mBoardWidth  = getBoardWidth(mSideWidth);

        // generate the memory for the size of the board.
        mBoard = new CubeInstance[mBoardWidth][mBoardHeight];
        mExtraCubes = new Vector<>();
        mCommitLocations = new Vector<>();
        mPathLocations = new HashMap<>();
        mActiveFace = 0;
        mBoardModelRotation = 0;
        mElapsedRotationTime = 0;
        mLastMoveLineCompletionCount = 0;
    }

    public CubeInstance[][] getCubeInstances() {
        return mBoard;
    }
    public Iterator<CubeInstance> getExtraCubes() {
        return mExtraCubes.iterator();
    }

    public CubeBoardPiece getActivePiece() {
        return mActivePiece;
    }

    public float getBoardRotation() {
        return mBoardModelRotation;
    }

    public boolean isRotating() {
        return mActiveFace != mTargetFace;
    }

    public void addBoardListener(ICubeBoardEventListener listener) {
        mListeners.add(listener);
    }

    private void initGL() {
        float mult = (mSideWidth) / 2.0f;
        addChild(new Line(new Vertex(-mult,-0.5f, mult), new Vertex( mult,-0.5f, mult), android.graphics.Color.WHITE));
        addChild(new Line(new Vertex( mult,-0.5f, mult), new Vertex( mult,-0.5f,-mult), android.graphics.Color.WHITE));
        addChild(new Line(new Vertex( mult,-0.5f,-mult), new Vertex(-mult,-0.5f,-mult), android.graphics.Color.WHITE));
        addChild(new Line(new Vertex(-mult,-0.5f,-mult), new Vertex(-mult,-0.5f, mult), android.graphics.Color.WHITE));
        mInitGL = true;
    }

    /**
     * This value is calculated in onPieceCommit, and can only be determined as "valid" in the case
     * where we are calling this method within the onPieceCommit listening strategy.
     * @return
     */
    public boolean wasBadMove() {
        return mWasBadMove;
    }

    /**
     * Attempts to rotate the board left or right.
     *
     * @param direction -1 for left, 1 for right.
     */
    public void rotate(int direction) {
        // if we are already spinning, do nothing. or if the active piece is dropping
        if (isRotating() ||
                (mActivePiece != null && mActivePiece.isDropping())) {
            return;
        }

        // ensure that each board piece block won't hit anything
        // on its way to the new location.
        if (null != mActivePiece) {
            Point[] pieceBlocks = mActivePiece.getPieceFacePositions();
            for(Point p : pieceBlocks) {
                // test each block against a side's length of blocks in the direction
                for(int diff = 0; diff < (mSideWidth); diff++) {
                    // the trueX location of the piece, plus the scanning differential
                    int trux = (mActiveFace * mSideWidth - mActiveFace + p.x) + (direction * diff);
                    if (trux < 0) { trux = mBoardWidth + trux; }
                    trux = trux % mBoardWidth;

                    if (p.y < mBoardHeight && null != mBoard[trux][p.y] ||
                            p.y == 0 ||
                            (p.y < mBoardHeight && null != mBoard[trux][p.y - 1])) {
                        fireBoardEvent(mRotateBlockStrategy);
                        return; // party is over folks, no rotation will happen.
                    }
                }
            }
        }

        // set the target face
        if (mActiveFace == 0 && direction == -1) {
            mTargetFace = mSideCount - 1;
        } else {
            mTargetFace = (mActiveFace + direction) % mSideCount;
        }

        // fire the event since we did do it
        fireBoardEvent(mBoardRotateStrategy);
    }

    /**
     *
     * @param direction -1 for counter clockwise, 1 for clockwise
     */
    public void rotatePiece(int direction) {
        // only allow a piece to rotate if we are not currently rotating
        if(null != mActivePiece && (mActiveFace == mTargetFace)) {
            if (mActivePiece.rotate(direction)) {
                fireBoardEvent(mPieceRotateStrategy);
            }
        }
    }

    /**
     *
     * @param boardX the logical board x coordinate
     * @param boardY the logical board y coordinate
     * @return a vertex in 3D space where that block would be
     */
    public Vertex calculateCubePosition(int boardX, int boardY) {
        int cubeX;
        int cubeZ;
        int face  = boardX / (mSideWidth-1);
        int sideMod = boardX % (mSideWidth-1);

        // determine the position offset from center of mass of board
        // for the new cube based on what face it is on
        if (face == 0 || face == 2 ) {
            int multiplyDirection = -(face - 1); // either 1, or -1
            cubeX = (multiplyDirection) * -(mSideWidth/2) + ((multiplyDirection) * sideMod);
            cubeZ = multiplyDirection * (mSideWidth/2);
        } else { // face has to be 1 or 3
            int multiplyDirection = -(2 - face); // either -1 or 1
            cubeX = (multiplyDirection) * -(mSideWidth / 2);
            cubeZ = -(mSideWidth / 2) * (multiplyDirection) + (sideMod * multiplyDirection);
        }

        return new Vertex(cubeX, boardY, cubeZ);
    }

    /**
     * Add a cube to a slot in the board, by specifying its board position
     * and the cube colors.
     *
     * @param x the x board position of the cube to commit
     * @param y the y board position
     * @param cubeReference the cube reference you want to use for this slot, color essentially
     */
    private void commitCube(int x, int y, float[] cubeReference) {
        Vertex v = calculateCubePosition(x, y);
        mBoard[x][y] = new CubeInstance(cubeReference);
        mBoard[x][y].setPosition(v.x, v.y, v.z);
        addChild(mBoard[x][y].withParent(this));
    }

    /**
     * Commites a given cube to the board space. Takes ownership
     * of the renderable cube. Assumes the cube has already been added to the scene.
     *
     * @param facex the final board space x offset
     * @param facey the final board space y offset
     * @param c the cube instance the board will assume responsiblity for
     */
    public void commit(int facex, int facey, CubeInstance c) {
        int trueX = trueX(facex);
        Vertex v = calculateCubePosition(trueX, facey);

        Log.d("CubeBoard", "commit (" + trueX + ", " + facey + ")");
        mBoard[trueX][facey] = c;
        mBoard[trueX][facey].setPosition(v.x, v.y, v.z);
        addChild(c.withParent(this));
        mCommitLocations.add(new Point(trueX, facey));
    }

    @Override
    public boolean update(long msDelta) {
        // update all of its children, which will include the active piece,
        // and any extra cubes that are falling off, or particle system, and
        // even the cube board elements themselves.
        updateChildren(msDelta);

        // update the active piece, as it is not a child of the board, but captive/hostage
        if(null != mActivePiece) { mActivePiece.update(msDelta); }

        // remove all the dead cubes from the render reference if they've died from child updates
        Iterator<CubeInstance> i = mExtraCubes.iterator();
        while(i.hasNext()) {
            CubeInstance cube = i.next();
            if(cube.isDead()) {
                cube.cleanup();
                removeChild(cube.withParent(null));
                i.remove();
            }
        }

        // update the rotation of the board if we our target active face
        // is not our active face
        if (mTargetFace != mActiveFace) {
            mElapsedRotationTime += msDelta / 1000.0f;
            float angleDifference = mFaceRotations[mTargetFace] - mFaceRotations[mActiveFace];
            if (angleDifference > 90.0f) {
                angleDifference = -90.0f;
            } else if (angleDifference < -90.0f ) {
                angleDifference = 90.0f;
            }
            mBoardModelRotation = mFaceRotations[mActiveFace] +
                    (mElapsedRotationTime / mBoardRotationSpeed) * angleDifference;

            // if we've completed the rotation, clamp to the target face
            if (mElapsedRotationTime > mBoardRotationSpeed) {
                mActiveFace = mTargetFace;
                mBoardModelRotation = mFaceRotations[mActiveFace];
                mElapsedRotationTime = 0;
            }
        }

        return true;
    }

    @Override
    public void cleanup() {
        mBoard = null;
        super.cleanup();
    }

    @Override
    public void render(Camera camera) {
        if (!mInitGL) { initGL(); }

        if(mActivePiece.isCommitted()) {
            loadNextActivePiece();
        }

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, -getBoardRotation(), 0, 1.0f, 0);
        camera.pushModelState(mModelMatrix);

        // render the line grid
        mRenderer.renderLineGrid(camera, this);

        // render the board, we've already rotated it!
        synchronized(mBoard) {
            mRenderer.render(camera, this);
        }

        // hack: when we come back, the renderer has popped our rotation off the stack
        // to render the active piece by using the shader's configuration. i think im
        // pre-optimizing this matrix multiplication out
        camera.pushModelState(mModelMatrix);

        // render the board's children
        renderChildren(camera);

        camera.popModelState();

        // the very lat thing to render is the active piece!
        if (null != mActivePiece) { mActivePiece.render(camera); }
    }

    public void testFill() {
        Random r = new Random();

        // fill the board with random cubes for testing
        for (int x = 0; x < mBoardWidth; x++) {
            int randomHeight = r.nextInt(3);

            for (int y = 0; y < randomHeight; y++) {
                commitCube(x, y, CubeLibrary.getInstance().getRandomCubeBuffer());
            }
        }
    }

    // starts the game based on the current board state
    public void start() {
        loadNextActivePiece();
    }

    /**
     * Will attempt to move the active piece, if there is one, in
     * the direction specified.
     * @param xDiff x differential for piece movement
     * @param yDiff y differential for piece movement
     * @return true if successful, false if it couldn't for some reason
     */
    public boolean movePiece(int xDiff, int yDiff) {
        if (null == mActivePiece) {
            return false;
        }

        if (mActivePiece.movePiece(xDiff, yDiff, true)) {
            fireBoardEvent(mPieceMoveStrategy);
            return true;
        }

        return false;
    }

    public int getSideWidth() {
        return mSideWidth;
    }

    public int getBoardHeight() {
        return mBoardHeight;
    }

    /**
     * Determines if the relative face for the active
     * face is occupied or not.
     *
     * @param faceX the x offset from the left side of the active face
     * @param faceY the y offset from the righ side of the active face
     * @return true if the face space is occupied, or false otherwise
     */
    public boolean isFaceSpaceOccupied(int faceX, int faceY) {
        // ignore anything out the top, for now
        if (faceY >= mBoardHeight) {
            return false;
        }

        // if we hit the floor, I'd say thats occupied.
        if (faceY < 0) {
            return true;
        }

        // its this position technically off the side of the face?
        if (faceX < 0 || faceX > (mSideWidth-1)) {
            return true;
        }

        // the maths happen
        return (null != mBoard[(mActiveFace * mSideWidth - mActiveFace + faceX) % mBoardWidth][faceY]);
    }

    public Iterator<CubeInstance> getDropCollisionCubeInstances() {
        return mActivePiece.getDropCollisionCubeInstances();
    }

    /**
     * Will make the active piece move at twice its normal speed.
     */
    public void modulePieceSpeed(int speedFactor) {
        if (null != mActivePiece) {
            mActivePiece.modulateSpeed(speedFactor);
            fireBoardEvent(mPieceHurryStrategy);
        }
    }

    /**
     * Used by the CubeBoardPiece to communicate a successful slide
     * to the board listeners.
     */
    public void onPieceSlide() {
        fireBoardEvent(mPieceSlideStrategy);
    }

    public void onPieceCommit() {
        mWasBadMove = false;
        // go through each commit location, and see if the space below it can reach its
        // way to the top. we are looking for "traps"
        for(Point p : mCommitLocations) {
            if (!hasSafeLanding(p.x, p.y - 1)) {
                mWasBadMove = true;
                break;
            }
        }

        fireBoardEvent(mPieceCommitStrategy);
        mLastMoveLineCompletionCount = 0;
        mCommitLocations.clear();
    }

    /**
     * Will attempt to soak up all empty spaces and mark them as good if they can get to the top.
     *
     * @param boardX the x board position
     * @param boardY the y board position
     * @return true if this position is considered a safe landing, defined as either being immediately
     *         occupied, or doesn't exist within a "cavern" in the board which means line completions
     *         are blocked.
     */
    private boolean hasSafeLanding(int boardX, int boardY) {
        // if we are on the bottom of the board, or its occupied that's cool
        if (boardY < 0 || null != mBoard[boardX][boardY]) {
            return true;
        }

        // we know its not occupied, so lets see if we can reach the top
        mPathLocations.clear();
        return canFindTop(mPathLocations, boardX, boardY);
    }

    private boolean canFindTop(HashMap<String,String> paths, int x, int y) {
        String hashKey = "(" + x + "," + y + ")";
        boolean foundTop = false;

        // do some simple bounds checking
        if (x < 0 || x >= mBoardWidth || y < 0) {
            return false;
        }
        // if we have reached the top slot, then we have found the top!
        if (y >= (mBoardHeight - 1)) {
            return true;
        }

        // if the space is empty, add it to the hash and search its surroundings.
        // if the space is already in the hash, ignore it.
        if (null == mBoard[x][y] && !paths.containsKey(hashKey)) {
            paths.put(hashKey, hashKey);
            foundTop = foundTop || canFindTop(paths, x - 1, y) || canFindTop(paths, x + 1, y) ||
                canFindTop(paths, x, y - 1) || canFindTop(paths, x, y + 1);
        }

        return foundTop;
    }

    public void dropActivePiece() {
        if (null != mActivePiece && !mActivePiece.isDropping()) {
            mActivePiece.drop();
            fireBoardEvent(mPieceDropStrategy);
        }
    }

    public void calculateActivePieceAverageModelPosition(Vertex v) {
        if (mActivePiece != null) {
            mActivePiece.calculateAverageModelPosition(v);
        }
    }

    /**
     * Used by BoardPieces when they are done committing themselves to the board. Runs all of
     * the logic used to complete the line, including star rating heuristics, and removing cubes
     * from the board.
     */
    public void testLineCompletion() {
        Random r = new Random();
        boolean didRowComplete = false;
        synchronized(mBoard) {
            // go through each row, and see if any of them are null or not. we start at the top to
            // simplify the implications of a row completion, as everything above it has already
            // been checked.
            for (int y = mBoardHeight - 1; y >= 0; y--) {
                boolean rowCompleted = true;

                // stop when we find an empty slot
                for (int x = 0; x < mBoardWidth; x++) {
                    if (null == mBoard[x][y]) {
                        rowCompleted = false;
                        break;
                    }
                }

                // if the row is completed, then remove the cube references from the board,
                // add them directly to the scene with a trajectory, and move everything above
                // that slot down one row.
                if (rowCompleted) {
                    mLastMoveLineCompletionCount += 1;
                    didRowComplete = true;
                    for (int x = 0; x < mBoardWidth; x++) {
                        if (mExtraCubes.size() < MAX_EXTRA_CUBES) {
                            CubeInstance c = mBoard[x][y];

                            // the reference will be removed from the board when everything
                            // moves down, but eject this one off the board and keep a reference
                            // so they are rendered outside of the rotation matrix
                            int face = x / (mSideWidth - 1);
                            c.flash(325);
                            c.setTrajectory(mFaceNormals[face].x * 10 + r.nextFloat() * 4.0f,
                                    mFaceNormals[face].y * 10 + r.nextFloat() * 4.0f,
                                    mFaceNormals[face].z * 10 + r.nextFloat() * 4.0f,
                                    0, -9.8f, 0);
                            c.setRotation(mFaceNormals[face].z + r.nextFloat(),
                                    mFaceNormals[face].y + r.nextFloat(),
                                    mFaceNormals[face].x + r.nextFloat(),
                                    (float)Math.PI * r.nextFloat());
                            mExtraCubes.add(c);
                        } else {
                            // if we do not have enough, remove the child.
                            removeChild(mBoard[x][y].withParent(null));
                        }

                        // move everything above this slot down one reference, and pull in
                        // a null reference for the top.
                        for (int moveY = y; moveY < mBoardHeight; moveY++) {
                            if (moveY + 1 >= mBoardHeight) {
                                mBoard[x][moveY] = null;
                            } else {
                                mBoard[x][moveY] = mBoard[x][moveY + 1];
                                if (null != mBoard[x][moveY]) {
                                    Vertex p = calculateCubePosition(x, moveY);
                                    mBoard[x][moveY].setPosition(p.x, p.y, p.z);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (didRowComplete) {
            fireBoardEvent(mLineCompleteStrategy);
        }
    }

    private void loadNextActivePiece() {
        // generate the player's piece
        mActivePiece = new CubeBoardPiece(this,
                CubeBoardPiece.STYLES[(new Random()).nextInt(CubeBoardPiece.STYLES.length)],
                CubeLibrary.getInstance().getRandomCubeBuffer());
    }

    private void fireBoardEvent(IListenerStrategy strategy) {
        for(ICubeBoardEventListener l : mListeners) {
            strategy.fire(l, this);
        }
    }

    public static int getBoardWidth(int sideWidth) {
        return (sideWidth * mSideCount) - mSideCount;
    }

    public int getLastMoveLineCompletionCount() {
        return mLastMoveLineCompletionCount;
    }

    private int trueX(int faceX) {
        return (mActiveFace * mSideWidth - mActiveFace + faceX) % (mBoardWidth);
    }

}
