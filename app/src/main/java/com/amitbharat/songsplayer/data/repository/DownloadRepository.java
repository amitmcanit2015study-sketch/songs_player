package com.amitbharat.songsplayer.data.repository;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.data.local.dao.DownloadDao;
import com.amitbharat.songsplayer.data.local.entity.DownloadEntity;
import com.amitbharat.songsplayer.data.model.DownloadItem;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.service.DownloadWorker;
import com.amitbharat.songsplayer.utils.StorageUtils;

import java.io.File;
import java.util.List;

public class DownloadRepository {

    private final DownloadDao downloadDao;
    private final Context context;

    public DownloadRepository(Application application) {
        this.context = application.getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(application);
        this.downloadDao = db.downloadDao();
    }

    public LiveData<List<DownloadEntity>> getAllDownloads() {
        return downloadDao.getAllDownloads();
    }

    public LiveData<List<DownloadEntity>> getCompletedDownloads() {
        return downloadDao.getCompletedDownloads();
    }

    public void startDownload(Song song) {
        startDownload(song, false);
    }

    public void startDownload(Song song, boolean isVideo) {
        if (song == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String extension = isVideo ? ".mp4" : ".mp3";
            String downloadUrl = song.getPlayableUri();

            if (downloadUrl == null || downloadUrl.isEmpty()) {
                return;
            }

            File targetFile = new File(StorageUtils.getMusicDownloadDir(context), song.getId() + extension);

            DownloadItem item = new DownloadItem(
                    song.getId(),
                    song.getTitle() + (isVideo ? " (Video)" : ""),
                    song.getArtist(),
                    song.getArtUrl(),
                    downloadUrl,
                    targetFile.getAbsolutePath(),
                    0,
                    0,
                    DownloadItem.Status.PENDING,
                    0,
                    System.currentTimeMillis()
            );

            downloadDao.insertDownload(DownloadEntity.fromDomain(item));

            Data inputData = new Data.Builder()
                    .putLong("song_id", song.getId())
                    .putString("title", item.getTitle())
                    .putString("stream_url", downloadUrl)
                    .putString("target_path", targetFile.getAbsolutePath())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                    .setInputData(inputData)
                    .addTag("download_" + song.getId())
                    .build();

            WorkManager.getInstance(context).enqueue(workRequest);
        });
    }

    public void deleteDownload(long songId, String localPath) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            downloadDao.deleteDownload(songId);
            StorageUtils.deleteFile(localPath);
        });
    }

    public void deleteDownloads(List<Long> songIds, List<String> paths) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            downloadDao.deleteDownloads(songIds);
            for (String path : paths) {
                StorageUtils.deleteFile(path);
            }
        });
    }

    public void clearCompleted() {
        AppDatabase.databaseWriteExecutor.execute(downloadDao::clearCompletedDownloads);
    }
}
