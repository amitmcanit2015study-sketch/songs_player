package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.utils.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    private final List<Album> albumList = new ArrayList<>();
    private final OnAlbumClickListener listener;

    public AlbumAdapter(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    public void setAlbums(List<Album> albums) {
        albumList.clear();
        if (albums != null) {
            albumList.addAll(albums);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        holder.bind(album, listener);
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivArt;
        private final TextView tvTitle;
        private final TextView tvArtist;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.iv_album_art);
            tvTitle = itemView.findViewById(R.id.tv_album_title);
            tvArtist = itemView.findViewById(R.id.tv_album_artist);
        }

        public void bind(Album album, OnAlbumClickListener listener) {
            Context context = itemView.getContext();
            tvTitle.setText(album.getTitle());
            tvArtist.setText(album.getArtist());
            ImageLoader.loadAlbumArt(context, album.getId(), album.getArtUrl(), ivArt);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAlbumClick(album);
            });
        }
    }
}
