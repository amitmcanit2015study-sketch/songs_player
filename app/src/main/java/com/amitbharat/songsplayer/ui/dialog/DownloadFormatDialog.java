package com.amitbharat.songsplayer.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.DialogDownloadFormatBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DownloadFormatDialog extends BottomSheetDialogFragment {

    public interface OnFormatSelectedListener {
        void onFormatSelected(Song song, boolean isVideo);
    }

    private final Song song;
    private final OnFormatSelectedListener listener;
    private DialogDownloadFormatBinding binding;

    public DownloadFormatDialog(Song song, OnFormatSelectedListener listener) {
        this.song = song;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDownloadFormatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (song != null) {
            binding.tvDialogSongTitle.setText(song.getTitle());
        }

        binding.cardDownloadMp3.setOnClickListener(v -> {
            if (listener != null && song != null) {
                listener.onFormatSelected(song, false);
            }
            dismiss();
        });

        binding.cardDownloadMp4.setOnClickListener(v -> {
            if (listener != null && song != null) {
                listener.onFormatSelected(song, true);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
