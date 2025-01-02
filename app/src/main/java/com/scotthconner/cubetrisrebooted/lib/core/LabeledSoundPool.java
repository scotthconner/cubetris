package com.scotthconner.cubetrisrebooted.lib.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;

/**
 * Singleton class used to manage audio resources. Wraps a SoundPool and provides string-based
 * access instead of manually keeping track of each loaded sound within the gamestate. also allows
 * decoupling between loading resources and playing them within sub-experiences.
 *
 * Created by scottc on 2/11/16.
 */
public class LabeledSoundPool {
    // constants
    private static int MAX_STREAM_COUNT = 10;
    private static int DEFAULT_PRIORITY = 1;

    // singleton instance
    private static LabeledSoundPool mPoolInstance = null;

    // instance state
    private Context                  mApplicationContext = null;
    private SoundPool                mSoundPool          = null;
    private HashMap<String, Integer> mSoundIds           = null;
    private HashMap<String, MediaPlayer> mSongs          = null;
    private MediaPlayer mActiveSong = null;

    public static LabeledSoundPool getInstance() {
        if (null == mPoolInstance) {
            mPoolInstance = new LabeledSoundPool();
        }

        return mPoolInstance;
    }

    private LabeledSoundPool() {
        mSoundPool = new SoundPool(MAX_STREAM_COUNT, AudioManager.STREAM_MUSIC, 0);
        mSoundIds = new HashMap<>();
        mSongs = new HashMap<>();
        mActiveSong = null;
    }

    public void setApplicationContext(Context cxt) {
        mApplicationContext = cxt;
    }

    public boolean loadSound(String label, int resourceId) {
        mSoundIds.put(label, mSoundPool.load(mApplicationContext, resourceId, DEFAULT_PRIORITY));
        return true;
    }

    public void playSound(String label, float volume) {
        Log.d("LabeledSoundPool", "playing sound " + label);
        mSoundPool.play(mSoundIds.get(label).intValue(), volume, volume, DEFAULT_PRIORITY, 0, 1.0f);
    }

    public void release() {
        mSoundPool.release();
        mSoundIds.clear();
        for(MediaPlayer song : mSongs.values()) {
            if (song.isPlaying()) {
                song.stop();
            }
            song.release();
        }
    }

    public void loadMusic(String label, int res, float volume) {
        mSongs.put(label, MediaPlayer.create(mApplicationContext, res));
        mSongs.get(label).setVolume(volume, volume);
    }

    public void startMusic(String label, boolean restart, boolean loop) {
        if (null != mActiveSong && mActiveSong.isPlaying()) {
            mActiveSong.stop();
        }
        mActiveSong = mSongs.get(label);
        if(restart) {
            mActiveSong.reset();
        }
        mActiveSong.start();
        mActiveSong.setLooping(loop);
    }

    public void unloadMusic(String label) {
        mSongs.get(label).release();
    }
}
