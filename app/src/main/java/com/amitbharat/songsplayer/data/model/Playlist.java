package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;

public class Playlist implements Serializable {

    private long id;
    private String name;
    private int trackCount;
    private long createdAt;
    private boolean isSmart;

    public Playlist(long id, String name, int trackCount, long createdAt, boolean isSmart) {
        this.id = id;
        this.name = name != null ? name : "Untitled Playlist";
        this.trackCount = trackCount;
        this.createdAt = createdAt;
        this.isSmart = isSmart;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isSmart() { return isSmart; }
    public void setSmart(boolean smart) { isSmart = smart; }
}
