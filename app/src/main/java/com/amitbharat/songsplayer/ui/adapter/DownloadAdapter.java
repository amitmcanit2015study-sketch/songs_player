package com.amitbharat.songsplayer.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.DownloadItem;
import com.amitbharat.songsplayer.utils.FormatUtils;
import com.amitbharat.songsplayer.utils.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    public interface OnDownloadItemClickListener {
        void onDownloadClick(DownloadItem item);
        void onDownloadActionClick(DownloadItem item);
        void onSelectionChanged(int selectedCount);
    }

    private final List<DownloadItem> downloadList = new ArrayList<>();
    private final OnDownloadItemClickListener listener;
    private boolean isSelectionMode = false;

    public DownloadAdapter(OnDownloadItemClickListener listener) {
        this.listener = listener;
    }

    public void setDownloads(List<DownloadItem> items) {
        downloadList.clear();
        if (items != null) {
            downloadList.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean active) {
        this.isSelectionMode = active;
        if (!active) {
            for (DownloadItem item : downloadList) {
                item.setSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void selectAll(boolean selectAll) {
        for (DownloadItem item : downloadList) {
            item.setSelected(selectAll);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(getSelectedItems().size());
        }
    }

    public List<DownloadItem> getSelectedItems() {
        List<DownloadItem> selected = new ArrayList<>();
        for (DownloadItem item : downloadList) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        DownloadItem item = downloadList.get(position);
        holder.bind(item, isSelectionMode, listener);
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

    class DownloadViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbSelect;
        private final ShapeableImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final LinearProgressIndicator progressIndicator;
        private final ImageButton btnAction;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_download_select);
            ivThumbnail = itemView.findViewById(R.id.iv_download_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_download_title);
            tvSubtitle = itemView.findViewById(R.id.tv_download_subtitle);
            progressIndicator = itemView.findViewById(R.id.progress_download);
            btnAction = itemView.findViewById(R.id.btn_download_action);
        }

        public void bind(DownloadItem item, boolean selectionMode, OnDownloadItemClickListener listener) {
            Context context = itemView.getContext();
            tvTitle.setText(item.getTitle());

            String sizeStr = FormatUtils.formatFileSize(item.getTotalBytes());
            String statusStr = item.getStatus().name();
            tvSubtitle.setText(String.format("%s • %s • %s", item.getArtist(), sizeStr, statusStr));

            ImageLoader.loadAlbumArt(context, 0, item.getArtUrl(), ivThumbnail);

            // Selection CheckBox
            if (selectionMode) {
                cbSelect.setVisibility(View.VISIBLE);
                cbSelect.setChecked(item.isSelected());
                btnAction.setVisibility(View.GONE);
            } else {
                cbSelect.setVisibility(View.GONE);
                btnAction.setVisibility(View.VISIBLE);
            }

            // Progress visibility
            if (item.getStatus() == DownloadItem.Status.DOWNLOADING) {
                progressIndicator.setVisibility(View.VISIBLE);
                progressIndicator.setProgress(item.getProgress());
                btnAction.setImageResource(R.drawable.ic_download);
            } else if (item.getStatus() == DownloadItem.Status.COMPLETED) {
                progressIndicator.setVisibility(View.GONE);
                btnAction.setImageResource(R.drawable.ic_check);
            } else {
                progressIndicator.setVisibility(View.GONE);
                btnAction.setImageResource(R.drawable.ic_clear);
            }

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged(getSelectedItems().size());
                }
            });

            itemView.setOnClickListener(v -> {
                if (selectionMode) {
                    cbSelect.setChecked(!cbSelect.isChecked());
                } else if (listener != null) {
                    listener.onDownloadClick(item);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (!selectionMode) {
                    setSelectionMode(true);
                    item.setSelected(true);
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onSelectionChanged(1);
                    }
                }
                return true;
            });

            btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onDownloadActionClick(item);
            });
        }
    }
}
