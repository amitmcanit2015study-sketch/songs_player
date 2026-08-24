package com.amitbharat.songsplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlaylistEntity playlist);
        void onPlaylistMoreClick(PlaylistEntity playlist, View anchorView);
    }

    private final List<PlaylistEntity> playlistList = new ArrayList<>();
    private final OnPlaylistClickListener listener;

    public PlaylistAdapter(OnPlaylistClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<PlaylistEntity> playlists) {
        playlistList.clear();
        if (playlists != null) {
            playlistList.addAll(playlists);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistEntity playlist = playlistList.get(position);
        holder.bind(playlist, listener);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvInfo;
        private final ImageButton btnMore;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_playlist_name);
            tvInfo = itemView.findViewById(R.id.tv_playlist_info);
            btnMore = itemView.findViewById(R.id.btn_playlist_more);
        }

        public void bind(PlaylistEntity playlist, OnPlaylistClickListener listener) {
            tvName.setText(playlist.name);
            tvInfo.setText("Custom Playlist");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPlaylistClick(playlist);
            });

            btnMore.setOnClickListener(v -> {
                if (listener != null) listener.onPlaylistMoreClick(playlist, v);
            });
        }
    }
}
