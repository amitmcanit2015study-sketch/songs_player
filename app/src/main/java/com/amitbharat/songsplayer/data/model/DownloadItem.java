package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;

public class DownloadItem implements Serializable {

    public enum Status {
        PENDING,
        DOWNLOADING,
        COMPLETED,
        FAILED
    }

    private long songId;
    private String title;
    private String artist;
    private String artUrl;
    private String streamUrl;
    private String localPath;
    private long totalBytes;
    private long downloadedBytes;
    private Status status;
    private int progress;
    private long downloadDate;
    private boolean isSelected;

    public DownloadItem(long songId, String title, String artist, String artUrl,
                        String streamUrl, String localPath, long totalBytes,
                        long downloadedBytes, Status status, int progress, long downloadDate) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.artUrl = artUrl;
        this.streamUrl = streamUrl;
        this.localPath = localPath;
        this.totalBytes = totalBytes;
        this.downloadedBytes = downloadedBytes;
        this.status = status;
        this.progress = progress;
        this.downloadDate = downloadDate;
        this.isSelected = false;
    }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getArtUrl() { return artUrl; }
    public void setArtUrl(String artUrl) { this.artUrl = artUrl; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) { this.totalBytes = totalBytes; }

    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public long getDownloadDate() { return downloadDate; }
    public void setDownloadDate(long downloadDate) { this.downloadDate = downloadDate; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
