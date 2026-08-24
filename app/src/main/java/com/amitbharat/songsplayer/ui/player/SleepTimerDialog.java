package com.amitbharat.songsplayer.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.databinding.DialogSleepTimerBinding;
import com.amitbharat.songsplayer.service.SleepTimerManager;
import com.amitbharat.songsplayer.utils.FormatUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SleepTimerDialog extends BottomSheetDialogFragment {

    private DialogSleepTimerBinding binding;
    private int selectedMinutes = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSleepTimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SleepTimerManager timerManager = SleepTimerManager.getInstance();

        timerManager.getIsTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            if (isRunning != null && isRunning) {
                binding.btnTimerStop.setVisibility(View.VISIBLE);
            } else {
                binding.btnTimerStop.setVisibility(View.GONE);
            }
        });

        timerManager.getRemainingMillis().observe(getViewLifecycleOwner(), remaining -> {
            if (remaining != null && remaining > 0) {
                binding.tvTimerStatus.setText(String.format("Timer active: %s remaining", FormatUtils.formatDuration(remaining)));
            } else {
                binding.tvTimerStatus.setText("Turn off playback automatically after timer expires");
            }
        });

        binding.chipGroupTimer.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_15m)) selectedMinutes = 15;
            else if (checkedIds.contains(R.id.chip_30m)) selectedMinutes = 30;
            else if (checkedIds.contains(R.id.chip_45m)) selectedMinutes = 45;
            else if (checkedIds.contains(R.id.chip_60m)) selectedMinutes = 60;
        });

        binding.btnTimerCancel.setOnClickListener(v -> dismiss());

        binding.btnTimerStop.setOnClickListener(v -> {
            timerManager.cancelTimer();
            Toast.makeText(requireContext(), R.string.sleep_timer_cancelled, Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.btnTimerSet.setOnClickListener(v -> {
            String custom = binding.etCustomMinutes.getText() != null ? binding.etCustomMinutes.getText().toString().trim() : "";
            if (!custom.isEmpty()) {
                try {
                    selectedMinutes = Integer.parseInt(custom);
                } catch (Exception ignored) {}
            }

            if (selectedMinutes > 0) {
                timerManager.startTimer(selectedMinutes);
                Toast.makeText(requireContext(), getString(R.string.sleep_timer_set, selectedMinutes), Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Please select or enter minutes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
