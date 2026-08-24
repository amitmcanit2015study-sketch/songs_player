package com.amitbharat.songsplayer.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.os.Build;

import com.amitbharat.songsplayer.data.model.EqualizerPreset;
import com.amitbharat.songsplayer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AudioEffectsManager {

    private final Context context;
    private final SharedPreferences prefs;

    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private LoudnessEnhancer loudnessEnhancer;

    private int audioSessionId = 0;
    private boolean isEnabled = true;

    public AudioEffectsManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        this.isEnabled = prefs.getBoolean(Constants.KEY_EQ_ENABLED, true);
    }

    /**
     * Attaches audio effects to a new audio session ID from ExoPlayer
     */
    public synchronized void attachAudioSession(int sessionId) {
        if (sessionId == 0) return;
        this.audioSessionId = sessionId;

        release();

        try {
            // Initialize Equalizer
            equalizer = new Equalizer(0, sessionId);
            equalizer.setEnabled(isEnabled);

            // Initialize BassBoost
            bassBoost = new BassBoost(0, sessionId);
            if (bassBoost.getStrengthSupported()) {
                short strength = (short) prefs.getInt(Constants.KEY_EQ_BASS_BOOST, 0);
                bassBoost.setStrength(strength);
                bassBoost.setEnabled(isEnabled);
            }

            // Initialize Virtualizer
            virtualizer = new Virtualizer(0, sessionId);
            if (virtualizer.getStrengthSupported()) {
                short strength = (short) prefs.getInt(Constants.KEY_EQ_VIRTUALIZER, 0);
                virtualizer.setStrength(strength);
                virtualizer.setEnabled(isEnabled);
            }

            // Initialize LoudnessEnhancer (API 19+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = new LoudnessEnhancer(sessionId);
                int gainMb = prefs.getInt(Constants.KEY_EQ_LOUDNESS, 0);
                loudnessEnhancer.setTargetGain(gainMb);
                loudnessEnhancer.setEnabled(isEnabled);
            }

            // Apply saved preset
            short savedPreset = (short) prefs.getInt(Constants.KEY_EQ_PRESET, 0);
            if (savedPreset >= 0 && savedPreset < equalizer.getNumberOfPresets()) {
                equalizer.usePreset(savedPreset);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        prefs.edit().putBoolean(Constants.KEY_EQ_ENABLED, enabled).apply();

        try {
            if (equalizer != null) equalizer.setEnabled(enabled);
            if (bassBoost != null) bassBoost.setEnabled(enabled);
            if (virtualizer != null) virtualizer.setEnabled(enabled);
            if (loudnessEnhancer != null) loudnessEnhancer.setEnabled(enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public List<EqualizerPreset> getPresets() {
        List<EqualizerPreset> list = new ArrayList<>();
        if (equalizer != null) {
            short numPresets = equalizer.getNumberOfPresets();
            for (short i = 0; i < numPresets; i++) {
                String name = equalizer.getPresetName(i);
                list.add(new EqualizerPreset(name, null, (short) 0, (short) 0));
            }
        }
        return list;
    }

    public void usePreset(short presetIndex) {
        if (equalizer != null && presetIndex >= 0 && presetIndex < equalizer.getNumberOfPresets()) {
            try {
                equalizer.usePreset(presetIndex);
                prefs.edit().putInt(Constants.KEY_EQ_PRESET, presetIndex).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setBandLevel(short band, short level) {
        if (equalizer != null) {
            try {
                equalizer.setBandLevel(band, level);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setBassBoostStrength(short strength) {
        if (bassBoost != null && bassBoost.getStrengthSupported()) {
            try {
                bassBoost.setStrength(strength);
                prefs.edit().putInt(Constants.KEY_EQ_BASS_BOOST, strength).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public short getBassBoostStrength() {
        if (bassBoost != null && bassBoost.getStrengthSupported()) {
            try {
                return bassBoost.getRoundedStrength();
            } catch (Exception ignored) {}
        }
        return (short) prefs.getInt(Constants.KEY_EQ_BASS_BOOST, 0);
    }

    public void setVirtualizerStrength(short strength) {
        if (virtualizer != null && virtualizer.getStrengthSupported()) {
            try {
                virtualizer.setStrength(strength);
                prefs.edit().putInt(Constants.KEY_EQ_VIRTUALIZER, strength).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public short getVirtualizerStrength() {
        if (virtualizer != null && virtualizer.getStrengthSupported()) {
            try {
                return virtualizer.getRoundedStrength();
            } catch (Exception ignored) {}
        }
        return (short) prefs.getInt(Constants.KEY_EQ_VIRTUALIZER, 0);
    }

    public void setLoudnessGain(int gainMb) {
        if (loudnessEnhancer != null) {
            try {
                loudnessEnhancer.setTargetGain(gainMb);
                prefs.edit().putInt(Constants.KEY_EQ_LOUDNESS, gainMb).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getLoudnessGain() {
        return prefs.getInt(Constants.KEY_EQ_LOUDNESS, 0);
    }

    public synchronized void release() {
        try {
            if (equalizer != null) {
                equalizer.release();
                equalizer = null;
            }
            if (bassBoost != null) {
                bassBoost.release();
                bassBoost = null;
            }
            if (virtualizer != null) {
                virtualizer.release();
                virtualizer = null;
            }
            if (loudnessEnhancer != null) {
                loudnessEnhancer.release();
                loudnessEnhancer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
