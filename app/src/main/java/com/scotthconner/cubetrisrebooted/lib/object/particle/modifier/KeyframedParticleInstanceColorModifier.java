package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import android.graphics.Color;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.lib.core.GLHelper;
import com.scotthconner.cubetrisrebooted.lib.core.tween.ITweenFunction;
import com.scotthconner.cubetrisrebooted.lib.core.tween.TweenMode;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

import java.util.Vector;

/**
 * Created by scottc on 5/1/16.
 */
public class KeyframedParticleInstanceColorModifier implements IParticleModifier {
    public class ColorKeyFrame {
        public int mColorStart;        // the position of this key-frame
        public int mColorEnd;
        public int mColorChange[];          // the end position of this key frame
        private int mTweenBuffer;         // used as a buffer for calculations
        public float mDurationMs;      // the duration of the tween in milliseconds
        public ITweenFunction mTweenFunction; // the tween function into this position
        public TweenMode mTweenMode;          // ease in, out, or both

        public ColorKeyFrame() {
            mTweenMode = null;
            mTweenFunction = null;
            mDurationMs = 0;
        }

        public ColorKeyFrame withStartColor(int color) {
            mColorStart = color;
            return this;
        }

        public ColorKeyFrame withEndColor(int color) {
            mColorEnd = color;
            mColorChange = new int[]{
                Color.alpha(color) - Color.alpha(mColorStart),
                Color.red(color) - Color.red(mColorStart),
                Color.green(color) - Color.green(mColorStart),
                Color.blue(color) - Color.blue(mColorStart)
            };

            return this;
        }

        public ColorKeyFrame withTween(ITweenFunction f, TweenMode mode) {
            mTweenMode = mode;
            mTweenFunction = f;
            return this;
        }

        public ColorKeyFrame withDurationMs(float d) {
            mDurationMs = d;
            return this;
        }

        public int getTweenColor(float elapsedFrameMs) {
            switch(mTweenMode) {
                case EASE_IN:
                    mTweenBuffer = Color.argb(
                        (int)mTweenFunction.easeIn(elapsedFrameMs, Color.alpha(mColorStart), mColorChange[0], mDurationMs),
                        (int)mTweenFunction.easeIn(elapsedFrameMs, Color.red(mColorStart), mColorChange[1], mDurationMs),
                        (int)mTweenFunction.easeIn(elapsedFrameMs, Color.green(mColorStart), mColorChange[2], mDurationMs),
                        (int)mTweenFunction.easeIn(elapsedFrameMs, Color.blue(mColorStart), mColorChange[3], mDurationMs)
                    );
                    break;
                case EASE_OUT:
                    mTweenBuffer = Color.argb(
                        (int)mTweenFunction.easeOut(elapsedFrameMs, Color.alpha(mColorStart), mColorChange[0], mDurationMs),
                        (int)mTweenFunction.easeOut(elapsedFrameMs, Color.red(mColorStart), mColorChange[1], mDurationMs),
                        (int)mTweenFunction.easeOut(elapsedFrameMs, Color.green(mColorStart), mColorChange[2], mDurationMs),
                        (int)mTweenFunction.easeOut(elapsedFrameMs, Color.blue(mColorStart), mColorChange[3], mDurationMs)
                    );
                    break;
                case EASE_IN_OUT:
                    mTweenBuffer = Color.argb(
                        (int)mTweenFunction.easeInOut(elapsedFrameMs, Color.alpha(mColorStart), mColorChange[0], mDurationMs),
                        (int)mTweenFunction.easeInOut(elapsedFrameMs, Color.red(mColorStart), mColorChange[1], mDurationMs),
                        (int)mTweenFunction.easeInOut(elapsedFrameMs, Color.green(mColorStart), mColorChange[2], mDurationMs),
                        (int)mTweenFunction.easeInOut(elapsedFrameMs, Color.blue(mColorStart), mColorChange[3], mDurationMs)
                    );
                    break;
            }

            return mTweenBuffer;
        }
    }

    // instance variables
    private Vector<ColorKeyFrame> mKeyFrames;
    private float mCurrentFrameMs;       // the current keyframe ms count
    private int   mCurrentKeyFrameIndex; // which keyframe are we using?
    public KeyframedParticleInstanceColorModifier() {
        mKeyFrames = new Vector<>();
        mCurrentFrameMs = 0;
        mCurrentKeyFrameIndex = 0;
    }

    // assumes this is the first thing you call on it when creating keyframes
    public KeyframedParticleInstanceColorModifier start(int startColor, int endColor, float d, ITweenFunction f, TweenMode mode) {
        // add the first position
        mKeyFrames.add(new ColorKeyFrame()
                           .withStartColor(startColor).withEndColor(endColor)
                           .withDurationMs(d)
                           .withTween(f, mode));
        // set it
        return this;
    }

    // used to add a keyframe onto the timeline. assumes begin() has been called exactly once first.
    public KeyframedParticleInstanceColorModifier animate(int nextColor, float d, ITweenFunction f, TweenMode mode) {
        ColorKeyFrame c = mKeyFrames.lastElement();

        // create a new keyframe based on the previous frame's position and change
        mKeyFrames.add(new ColorKeyFrame()
                           .withStartColor(c.mColorEnd).withEndColor(nextColor)
                           .withDurationMs(d)
                           .withTween(f, mode)
        );
        return this;
    }

    @Override
    public void update(ParticleInstance particle, long msDelta) {
        if (mCurrentKeyFrameIndex > (mKeyFrames.size() - 1)) {
            return; // we are done here buddy
        }

        // calculate the current frame ms
        mCurrentFrameMs += msDelta;

        // grab the current keyframe
        ColorKeyFrame keyframe = mKeyFrames.get(mCurrentKeyFrameIndex);

        // determine if this keyframe has expired, and increment the keyframe index
        // and re-calculate the relative keyframe elapsed time
        if (mCurrentFrameMs >= keyframe.mDurationMs) {
            mCurrentKeyFrameIndex++;
            mCurrentFrameMs = mCurrentFrameMs - keyframe.mDurationMs;

            // if the key frame index is out of range, the particle timeline is over and dead
            if (mCurrentKeyFrameIndex > (mKeyFrames.size() - 1)) {
                return; // we are done here buddy
            } else {
                keyframe = mKeyFrames.get(mCurrentKeyFrameIndex);
            }
        }

        // using the current keyframe, set the particle color
        int c = keyframe.getTweenColor(mCurrentFrameMs);
        Log.d("KeyframeParticleColor", "(" + mCurrentKeyFrameIndex + ":" + mCurrentFrameMs + ") start: " + GLHelper.getColorString(keyframe.mColorStart) + "end: " +
                                           GLHelper.getColorString(keyframe.mColorEnd) + " tween: " + GLHelper.getColorString(c) + "change[" + keyframe.mColorChange[0] + "," + keyframe.mColorChange[1] + "," + keyframe.mColorChange[2] + "," + keyframe.mColorChange[3] +"]" );
        particle.setColor(Color.alpha(c) / 255.0f, Color.red(c) / 255.0f, Color.green(c) / 255.0f, Color.blue(c) / 255.0f);
    }

    @Override
    public void reset(ParticleInstance particle) {
        mCurrentFrameMs = 0;
        mCurrentKeyFrameIndex = 0;
    }
}

