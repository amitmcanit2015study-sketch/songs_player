package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.utils.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    public interface OnArtistClickListener {
        void onArtistClick(Artist artist);
    }

    private final List<Artist> artistList = new ArrayList<>();
    private final OnArtistClickListener listener;

    public ArtistAdapter(OnArtistClickListener listener) {
        this.listener = listener;
    }

    public void setArtists(List<Artist> artists) {
        artistList.clear();
        if (artists != null) {
            artistList.addAll(artists);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.bind(artist, listener);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivImage;
        private final TextView tvName;
        private final TextView tvTracksCount;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_artist_image);
            tvName = itemView.findViewById(R.id.tv_artist_name);
            tvTracksCount = itemView.findViewById(R.id.tv_artist_tracks_count);
        }

        public void bind(Artist artist, OnArtistClickListener listener) {
            Context context = itemView.getContext();
            tvName.setText(artist.getName());
            
            if (artist.getGenre() != null && !artist.getGenre().isEmpty() && !artist.getGenre().equals("Artist")) {
                tvTracksCount.setText(String.format("%s • %d songs", artist.getGenre(), artist.getTrackCount()));
            } else if (artist.getTrackCount() > 0) {
                tvTracksCount.setText(String.format("%d songs", artist.getTrackCount()));
            } else {
                tvTracksCount.setText("Popular Artist");
            }

            ImageLoader.loadAlbumArt(context, 0, artist.getImageUrl(), ivImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onArtistClick(artist);
            });
        }
    }
}
