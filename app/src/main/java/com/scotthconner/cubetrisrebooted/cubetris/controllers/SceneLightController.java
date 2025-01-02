package com.scotthconner.cubetrisrebooted.cubetris.controllers;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IPlayerController;
import com.scotthconner.cubetrisrebooted.lib.render.core.Scene;

/**
 * Created by scottc on 1/23/16.
 */
public class SceneLightController implements IPlayerController {
    Scene mScene;

    // toggle and buffer triggers
    boolean mGasToggle;
    boolean mBrakeToggle;

    public SceneLightController(Scene scene){
        mScene = scene;
        mGasToggle = false;
        mBrakeToggle = false;
    }

    @Override
    public boolean processGenericMotionEvent(MotionEvent motionEvent) {
        float gas = motionEvent.getAxisValue(MotionEvent.AXIS_GAS);
        float brake = motionEvent.getAxisValue(MotionEvent.AXIS_BRAKE);

        if (gas > 0.0f) {
            if (!mGasToggle) {
                //mGasToggle = true;
                mScene.setAmbientFactor(Math.min(mScene.getAmbientFactor() + 0.02f, 1.0f));
                Log.d("LightSceneController", "ambient: " + mScene.getAmbientFactor());
            } else {
                mGasToggle = false;
            }

        } else if (brake > 0.0f) {
            if (!mBrakeToggle) {
                mScene.setAmbientFactor(Math.max(mScene.getAmbientFactor() - 0.02f, 0.0f));
                Log.d("LightSceneController", "ambient: " + mScene.getAmbientFactor());
                //mBrakeToggle = true;

            } else {
                mBrakeToggle = false;
            }
        }

        return false;
    }

    @Override
    public boolean processKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
            mScene.getSceneSun().attenuation += 0.0001f;
            Log.d("LightSceneController", "attenuation: " + mScene.getSceneSun().attenuation);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
            mScene.getSceneSun().attenuation -= 0.0001f;
            Log.d("LightSceneController", "attenuation: " + mScene.getSceneSun().attenuation);
            return true;
        }

        return false;
    }

    @Override
    public boolean processKeyUp(int keyCode, KeyEvent event) {
        Vertex lightPos = mScene.getSceneSun().position;
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            lightPos.y -= 0.5f;
            Log.d("LightSceneController", "light position: " + lightPos.x + ", " + lightPos.y + ", " + lightPos.z);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            lightPos.y += 0.5f;
            Log.d("LightSceneController", "light position: " + lightPos.x + ", " + lightPos.y + ", " + lightPos.z);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            lightPos.z -= 0.5f;
            Log.d("LightSceneController", "light position: " + lightPos.x + ", " + lightPos.y + ", " + lightPos.z);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            lightPos.z += 0.5f;
            Log.d("LightSceneController", "light position: " + lightPos.x + ", " + lightPos.y + ", " + lightPos.z);
        }


        return false;
    }
}
