package com.amitbharat.songsplayer.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.databinding.ActivitySettingsBinding;
import com.amitbharat.songsplayer.utils.Constants;
import com.amitbharat.songsplayer.utils.ImageLoader;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        binding.toolbarSettings.setNavigationOnClickListener(v -> finish());

        setupPreferences();
    }

    private void setupPreferences() {
        // Dynamic Colors Switch
        boolean dynamicColors = prefs.getBoolean(Constants.KEY_DYNAMIC_COLORS, true);
        binding.switchDynamicColors.setChecked(dynamicColors);
        binding.switchDynamicColors.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(Constants.KEY_DYNAMIC_COLORS, isChecked).apply();
            Toast.makeText(this, "Restart app to apply theme changes completely", Toast.LENGTH_SHORT).show();
        });

        // Gapless Playback Switch
        boolean gapless = prefs.getBoolean(Constants.KEY_GAPLESS, true);
        binding.switchGapless.setChecked(gapless);
        binding.switchGapless.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(Constants.KEY_GAPLESS, isChecked).apply();
        });

        // Audio Quality Picker
        String currentQuality = prefs.getString(Constants.KEY_AUDIO_QUALITY, "High (320 kbps)");
        binding.tvCurrentQuality.setText(currentQuality);
        binding.rowAudioQuality.setOnClickListener(v -> {
            String[] options = {"High (320 kbps)", "Medium (192 kbps)", "Low (128 kbps)"};
            new AlertDialog.Builder(this)
                    .setTitle(R.string.pref_audio_quality)
                    .setItems(options, (dialog, which) -> {
                        String selected = options[which];
                        prefs.edit().putString(Constants.KEY_AUDIO_QUALITY, selected).apply();
                        binding.tvCurrentQuality.setText(selected);
                    })
                    .show();
        });

        // Clear Cache
        binding.rowClearCache.setOnClickListener(v -> {
            ImageLoader.clearCache(this);
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
        });
    }
}
