package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;

public class Album implements Serializable {

    private long id;
    private String title;
    private String artist;
    private String artUrl;
    private int trackCount;
    private int year;

    public Album(long id, String title, String artist, String artUrl, int trackCount, int year) {
        this.id = id;
        this.title = title != null ? title : "Unknown Album";
        this.artist = artist != null ? artist : "Unknown Artist";
        this.artUrl = artUrl;
        this.trackCount = trackCount;
        this.year = year;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getArtUrl() { return artUrl; }
    public void setArtUrl(String artUrl) { this.artUrl = artUrl; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
