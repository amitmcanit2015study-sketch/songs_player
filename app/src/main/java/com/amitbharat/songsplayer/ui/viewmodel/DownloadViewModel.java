package com.amitbharat.songsplayer.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.amitbharat.songsplayer.data.local.entity.DownloadEntity;
import com.amitbharat.songsplayer.data.model.DownloadItem;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.repository.DownloadRepository;

import java.util.ArrayList;
import java.util.List;

public class DownloadViewModel extends AndroidViewModel {

    private final DownloadRepository downloadRepository;
    private final LiveData<List<DownloadItem>> allDownloads;
    private final LiveData<List<DownloadItem>> completedDownloads;

    public DownloadViewModel(@NonNull Application application) {
        super(application);
        this.downloadRepository = new DownloadRepository(application);

        allDownloads = Transformations.map(downloadRepository.getAllDownloads(), this::mapEntities);
        completedDownloads = Transformations.map(downloadRepository.getCompletedDownloads(), this::mapEntities);
    }

    private List<DownloadItem> mapEntities(List<DownloadEntity> entities) {
        List<DownloadItem> items = new ArrayList<>();
        if (entities != null) {
            for (DownloadEntity entity : entities) {
                items.add(entity.toDomain());
            }
        }
        return items;
    }

    public LiveData<List<DownloadItem>> getAllDownloads() {
        return allDownloads;
    }

    public LiveData<List<DownloadItem>> getCompletedDownloads() {
        return completedDownloads;
    }

    public void startDownload(Song song) {
        downloadRepository.startDownload(song, false);
    }

    public void startDownload(Song song, boolean isVideo) {
        downloadRepository.startDownload(song, isVideo);
    }

    public void deleteDownload(DownloadItem item) {
        downloadRepository.deleteDownload(item.getSongId(), item.getLocalPath());
    }

    public void deleteSelectedDownloads(List<DownloadItem> items) {
        if (items == null || items.isEmpty()) return;
        List<Long> ids = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        for (DownloadItem item : items) {
            ids.add(item.getSongId());
            paths.add(item.getLocalPath());
        }
        downloadRepository.deleteDownloads(ids, paths);
    }

    public void clearCompleted() {
        downloadRepository.clearCompleted();
    }
}
