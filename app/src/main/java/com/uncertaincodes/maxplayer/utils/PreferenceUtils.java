package com.uncertaincodes.maxplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.media3.exoplayer.ExoPlayer;

public class PreferenceUtils {
    private static final String PREF_KEY_PLAYBACK_POSITION = "pp_";
    private static final String PREF_KEY_CURRENT_WINDOW_INDEX = "cwi_";
    private static final String PREF_KEY_PLAY_WHEN_READY = "pwr_";
    private static final String PREF_KEY_IS_STATUS_PERMISSION = "grant_sp";

    final Context context;
    final SharedPreferences sharedPreferences;

    public PreferenceUtils(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void savePlaybackState(ExoPlayer player, String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_KEY_PLAYBACK_POSITION + path, player.getCurrentPosition());
        editor.putInt(PREF_KEY_CURRENT_WINDOW_INDEX + path, player.getCurrentWindowIndex());
        editor.putBoolean(PREF_KEY_PLAY_WHEN_READY + path, player.getPlayWhenReady());
        editor.apply();
    }

    public long getPlayBackPosition(String path) {
        return sharedPreferences.getLong(PREF_KEY_PLAYBACK_POSITION + path, 0);
    }

    public int getCurrentWindow(String path) {
        return sharedPreferences.getInt(PREF_KEY_CURRENT_WINDOW_INDEX + path, 0);
    }

    public boolean isPlayWhenReady(String path) {
        return sharedPreferences.getBoolean(PREF_KEY_PLAY_WHEN_READY + path, true);
    }

}
