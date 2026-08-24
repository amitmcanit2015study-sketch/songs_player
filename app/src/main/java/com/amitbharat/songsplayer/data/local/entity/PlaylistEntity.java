package com.amitbharat.songsplayer.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class PlaylistEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public long createdAt;

    public PlaylistEntity() {}

    @androidx.room.Ignore
    public PlaylistEntity(String name, long createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }
}
