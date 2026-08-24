package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.DeviceFolder;
import com.amitbharat.songsplayer.data.model.Song;

import java.util.ArrayList;
import java.util.List;

public class DeviceFolderAdapter extends RecyclerView.Adapter<DeviceFolderAdapter.FolderViewHolder> {

    public interface OnFolderSongClickListener {
        void onSongClick(Song song, List<Song> folderSongs, int position);
        void onSongFavorite(Song song);
        void onSongMore(Song song, View anchor);
    }

    private final List<DeviceFolder> folderList = new ArrayList<>();
    private final OnFolderSongClickListener listener;

    public DeviceFolderAdapter(OnFolderSongClickListener listener) {
        this.listener = listener;
    }

    public void setFolders(List<DeviceFolder> folders) {
        folderList.clear();
        if (folders != null) {
            folderList.addAll(folders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        DeviceFolder folder = folderList.get(position);
        holder.bind(folder, listener);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutHeader;
        private final TextView tvFolderName;
        private final TextView tvFolderCount;
        private final ImageView ivArrow;
        private final RecyclerView rvSongs;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layout_folder_header);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            tvFolderCount = itemView.findViewById(R.id.tv_folder_count);
            ivArrow = itemView.findViewById(R.id.iv_folder_expand_arrow);
            rvSongs = itemView.findViewById(R.id.rv_folder_songs);
        }

        public void bind(DeviceFolder folder, OnFolderSongClickListener listener) {
            Context context = itemView.getContext();
            tvFolderName.setText(folder.getFolderName());
            tvFolderCount.setText(String.format("%d files • %s", folder.getSongCount(), folder.getFolderPath()));

            rvSongs.setLayoutManager(new LinearLayoutManager(context));
            SongAdapter childAdapter = new SongAdapter(new SongAdapter.OnSongClickListener() {
                @Override
                public void onSongClick(Song song, int position) {
                    if (listener != null) {
                        listener.onSongClick(song, folder.getSongs(), position);
                    }
                }

                @Override
                public void onFavoriteClick(Song song, int position) {
                    if (listener != null) {
                        listener.onSongFavorite(song);
                    }
                }

                @Override
                public void onMoreClick(Song song, View anchorView, int position) {
                    if (listener != null) {
                        listener.onSongMore(song, anchorView);
                    }
                }
            });

            rvSongs.setAdapter(childAdapter);
            childAdapter.submitList(new ArrayList<>(folder.getSongs()));

            // Handle expansion state
            rvSongs.setVisibility(folder.isExpanded() ? View.VISIBLE : View.GONE);
            ivArrow.setRotation(folder.isExpanded() ? 180f : 0f);

            layoutHeader.setOnClickListener(v -> {
                folder.setExpanded(!folder.isExpanded());
                rvSongs.setVisibility(folder.isExpanded() ? View.VISIBLE : View.GONE);
                ivArrow.animate().rotation(folder.isExpanded() ? 180f : 0f).setDuration(200).start();
            });
        }
    }
}
