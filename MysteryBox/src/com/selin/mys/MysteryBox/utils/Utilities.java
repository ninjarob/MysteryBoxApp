package com.selin.mys.MysteryBox.utils;

import com.selin.mys.MysteryBox.game.Settings;
import org.andengine.audio.music.Music;
import org.andengine.audio.sound.Sound;

/**
 * Created with IntelliJ IDEA.
 * User: rkevan
 * Date: 6/6/13
 * Time: 8:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class Utilities {

    public static void playMusic(Music mMusic) {
        if (Settings.musicEnabled && mMusic != null && !mMusic.isPlaying()) {
            mMusic.play();
        }
    }

    public static void playSound(Sound s) {
        if (Settings.soundEnabled && s != null) {
            s.play();
        }
    }
}
