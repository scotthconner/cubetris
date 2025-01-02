package com.scotthconner.cubetrisrebooted.lib.object.text;

import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Font;

/**
 * Created by scottc on 3/2/16.
 */
public class FPSCounter extends FontText {
    // used to determine what the frame rate is
    private long mFrameMsElapsed;
    private int  mFrameCount;
    private int  mPreviousFramesPerSecond;

    public FPSCounter(Font f) {
        super(f);
        setText("00");
    }

    @Override
    public boolean update(long msDelta) {
        mFrameMsElapsed += msDelta;
        if (mFrameMsElapsed >= 1000) {
            setText("" + mFrameCount);
            mFrameCount = 0;
            mFrameMsElapsed = 0;
        }

        return true;
    }

    @Override
    public void render(Camera c) {
        mFrameCount += 1;
        super.render(c);
    }
}
