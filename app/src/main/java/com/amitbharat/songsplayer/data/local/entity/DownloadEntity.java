package com.amitbharat.songsplayer.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.amitbharat.songsplayer.data.model.DownloadItem;

@Entity(tableName = "downloads")
public class DownloadEntity {

    @PrimaryKey
    public long songId;
    public String title;
    public String artist;
    public String artUrl;
    public String streamUrl;
    public String localPath;
    public long totalBytes;
    public long downloadedBytes;
    public String status; // PENDING, DOWNLOADING, COMPLETED, FAILED
    public int progress;
    public long downloadDate;

    public DownloadEntity() {}

    public static DownloadEntity fromDomain(DownloadItem item) {
        DownloadEntity entity = new DownloadEntity();
        entity.songId = item.getSongId();
        entity.title = item.getTitle();
        entity.artist = item.getArtist();
        entity.artUrl = item.getArtUrl();
        entity.streamUrl = item.getStreamUrl();
        entity.localPath = item.getLocalPath();
        entity.totalBytes = item.getTotalBytes();
        entity.downloadedBytes = item.getDownloadedBytes();
        entity.status = item.getStatus().name();
        entity.progress = item.getProgress();
        entity.downloadDate = item.getDownloadDate();
        return entity;
    }

    public DownloadItem toDomain() {
        DownloadItem.Status s = DownloadItem.Status.PENDING;
        try {
            s = DownloadItem.Status.valueOf(status);
        } catch (Exception ignored) {}

        return new DownloadItem(
                songId, title, artist, artUrl, streamUrl,
                localPath, totalBytes, downloadedBytes, s,
                progress, downloadDate
        );
    }
}
