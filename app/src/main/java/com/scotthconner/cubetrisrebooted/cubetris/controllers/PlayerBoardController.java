package com.scotthconner.cubetrisrebooted.cubetris.controllers;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scotthconner.cubetrisrebooted.cubetris.board.CubeBoard;
import com.scotthconner.cubetrisrebooted.lib.gamestate.IPlayerController;

/**
 * Created by scottc on 1/23/16.
 */
public class PlayerBoardController implements IPlayerController {
    private CubeBoard mCubeBoard;

    // we use toggles to ensure the piece doesn't spin like crazy
    // so we buffer the input.
    boolean mGasToggle;
    boolean mBrakeToggle;

    public PlayerBoardController(CubeBoard cubeBoard){
        mCubeBoard = cubeBoard;
        mGasToggle = false;
        mBrakeToggle = false;
    }

    @Override
    public boolean processGenericMotionEvent(MotionEvent motionEvent) {
        float gas = motionEvent.getAxisValue(MotionEvent.AXIS_GAS);
        float brake = motionEvent.getAxisValue(MotionEvent.AXIS_BRAKE);

        if (gas > 0.0f) {
            if (gas == 1.0f && !mGasToggle) {
                mGasToggle = true;
                mCubeBoard.rotatePiece(1);
            } else {
                mGasToggle = false;
            }

        } else if (brake > 0.0f) {
            if (brake == 1.0f && !mBrakeToggle) {
                mBrakeToggle = true;
                mCubeBoard.rotatePiece(-1);
            } else {
                mBrakeToggle = false;
            }
        }

        return false;
    }

    @Override
    public boolean processKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
            mCubeBoard.rotate(1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
            mCubeBoard.rotate(-1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            mCubeBoard.movePiece(-1, 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mCubeBoard.movePiece(1,0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            mCubeBoard.modulePieceSpeed(4);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            mCubeBoard.dropActivePiece();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
            mCubeBoard.rotatePiece(-1);
        }

        return false;
    }

    @Override
    public boolean processKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            mCubeBoard.modulePieceSpeed(1);
        }

        return false;
    }
}
