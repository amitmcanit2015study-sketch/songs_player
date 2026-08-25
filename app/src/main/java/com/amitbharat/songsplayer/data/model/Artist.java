package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;

public class Artist implements Serializable {

    private long id;
    private String name;
    private int trackCount;
    private int albumCount;
    private String imageUrl;
    private String genre;

    public Artist(long id, String name, int trackCount, int albumCount) {
        this(id, name, trackCount, albumCount, null, "Artist");
    }

    public Artist(long id, String name, int trackCount, int albumCount, String imageUrl, String genre) {
        this.id = id;
        this.name = name != null ? name : "Unknown Artist";
        this.trackCount = trackCount;
        this.albumCount = albumCount;
        this.imageUrl = imageUrl;
        this.genre = genre != null ? genre : "Artist";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public int getAlbumCount() { return albumCount; }
    public void setAlbumCount(int albumCount) { this.albumCount = albumCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}
