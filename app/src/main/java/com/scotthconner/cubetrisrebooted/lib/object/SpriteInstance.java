package com.scotthconner.cubetrisrebooted.lib.object;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.IRenderable;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.IBlendFunction;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Sprite;

/**
 *
 *
 * Created by scottc on 2/22/16.
 */
public class SpriteInstance implements IRenderable {
    // Object references
    Scene          mScene;
    Sprite         mSprite;
    IBlendFunction mBlendFunction;

    // instance state
    Vertex mPosition;  // the sprite's position in model space

    public SpriteInstance() {
        mBlendFunction = null;
    }

    // IRENDERABLE OVERRIDES ////////////////////////////////////////
    @Override
    public boolean update(long msDelta) {
        return true;
    }

    @Override
    public void render(Camera camera) {
        mBlendFunction.enable();
        mSprite.render(camera, mPosition.x, mPosition.y, mPosition.z);
        mBlendFunction.disable();
    }

    @Override
    public void setScene(Scene scene) {
        mScene = scene;
    }

    @Override
    public Scene getScene() {
        return mScene;
    }

    public void cleanup() { }
    // IRENDERABLE OVERRIDES ////////////////////////////////////////

    // BUILDER //////////////////////////////////////////////////////
    public SpriteInstance withSprite(Sprite s) {
        mSprite = s;
        return this;
    }

    public SpriteInstance withBlendFunction(IBlendFunction b) {
        mBlendFunction = b;
        return this;
    }

    public SpriteInstance withPosition(float x, float y, float z) {
        mPosition = new Vertex();
        mPosition.set(x, y, z);
        return this;
    }

    public SpriteInstance withScene(Scene s) {
        setScene(s);
        return this;
    }
    /////////////////////////////////////////////////////////////////
}
