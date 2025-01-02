package com.scotthconner.cubetrisrebooted.lib.render.core;

import android.util.Log;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;

import java.util.Stack;
import java.util.Vector;

/**
 * SceneObject
 *
 * Base implementation of an IRenderable. Manages the state, and adds helper and builder methods
 * for managing a scene position.
 *
 * Allows the ability to add children, with helper update and render methods that makes attaching
 * pieces into the scene graph easier to manage.
 *
 * Created by scottc on 3/2/16.
 */
public abstract class SceneObject implements IRenderable {
    // the scene reference of the scene it belongs to
    protected Scene mScene;

    // the parent and children references in the graph
    protected SceneObject mParent;
    protected final Vector<IRenderable> mChildren;
    private Stack<IRenderable> mIncomingChildren;
    private Stack<IRenderable> mDustBin;

    // object state
    protected float[] mModelMatrix;
    protected Vertex mPosition;
    protected boolean mIsDead;

    public SceneObject() {
        mModelMatrix = new float[16];
        mIncomingChildren = new Stack<>();
        mDustBin = new Stack<>();
        mPosition = new Vertex();
        mChildren = new Vector<>();
        mParent = null;
        mIsDead = false;
    }

    public void setScene(Scene scene) {
        mScene = scene;
    }

    public Scene getScene() {
        return mScene;
    }

    public SceneObject getParent() { return mParent; }

    public Vertex getPosition() {
        return mPosition;
    }

    public SceneObject withPosition( float x, float y, float z) {
        mPosition = new Vertex(x, y, z);
        return this;
    }

    public SceneObject withParent(SceneObject parent) {
        mParent = parent;
        return this;
    }

    public void addChild(IRenderable renderable) {
        synchronized(mChildren) {
            mIncomingChildren.add(renderable);
        }
    }

    public boolean removeChild(IRenderable renderable) {
        synchronized(mChildren) {
            mDustBin.push(renderable);
        }
        return true;
    }

    public void updateChildren(long msDelta) {
        synchronized(mChildren) {
            while(!mDustBin.empty()) {
                mChildren.remove(mDustBin.pop());
            }

            for(IRenderable  incoming: mIncomingChildren) {
                mChildren.add(incoming);
            }
            mIncomingChildren.clear();

            for(IRenderable renderable : mChildren) {
                if(!renderable.update(msDelta)) {
                    Log.d("SceneObject", "Cleaning up object = " + renderable.toString());
                    renderable.cleanup();
                    mDustBin.push(renderable);
                }
            }

            while (!mDustBin.empty()) {
                mChildren.remove(mDustBin.pop());
            }
        }
    }

    public void renderChildren(Camera camera) {
        synchronized(mChildren) {
            for (IRenderable child : mChildren) {
                child.render(camera);
            }
        }
    }

    public boolean isDead() {
        return mIsDead;
    }

    public void cleanupChildren() {
        synchronized(mChildren) {
            for (IRenderable child : mChildren) {
                // only clean up the child if it isn't in the dustbin by being removed.
                // if its been removed at this point, we can assume the ownership has changed
                if (!mDustBin.contains(child)) { child.cleanup(); }
            }
            mChildren.clear();
        }
    }

    @Override
    public void cleanup() {
        cleanupChildren();
    }
}
