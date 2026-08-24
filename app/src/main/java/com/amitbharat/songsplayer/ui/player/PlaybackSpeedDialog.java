package com.amitbharat.songsplayer.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.databinding.DialogPlaybackSpeedBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PlaybackSpeedDialog extends BottomSheetDialogFragment {

    private DialogPlaybackSpeedBinding binding;
    private final PlaybackService playbackService;

    public PlaybackSpeedDialog(PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPlaybackSpeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (playbackService == null) return;

        Float currentSpeed = PlaybackService.getPlaybackSpeedLive().getValue();
        if (currentSpeed != null) {
            if (currentSpeed == 0.5f) binding.rbSpeed05.setChecked(true);
            else if (currentSpeed == 0.75f) binding.rbSpeed075.setChecked(true);
            else if (currentSpeed == 1.0f) binding.rbSpeed10.setChecked(true);
            else if (currentSpeed == 1.25f) binding.rbSpeed125.setChecked(true);
            else if (currentSpeed == 1.5f) binding.rbSpeed15.setChecked(true);
            else if (currentSpeed == 2.0f) binding.rbSpeed20.setChecked(true);
        }

        binding.rgSpeed.setOnCheckedChangeListener((group, checkedId) -> {
            float newSpeed = 1.0f;
            if (checkedId == R.id.rb_speed_05) newSpeed = 0.5f;
            else if (checkedId == R.id.rb_speed_075) newSpeed = 0.75f;
            else if (checkedId == R.id.rb_speed_10) newSpeed = 1.0f;
            else if (checkedId == R.id.rb_speed_125) newSpeed = 1.25f;
            else if (checkedId == R.id.rb_speed_15) newSpeed = 1.5f;
            else if (checkedId == R.id.rb_speed_20) newSpeed = 2.0f;

            playbackService.setPlaybackSpeed(newSpeed);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
