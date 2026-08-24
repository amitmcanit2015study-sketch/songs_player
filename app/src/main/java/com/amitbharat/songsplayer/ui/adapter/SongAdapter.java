package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.utils.FormatUtils;
import com.amitbharat.songsplayer.utils.ImageLoader;

public class SongAdapter extends ListAdapter<Song, SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
        void onFavoriteClick(Song song, int position);
        void onMoreClick(Song song, View anchorView, int position);
    }

    private final OnSongClickListener listener;

    public SongAdapter(OnSongClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.isFavorite() == newItem.isFavorite()
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getArtist().equals(newItem.getArtist());
        }
    };

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);
        holder.bind(song, listener, position);
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumbnail;
        private final TextView tvDurationBadge;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final ImageButton ivFavorite;
        private final ImageButton btnMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_song_thumbnail);
            tvDurationBadge = itemView.findViewById(R.id.tv_duration_badge);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvSubtitle = itemView.findViewById(R.id.tv_song_subtitle);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            btnMore = itemView.findViewById(R.id.btn_song_more);
        }

        public void bind(Song song, OnSongClickListener listener, int position) {
            Context context = itemView.getContext();

            tvTitle.setText(song.getTitle());
            String duration = FormatUtils.formatDuration(song.getDuration());
            
            if (tvDurationBadge != null) {
                if (song.getDuration() > 0) {
                    tvDurationBadge.setText(duration);
                    tvDurationBadge.setVisibility(View.VISIBLE);
                } else {
                    tvDurationBadge.setVisibility(View.GONE);
                }
            }

            String views = song.getPlayCount() > 0 ? (song.getPlayCount() >= 1000 ? (song.getPlayCount() / 1000) + "K views" : song.getPlayCount() + " views") : "Official Stream";
            tvSubtitle.setText(String.format("%s • %s", song.getArtist(), views));

            // Load album art
            ImageLoader.loadAlbumArt(context, song.getAlbumId(), song.getArtUrl(), ivThumbnail);

            // Favorite Icon
            if (song.isFavorite()) {
                ivFavorite.setImageResource(R.drawable.ic_favorite);
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSongClick(song, position);
            });

            ivFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(song, position);
            });

            btnMore.setOnClickListener(v -> {
                if (listener != null) listener.onMoreClick(song, v, position);
            });
        }
    }
}
