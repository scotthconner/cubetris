package com.scotthconner.cubetrisrebooted.lib.object.particle.modifier;

import com.scotthconner.cubetrisrebooted.lib.core.Vertex;
import com.scotthconner.cubetrisrebooted.lib.core.tween.ITweenFunction;
import com.scotthconner.cubetrisrebooted.lib.core.tween.TweenMode;
import com.scotthconner.cubetrisrebooted.lib.object.particle.ParticleInstance;

import java.util.Vector;

/**
 * Interpolates the particle based on its keyframes, and tween mode. Will expire the particle
 * at the end of the time keyframe timeline
 *
 * Created by scottc on 4/21/16.
 */
public class KeyframedParticleInstancePositionModifier implements IParticleModifier {
    public class PositionKeyFrame {
        public Vertex mPositionStart;        // the position of this key-frame
        public Vertex mPositionChange;          // the end position of this key frame
        private Vertex mTweenBuffer;         // used as a buffer for calculations
        public float mDurationMs;      // the duration of the tween in milliseconds
        public ITweenFunction mTweenFunction; // the tween function into this position
        public TweenMode mTweenMode;          // ease in, out, or both

        public PositionKeyFrame() {
            mPositionStart = new Vertex();
            mPositionChange = new Vertex();
            mTweenBuffer = new Vertex();
            mTweenMode = null;
            mTweenFunction = null;
            mDurationMs = 0;
        }

        public PositionKeyFrame withStartPosition(float x, float y, float z) {
            if (mPositionStart == null) {
                mPositionStart = new Vertex(x, y, z);
            } else {
                mPositionStart.set(x, y, z);
            }

            return this;
        }

        public PositionKeyFrame withTweenChange(float x, float y, float z) {
            if (mPositionChange == null) {
                mPositionChange = new Vertex(x, y, z);
            } else {
                mPositionChange.set(x, y, z);
            }

            return this;
        }

        public PositionKeyFrame withTween(ITweenFunction f, TweenMode mode) {
            mTweenMode = mode;
            mTweenFunction = f;
            return this;
        }

        public PositionKeyFrame withDurationMs(float d) {
            mDurationMs = d;
            return this;
        }

        public Vertex getTweenPosition(float elapsedFrameMs) {
            switch(mTweenMode) {
                case EASE_IN:
                    mTweenBuffer.x = mTweenFunction.easeIn(elapsedFrameMs, mPositionStart.x, mPositionChange.x, mDurationMs);
                    mTweenBuffer.y = mTweenFunction.easeIn(elapsedFrameMs, mPositionStart.y, mPositionChange.y, mDurationMs);
                    mTweenBuffer.z = mTweenFunction.easeIn(elapsedFrameMs, mPositionStart.z, mPositionChange.z, mDurationMs);
                    break;
                case EASE_OUT:
                    mTweenBuffer.x = mTweenFunction.easeOut(elapsedFrameMs, mPositionStart.x, mPositionChange.x, mDurationMs);
                    mTweenBuffer.y = mTweenFunction.easeOut(elapsedFrameMs, mPositionStart.y, mPositionChange.y, mDurationMs);
                    mTweenBuffer.z = mTweenFunction.easeOut(elapsedFrameMs, mPositionStart.z, mPositionChange.z, mDurationMs);
                    break;
                case EASE_IN_OUT:
                    mTweenBuffer.x = mTweenFunction.easeInOut(elapsedFrameMs, mPositionStart.x, mPositionChange.x, mDurationMs);
                    mTweenBuffer.y = mTweenFunction.easeInOut(elapsedFrameMs, mPositionStart.y, mPositionChange.y, mDurationMs);
                    mTweenBuffer.z = mTweenFunction.easeInOut(elapsedFrameMs, mPositionStart.z, mPositionChange.z, mDurationMs);
                    break;
            }

            return mTweenBuffer;
        }
    }

    // instance variables
    private Vector<PositionKeyFrame> mKeyFrames;
    private float mCurrentFrameMs;       // the current keyframe ms count
    private int   mCurrentKeyFrameIndex; // which keyframe are we using?
    public KeyframedParticleInstancePositionModifier() {
        mKeyFrames = new Vector<>();
        mCurrentFrameMs = 0;
        mCurrentKeyFrameIndex = 0;
    }

    // assumes this is the first thing you call on it when creating keyframes
    public KeyframedParticleInstancePositionModifier start(float x, float y, float z, float cx, float cy, float cz, float d, ITweenFunction f, TweenMode mode) {
        // add the first position
        mKeyFrames.add(new PositionKeyFrame()
                           .withStartPosition(x, y, z)
                           .withDurationMs(d)
                           .withTween(f, mode)
                           .withTweenChange(cx, cy, cz));

        // set it
        return this;
    }

    // used to add a keyframe onto the timeline. assumes begin() has been called exactly once first.
    public KeyframedParticleInstancePositionModifier animate(float cx, float cy, float cz, float d, ITweenFunction f, TweenMode mode) {
        PositionKeyFrame c = mKeyFrames.lastElement();

        // create a new keyframe based on the previous frame's position and change
        mKeyFrames.add(new PositionKeyFrame()
                           .withStartPosition(c.mPositionStart.x + c.mPositionChange.x,
                                                 c.mPositionStart.y + c.mPositionChange.y,
                                                 c.mPositionStart.z + c.mPositionChange.z)
                           .withDurationMs(d)
                           .withTween(f, mode)
                           .withTweenChange(cx, cy, cz));

        return this;
    }

    @Override
    public void update(ParticleInstance particle, long msDelta) {
        // calculate the current frame ms
        mCurrentFrameMs += msDelta;

        // grab the current keyframe
        PositionKeyFrame keyframe = mKeyFrames.get(mCurrentKeyFrameIndex);

        // determine if this keyframe has expired, and increment the keyframe index
        // and re-calculate the relative keyframe elapsed time
        if (mCurrentFrameMs >= keyframe.mDurationMs) {
            mCurrentKeyFrameIndex++;
            mCurrentFrameMs = mCurrentFrameMs - keyframe.mDurationMs;

            // if the key frame index is out of range, the particle timeline is over and dead
            if (mCurrentKeyFrameIndex > (mKeyFrames.size() - 1)) {
                particle.mLife = 0;
                return; // we are done here buddy
            } else {
                keyframe = mKeyFrames.get(mCurrentKeyFrameIndex);
            }
        }

        // using the current keyframe, set the particle position
        Vertex tp = keyframe.getTweenPosition(mCurrentFrameMs);
        particle.setPosition(tp.x, tp.y, tp.z);
    }

    @Override
    public void reset(ParticleInstance particle) {
        mCurrentFrameMs = 0;
        mCurrentKeyFrameIndex = 0;
    }
}
