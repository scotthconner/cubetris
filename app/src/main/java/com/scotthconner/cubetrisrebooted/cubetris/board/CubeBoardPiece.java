package com.scotthconner.cubetrisrebooted.cubetris.board;

import android.graphics.Point;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.SceneObject;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * CubeBoardPiece
 *
 * This class is tightly coupled with CubeBoard, as CubeBoard controls the lifespan and manipulation
 * of the cube board piece.
 *
 * Created by scottc on 1/30/16.
 */
public class CubeBoardPiece extends SceneObject {
    // piece definitions
    public static final Point[] STYLE_SQUARE = {
            new Point(0,  0),
            new Point(1,  0),
            new Point(1, -1),
            new Point(0, -1)
    };
    public static final Point[] STYLE_Z = {
            new Point( 0,  0),
            new Point(-1,  0),
            new Point( 0, -1),
            new Point( 1, -1)
    };
    public static final Point[] STYLE_S = {
            new Point( 0,  0),
            new Point( 1,  0),
            new Point( 0, -1),
            new Point(-1, -1)
    };
    public static final Point[] STYLE_L = {
            new Point(0,  1),
            new Point(0,  0),
            new Point(0, -1),
            new Point(1, -1)
    };
    public static final Point[] STYLE_R = {
            new Point( 0,  1),
            new Point( 0,  0),
            new Point( 0, -1),
            new Point(-1, -1)
    };
    private static final Point[] STYLE_T = {
            new Point( 0,  0),
            new Point(-1,  0),
            new Point( 1,  0),
            new Point( 0, -1)
    };
    public static final Point[] STYLE_I = {
            new Point(0,  1),
            new Point(0,  0),
            new Point(0, -1),
            new Point(0, -2)
    };
    public static final Point[][] STYLES = {
        STYLE_SQUARE,
        STYLE_Z,
        STYLE_S,
        STYLE_L,
        STYLE_R,
        STYLE_T,
        STYLE_I
    };

    /**
     * CubeBoardPieceElement
     *
     * Struct that binds the renderable cube references within the scene to their
     * logical offsets around the piece's center of 0,0. These points are used for board
     * logic, as well as calculating rotations of the cube's physical pieces.
     */
    private class CubeBoardPieceElement {
        public CubeInstance mCube;
        public Point mPoint;

        public CubeBoardPieceElement(CubeInstance c, int x, int y) {
            mCube = c;
            mPoint = new Point(x,y);
        }
    }

    // constants
    private static final int DEFAULT_FALL_SPEED =             600;
    private static final float SLIDE_SENSITIVIY =           0.60f;
    private static final int DROP_SPEED         =              30;

    // external references, coupled objects
    CubeBoard mCubeBoard;

    // piece movement
    private int mRestingSpeed;       // unacellerated speed.
    private int mFallSpeed;          // milliseconds for a piece drop
    private int mFallTimeElapsed;    // number of elapsed milliseconds since last drop
    private int mSlideDirection;     // -1 or 1 in the x direction usually
    private boolean mSliding;        // toggle which will allow a drop-side
    private boolean mCanRotate;      // determines if the piece can rotate
    private boolean mDropping;       // perminent state of super fast fall speed

    // the renderable pieces, and good information about them
    private final CubeBoardPieceElement[]  mCubes; // the actual cubes bound with their board offsets
    private boolean[][] mFaceMask; // used for some retrieval operations

    // the piece's face position
    int mFaceX;
    int mFaceY;

    // has this piece been committed?
    boolean mCommitted;

    // renderable buffer
    private float[][] mVertexBuffer;

    /**
     * Creates a new cube board piece. Represents the cube renderables themselves,
     * which will be owned by the board ultimately. Also controls its own rotation.
     * Is ultimately unaware of its "board position"
     *
     * @param cb the cube board the piece is interacting with
     * @param style array of points describing block piece placement around its center (0,0)
     */
    public CubeBoardPiece(CubeBoard cb, Point[] style, float[] cubeBufferReference) {
        // initialize the data structures
        mVertexBuffer = new float[4][];
        mSliding = false;
        mCubeBoard = cb;
        mCommitted = false;
        mDropping = false;
        mCanRotate = (style != CubeBoardPiece.STYLE_SQUARE);
        mScene = mCubeBoard.getScene();
        mCubes = new CubeBoardPieceElement[style.length];
        mFaceMask = new boolean[cb.getSideWidth()][cb.getBoardHeight()];

        // set intial state
        mRestingSpeed = DEFAULT_FALL_SPEED;
        mFallSpeed = mRestingSpeed;
        mFallTimeElapsed = 0;

        // always keep the cubes facing the player, so keep track of the piece's
        // position based on the active face
        mFaceX = mCubeBoard.getSideWidth() / 2;
        mFaceY = mCubeBoard.getBoardHeight();

        for( int x = 0; x < style.length; x++) {
            mCubes[x] = new CubeBoardPieceElement(new CubeInstance(cubeBufferReference),
                    style[x].x, style[x].y);
            addChild(mCubes[x].mCube.withParent(this));

            // line up the cubes in model space to always be on the front face
            // and in the top middle, based on the piece's position within
            // the style matrix
            Vertex v = getModelSpacePosition(mFaceX + style[x].x, mFaceY + style[x].y);
            mCubes[x].mCube.setPosition(v.x, v.y, v.z);
        }
    }

    @Override
    public boolean update(long msDelta) {
        updateChildren(msDelta);

        if (isCommitted()) {
            return false;
        }

        synchronized(mCubes) {
            // always keep track of the fall time elapsed, as the piece
            // is continually 'falling' down the board
            mFallTimeElapsed += msDelta;

            // is it time for the piece to move down?
            if (mFallTimeElapsed >= mFallSpeed) {
                // move the piece down vertically on the board, but if we can't, we need
                // to commit all of the pieces into place.
                if (!movePiece(0, -1, true)) {
                    // so we've moved down now did the player activate the slide
                    // technique to potentially move in their previous requested x direction?!?!
                    if (mSliding) {
                        Log.d("CubeBoardPiece","update:: attempting to slide..");
                        // try to commit in the previous slide direction ......
                        if (movePiece(mSlideDirection,0, true)) {
                            // wow, congrats dude.
                            Log.d("CubeBoardPiece", "update:: Boom! Player just executed a hard slide!");
                            mCubeBoard.onPieceSlide();
                        } else {
                            // fail
                            Log.d("CubeBoardPiece", "update:: SlideFail! Committing...");
                            commitToBoard();
                            resetFallCycle();
                            return false;
                        }
                    } else {
                        commitToBoard();
                        resetFallCycle();
                        return false;
                    }
                } else {
                    // so we've moved down now did the player activate the slide
                    // technique to potentially move in their previous requested x direction?!?!
                    if (mSliding) {
                        Log.d("CubeBoardPiece","update:: attempting to slide after fall..");
                        // try to commit in the previous slide direction ......
                        if (movePiece(mSlideDirection,0, true)) {
                            // wow, congrats dude.
                            Log.d("CubeBoardPiece", "update:: Boom! Player just executed a scoop slide!");
                            mCubeBoard.onPieceSlide();
                        }
                    }

                    // once our piece has settled, if we could move down,
                    // did we just move down onto a piece below us that we can't "fall into?"
                    if (!movePiece(0, -1, false)) {
                        //no ? ah, I guess that's where it goes then.
                        commitToBoard();
                        resetFallCycle();
                        return false;
                    }
                }

                resetFallCycle();
            } else {
                float yDiff = ((float) mFallTimeElapsed / (float) mFallSpeed * 1.0f); // hardcode cube size
                int faceY = mFaceY;
                // things should move into the space its going to soon occupy, so lets move the pieces down on
                // a y differential based on the fall speed, and the ratio of time elapsed
                for(CubeBoardPieceElement c : mCubes) {
                    Vertex cubePos = c.mCube.getPosition();
                    c.mCube.setPosition(cubePos.x, faceY + c.mPoint.y - yDiff, cubePos.z);
                }
            }
        }
        return true;
    }

    @Override
    public void render(Camera camera) {
        renderChildren(camera);
    }

    @Override
    public void setScene(Scene scene) {
        mScene = scene;
    }
    public Scene getScene() {
        return mScene;
    }

    public boolean isCommitted() {
        return mCommitted;
    }

    /**
     * Will move the piece along the face grid by using the x and y
     * differentials. Returns true if it was sucessful, or false if it wasn't.
     *
     * @param xDiff x grid differential
     * @param yDiff y grid differential
     * @param commit true if you want to commit the move, false if you want to test it
     * @return true if the piece moved, false if it couldn't
     */
    public boolean  movePiece(int xDiff, int yDiff, boolean commit) {
        if (isCommitted()) {
            return false;
        }

        synchronized(mCubes) {
            int targetFaceX = mFaceX + xDiff;
            int targetFaceY = mFaceY + yDiff;

            boolean collisionDetected = false;
            boolean slideCollisionDetected = false;

            // are we too far left or right based on the size of the face? deal breaker
            if (((mFaceX + xDiff + leftOffsetMax()) < 0) ||
                    ((mFaceX + xDiff + rightOffsetMax()) >= mCubeBoard.getSideWidth())) {
                return false;
            }

            // would we go through the floor of the board?
            if (targetFaceY < 0) {
                collisionDetected = true;
            }

            // if there is any collision for any piece in their target spaces,
            // we need to return false so it doesn't happen
            for(CubeBoardPieceElement c : mCubes) {
                if (mCubeBoard.isFaceSpaceOccupied(targetFaceX + c.mPoint.x,
                        targetFaceY + c.mPoint.y)) {
                    if (xDiff != 0) {
                        slideCollisionDetected = true;
                    }
                    collisionDetected = true;
                }

                // also, if there is an x differential also see if
                // the x-neighbor below will have any problems visually. we do
                // this because the block animates "into" the space its going to be soon
                // occupying
                if (xDiff != 0 && mFallTimeElapsed != 0) {
                    if (mCubeBoard.isFaceSpaceOccupied(targetFaceX + c.mPoint.x,
                            targetFaceY + c.mPoint.y - 1)) {
                        Log.d("CubeBoard::MovePiece","visual inspection detection");
                        collisionDetected = true;
                    }
                }
            }

            // are we potentially trying to toggle a slide maneuver?
            if(slideCollisionDetected &&
                    ((float)mFallTimeElapsed / (float)mFallSpeed) >= SLIDE_SENSITIVIY) {
                Log.d("CubeBoardPiece","::movePiece slide toggled (slideDirection: " + xDiff + ")");

                // store the intent to slide for when the next drop happens
                mSliding = true;
                mSlideDirection = xDiff;
            }

            // if there was a collision, party is over
            if (collisionDetected) {
                return false;
            }

            if (commit) {
                // we've eliminated all collision possibilities, so
                // change the face position logically
                mFaceX += xDiff;
                mFaceY += yDiff;

                // if we are falling, reset the time elapsed
                if (yDiff < 0) {
                    mFallTimeElapsed = 0;
                }

                // go through each piece and move it in model space
                for(CubeBoardPieceElement c : mCubes) {
                    Vertex cubePos = c.mCube.getPosition();
                    c.mCube.setPosition(cubePos.x + xDiff,
                            mFaceY + c.mPoint.y, cubePos.z);
                }
            }

        }
        // we were able to move the piece
        return true;
    }

    /**
     * Makes the piece fall twice as fast as its resting speed.
     *
     * @param speedFactor how many times faster you want the piece to fall.
     */
    public void modulateSpeed(int speedFactor) {
        if (mDropping) {
            // you'd like that huh?
            return;
        }

        // we also want to module the elapsed fall time so there is no jitter, so
        // hold onto the current Y offset
        float currentYOffset = (float)mFallTimeElapsed / (float)mFallSpeed * 1.0f ; // 1.0f cube size

        // module the current fall speed
        mFallSpeed = mRestingSpeed / speedFactor;

        // calculate what fall elapsed time should be with the new speed to reach the same offset
        // to keep the jitter from happening when the fall ratio is calculated
        mFallTimeElapsed = (int)(currentYOffset * (mFallSpeed * 1.0f)); // 1.0f cube size
    }

    /**
     * rotate the piece either clockwise (1) or counterclockwise (-1)
     * @param direction -1 or 1.
     */
    public boolean rotate(int direction) {
        if(!mCanRotate || mDropping) {
            return false;
        }

        synchronized(mCubes) {
            // see if the rotation would cause a collision on the board
            for(CubeBoardPieceElement c : mCubes) {
                // rotate the pieces logically
                int newFaceX = mFaceX + (direction * c.mPoint.y);
                int newFaceY = mFaceY + (-direction * c.mPoint.x);
                if (mCubeBoard.isFaceSpaceOccupied(newFaceX, newFaceY)) {
                    return false; // there is a collision, don't do anything.
                }
            }

            // then actually do it
            for(CubeBoardPieceElement c : mCubes) {
                // rotate the pieces logically
                int newX = (direction * c.mPoint.y);
                int newY = (-direction * c.mPoint.x);
                c.mPoint.x = newX;
                c.mPoint.y = newY;

                // now with the new position, "lock" them in visually.
                Vertex v = getModelSpacePosition(mFaceX + c.mPoint.x, mFaceY + c.mPoint.y);
                c.mCube.setPosition(v.x, v.y, v.z);
            }
        }

        return true;
    }

    /**
     * Sets a permenant state of a very fast fall speed,
     * its essentially committing the block
     */
    public void drop() {
        mDropping = true;
        mFallSpeed = DROP_SPEED;
    }

    public boolean isDropping() {
        return mDropping;
    }

    /**
     * Provides an array of points that represent the relative
     * face position starting in the bottom left of the face.
     *
     * @return an array of translated points with face position and offsets added together
     */
    public Point[] getPieceFacePositions() {
        synchronized(mCubes) {
            Point[] points = new Point[mCubes.length];
            for (int x = 0; x < mCubes.length; x++) {
                points[x] = new Point(mCubes[x].mPoint.x + mFaceX,
                        mCubes[x].mPoint.y + mFaceY);
            }

            return points;
        }
    }

    /**
     * Will write the average model position of all the cubes into the vertex
     * @param v the vertex you want to contain the average model position
     */
    public void calculateAverageModelPosition(Vertex v) {
        v.set(0, 0, 0);
        for (CubeBoardPieceElement cube : mCubes) {
            Vertex cv = cube.mCube.getPosition();
            v.x += cv.x;
            v.y += cv.y;
            v.z += cv.z;
        }

        v.x /= 4.0f;
        v.y /= 4.0f;
        v.z /= 4.0f;
    }

    /**
     * Will write the average model position of all the cubes into the vertex
     * @param v the vertex you want to contain the average model position
     */
    public void calculateAverageModelFacePosition(Vertex v) {
        v.set(0, 0, 0);
        for (Point p : getPieceFacePositions()) {
            Vertex cv = getModelSpacePosition(p.x, p.y);
            v.x += cv.x;
            v.y += cv.y;
            v.z += cv.z;
        }

        v.x /= 4.0f;
        v.y /= 4.0f;
        v.z /= 4.0f;
    }


    /**
     * Provides an array of points that represent the relative
     * face position starting in the bottom left of the face.
     *
     * @return an array of translated points with face position and offsets added together
     */
    public Iterator<CubeInstance> getDropCollisionCubeInstances() {
        LinkedList<CubeInstance> collisionCubes = new LinkedList<>();

        synchronized(mCubes) {
            Point[] facePositions = getPieceFacePositions();

            // populate the face mask, a virtual boolean representation
            // of the face board, with only the active piece mapped onto it
            int cubeIndex = 0;
            for(Point p : facePositions) {
                // if it will be out of bounds, just add it...
                if (p.x < 0 || p.x > (mCubeBoard.getSideWidth()-1) || p.y >= mCubeBoard.getBoardHeight()) {
                    collisionCubes.add(mCubes[cubeIndex].mCube);
                } else {
                    mFaceMask[p.x][p.y] = true;
                }
                cubeIndex++;
            }

            // determine the collisions downward, and find any true block that has an empty spot
            // below it
            cubeIndex = 0;
            for(Point p : facePositions) {
                if (p.y < mCubeBoard.getBoardHeight() && (p.y == 0 || (p.y != 0 && !mFaceMask[p.x][p.y-1]))) {
                    collisionCubes.add(mCubes[cubeIndex].mCube);
                }
                cubeIndex++;
            }

            // wipe the face mask
            for(Point p : facePositions) {
                if (!(p.x < 0 || p.x > (mCubeBoard.getSideWidth()-1) || p.y >= mCubeBoard.getBoardHeight())) {
                    mFaceMask[p.x][p.y] = false;
                }
            }
        }

        return collisionCubes.iterator();
    }

    /**
     * Provides an array of cube instances that represent the relative
     * face position starting in the bottom left of the face.
     *
     * @param slideDirection -1 for left, 1 for right
     * @return cube instances that are on the face of the slide
     */
    public Iterator<CubeInstance> getSlideCollisionCubeInstances(int slideDirection) {
        LinkedList<CubeInstance> collisionCubes = new LinkedList<>();

        synchronized(mCubes) {
            Point[] facePositions = getPieceFacePositions();

            // populate the face mask, a virtual boolean representation
            // of the face board, with only the active piece mapped onto it
            int cubeIndex = 0;
            for(Point p : facePositions) {
                // if it will be out of bounds, skip it.
                if (!(p.x < 0 || p.x > (mCubeBoard.getSideWidth()-1) || p.y >= mCubeBoard.getBoardHeight())) {
                    mFaceMask[p.x][p.y] = true;
                }
                cubeIndex++;
            }

            // determine the collisions downward, and find any true block that has an empty spot
            // below it
            cubeIndex = 0;
            for(Point p : facePositions) {
                // if the slide target is out of face bounds add it
                if ((p.x + slideDirection) < 0 || (p.x + slideDirection) > mCubeBoard.getSideWidth()) {
                    collisionCubes.add(mCubes[cubeIndex].mCube);
                } else if (p.y < mCubeBoard.getBoardHeight() && (p.y == 0 || (!mFaceMask[p.x+slideDirection][p.y]))) {
                    // only add it if there isn't a piece block next to it in that direction
                    collisionCubes.add(mCubes[cubeIndex].mCube);
                }
                cubeIndex++;
            }

            // wipe the face mask
            for(Point p : facePositions) {
                if (!(p.x < 0 || p.x > (mCubeBoard.getSideWidth()-1) || p.y >= mCubeBoard.getBoardHeight())) {
                    mFaceMask[p.x][p.y] = false;
                }
            }
        }

        return collisionCubes.iterator();
    }

    public float[][] getVertexBuffers() {
        for(int x = 0; x < mCubes.length; x++) {
            mVertexBuffer[x] = mCubes[x].mCube.getVertexBuffer();
        }
        return mVertexBuffer;
    }

    /**
     * Returns a model space position of the matrix slot, based on position on the board
     * face. Is based on universal model space and not rotated board space.
     *
     * @param faceX the x offset within the piece matrix
     * @param faceY the y offset within the piece matrix
     * @return a full vertex with model position for that space in the piece
     */
    private Vertex getModelSpacePosition(int faceX, int faceY) {
        return new Vertex( -mCubeBoard.getSideWidth() / 2.0f + faceX + 0.5f,
                faceY,
                mCubeBoard.getSideWidth() / 2.0f - 0.5f );
    }

    /**
     *
     * @return the left-most offset from any of the points inside the pieces
     */
    private int leftOffsetMax() {
        int xMin = 0;

        synchronized(mCubes) {
            for(CubeBoardPieceElement c : mCubes) {
                if (c.mPoint.x < xMin) { xMin = c.mPoint.x; }
            }
        }
        return xMin;
    }

    /**
     *
     * @return the left-most offset from any of the points inside the pieces
     */
    private int rightOffsetMax() {
        int xMax = 0;

        synchronized(mCubes) {
            for(CubeBoardPieceElement c : mCubes) {
                if (c.mPoint.x > xMax) {
                    xMax = c.mPoint.x;
                }
            }
        }
        return xMax;
    }

    /**
     * Do this when we are ready to pour concrete on the piece.
     */
    private void commitToBoard() {
        // commit each block to the board
        for(CubeBoardPieceElement c : mCubes) {
            // the board now owns the cube, so commit it
            removeChild(c.mCube.withParent(null));
            mCubeBoard.commit(mFaceX + c.mPoint.x, mFaceY + c.mPoint.y, c.mCube);

            // animate the piece as we commit it with a flash.
            c.mCube.flash(600);
        }

        // test line completion now that the board holds these cubes
        mCubeBoard.testLineCompletion();

        // broadcast to the board listeners that a full commit has been done.
        mCubeBoard.onPieceCommit();

        // kill this piece
        mCommitted = true;
    }

    private void resetFallCycle() {
        // reset all of the fall logic after the piece moved
        mFallTimeElapsed = 0;
        mSliding = false;
    }
}
