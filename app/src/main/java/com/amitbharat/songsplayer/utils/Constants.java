package com.amitbharat.songsplayer.utils;

public final class Constants {

    private Constants() {}

    // Service & Action Constants
    public static final String ACTION_PLAY = "com.amitbharat.songsplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.amitbharat.songsplayer.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.amitbharat.songsplayer.ACTION_NEXT";
    public static final String ACTION_PREV = "com.amitbharat.songsplayer.ACTION_PREV";
    public static final String ACTION_TOGGLE_FAVORITE = "com.amitbharat.songsplayer.ACTION_TOGGLE_FAVORITE";

    // Extras
    public static final String EXTRA_SONG = "extra_song";
    public static final String EXTRA_SONG_LIST = "extra_song_list";
    public static final String EXTRA_SONG_POSITION = "extra_song_position";
    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "extra_playlist_name";

    // SharedPreferences Keys
    public static final String PREF_NAME = "songs_player_prefs";
    public static final String KEY_REPEAT_MODE = "key_repeat_mode";
    public static final String KEY_SHUFFLE_MODE = "key_shuffle_mode";
    public static final String KEY_PLAYBACK_SPEED = "key_playback_speed";
    public static final String KEY_DYNAMIC_COLORS = "key_dynamic_colors";
    public static final String KEY_AUDIO_QUALITY = "key_audio_quality";
    public static final String KEY_CROSSFADE = "key_crossfade";
    public static final String KEY_GAPLESS = "key_gapless";
    public static final String KEY_LAST_PLAYED_SONG_ID = "key_last_song_id";
    public static final String KEY_LAST_PLAYED_POSITION = "key_last_position";

    // Equalizer Prefs
    public static final String KEY_EQ_ENABLED = "key_eq_enabled";
    public static final String KEY_EQ_PRESET = "key_eq_preset";
    public static final String KEY_EQ_BASS_BOOST = "key_eq_bass_boost";
    public static final String KEY_EQ_VIRTUALIZER = "key_eq_virtualizer";
    public static final String KEY_EQ_LOUDNESS = "key_eq_loudness";

    // Database
    public static final String DATABASE_NAME = "songs_player_database";
}
