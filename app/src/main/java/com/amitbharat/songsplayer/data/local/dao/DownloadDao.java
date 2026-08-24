package com.amitbharat.songsplayer.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.amitbharat.songsplayer.data.local.entity.DownloadEntity;

import java.util.List;

@Dao
public interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDownload(DownloadEntity download);

    @Update
    void updateDownload(DownloadEntity download);

    @Query("SELECT * FROM downloads ORDER BY downloadDate DESC")
    LiveData<List<DownloadEntity>> getAllDownloads();

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY downloadDate DESC")
    LiveData<List<DownloadEntity>> getCompletedDownloads();

    @Query("SELECT * FROM downloads WHERE songId = :songId LIMIT 1")
    DownloadEntity getDownloadById(long songId);

    @Query("UPDATE downloads SET progress = :progress, downloadedBytes = :downloadedBytes, status = :status WHERE songId = :songId")
    void updateProgress(long songId, int progress, long downloadedBytes, String status);

    @Query("DELETE FROM downloads WHERE songId = :songId")
    void deleteDownload(long songId);

    @Query("DELETE FROM downloads WHERE songId IN (:songIds)")
    void deleteDownloads(List<Long> songIds);

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    void clearCompletedDownloads();
}
