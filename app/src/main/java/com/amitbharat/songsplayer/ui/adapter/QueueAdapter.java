package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.utils.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnQueueItemListener {
        void onQueueItemClick(Song song, int position);
        void onQueueItemRemove(int position);
    }

    private final List<Song> queueList = new ArrayList<>();
    private final OnQueueItemListener listener;
    private int currentPlayingIndex = -1;

    public QueueAdapter(OnQueueItemListener listener) {
        this.listener = listener;
    }

    public void setQueue(List<Song> songs, int playingIndex) {
        queueList.clear();
        if (songs != null) {
            queueList.addAll(songs);
        }
        this.currentPlayingIndex = playingIndex;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Song song = queueList.get(position);
        boolean isCurrent = (position == currentPlayingIndex);
        holder.bind(song, isCurrent, listener, position);
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final ImageButton btnRemove;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_queue_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_queue_title);
            tvArtist = itemView.findViewById(R.id.tv_queue_artist);
            btnRemove = itemView.findViewById(R.id.btn_queue_remove);
        }

        public void bind(Song song, boolean isCurrent, OnQueueItemListener listener, int position) {
            Context context = itemView.getContext();
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            ImageLoader.loadAlbumArt(context, song.getAlbumId(), song.getArtUrl(), ivThumbnail);

            if (isCurrent) {
                tvTitle.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                tvTitle.setTextColor(context.getResources().getColor(R.color.on_surface_dark));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onQueueItemClick(song, position);
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onQueueItemRemove(position);
            });
        }
    }
}
