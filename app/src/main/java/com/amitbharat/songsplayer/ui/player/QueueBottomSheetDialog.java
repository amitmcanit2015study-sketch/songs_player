package com.amitbharat.songsplayer.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.BottomSheetQueueBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.amitbharat.songsplayer.ui.adapter.QueueAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class QueueBottomSheetDialog extends BottomSheetDialogFragment implements QueueAdapter.OnQueueItemListener {

    private BottomSheetQueueBinding binding;
    private final PlaybackService playbackService;
    private QueueAdapter queueAdapter;

    public QueueBottomSheetDialog(PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queueAdapter = new QueueAdapter(this);
        binding.rvQueue.setAdapter(queueAdapter);

        PlaybackService.getQueueLive().observe(getViewLifecycleOwner(), queue -> {
            Integer index = PlaybackService.getCurrentQueueIndexLive().getValue();
            queueAdapter.setQueue(queue, index != null ? index : -1);
            binding.tvQueueHeader.setText(String.format("Now Playing (%d)", queue != null ? queue.size() : 0));
        });

        PlaybackService.getCurrentQueueIndexLive().observe(getViewLifecycleOwner(), index -> {
            List<Song> queue = PlaybackService.getQueueLive().getValue();
            queueAdapter.setQueue(queue, index != null ? index : -1);
        });

        binding.btnClearQueue.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.clearQueue();
                dismiss();
            }
        });
    }

    @Override
    public void onQueueItemClick(Song song, int position) {
        if (playbackService != null) {
            List<Song> queue = PlaybackService.getQueueLive().getValue();
            if (queue != null) {
                playbackService.playSongList(queue, position);
            }
        }
    }

    @Override
    public void onQueueItemRemove(int position) {
        if (playbackService != null) {
            playbackService.removeFromQueue(position);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
