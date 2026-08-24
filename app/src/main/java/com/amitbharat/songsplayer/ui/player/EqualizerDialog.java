package com.amitbharat.songsplayer.ui.player;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.EqualizerPreset;
import com.amitbharat.songsplayer.databinding.DialogEqualizerBinding;
import com.amitbharat.songsplayer.service.AudioEffectsManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class EqualizerDialog extends BottomSheetDialogFragment {

    private DialogEqualizerBinding binding;
    private final AudioEffectsManager effectsManager;

    public EqualizerDialog(AudioEffectsManager effectsManager) {
        this.effectsManager = effectsManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEqualizerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (effectsManager == null) return;

        binding.switchEqualizer.setChecked(effectsManager.isEnabled());
        binding.switchEqualizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            effectsManager.setEnabled(isChecked);
        });

        setupPresets();
        setupBands();
        setupAudioEffectSliders();
    }

    private void setupPresets() {
        List<EqualizerPreset> presets = effectsManager.getPresets();
        List<String> names = new ArrayList<>();
        for (EqualizerPreset p : presets) {
            names.add(p.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        binding.spinnerPresets.setAdapter(adapter);

        binding.spinnerPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                effectsManager.usePreset((short) position);
                setupBands();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupBands() {
        binding.layoutEqualizerBands.removeAllViews();
        Equalizer eq = effectsManager.getEqualizer();
        if (eq == null) return;

        short numBands = eq.getNumberOfBands();
        short[] bandLevelRange = eq.getBandLevelRange(); // e.g. -1500 to 1500 mB
        short minLevel = bandLevelRange[0];
        short maxLevel = bandLevelRange[1];

        for (short i = 0; i < numBands; i++) {
            final short bandIndex = i;
            int centerFreqHz = eq.getCenterFreq(i) / 1000;
            short currentLevel = eq.getBandLevel(i);

            LinearLayout bandRow = new LinearLayout(requireContext());
            bandRow.setOrientation(LinearLayout.HORIZONTAL);
            bandRow.setPadding(0, 8, 0, 8);

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setLayoutParams(new LinearLayout.LayoutParams(140, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvLabel.setText(centerFreqHz >= 1000 ? (centerFreqHz / 1000) + " kHz" : centerFreqHz + " Hz");
            tvLabel.setTextColor(getResources().getColor(R.color.on_surface_dark));

            SeekBar sbBand = new SeekBar(requireContext());
            sbBand.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            sbBand.setMax(maxLevel - minLevel);
            sbBand.setProgress(currentLevel - minLevel);
            sbBand.setProgressTintList(getResources().getColorStateList(R.color.primary));
            sbBand.setThumbTintList(getResources().getColorStateList(R.color.primary));

            sbBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        short newLevel = (short) (progress + minLevel);
                        effectsManager.setBandLevel(bandIndex, newLevel);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            bandRow.addView(tvLabel);
            bandRow.addView(sbBand);
            binding.layoutEqualizerBands.addView(bandRow);
        }
    }

    private void setupAudioEffectSliders() {
        // Bass Boost
        binding.sliderBassBoost.setValue(effectsManager.getBassBoostStrength());
        binding.sliderBassBoost.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                effectsManager.setBassBoostStrength((short) value);
            }
        });

        // Virtualizer
        binding.sliderVirtualizer.setValue(effectsManager.getVirtualizerStrength());
        binding.sliderVirtualizer.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                effectsManager.setVirtualizerStrength((short) value);
            }
        });

        // Loudness Enhancer
        binding.sliderLoudness.setValue(effectsManager.getLoudnessGain());
        binding.sliderLoudness.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                effectsManager.setLoudnessGain((int) value);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
