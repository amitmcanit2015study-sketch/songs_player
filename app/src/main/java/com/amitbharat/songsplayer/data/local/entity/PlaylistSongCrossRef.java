package com.amitbharat.songsplayer.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = {"playlistId", "songId"},
    indices = {@Index("playlistId"), @Index("songId")}
)
public class PlaylistSongCrossRef {

    public long playlistId;
    public long songId;
    public int orderIndex;

    public PlaylistSongCrossRef(long playlistId, long songId, int orderIndex) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.orderIndex = orderIndex;
    }
}
