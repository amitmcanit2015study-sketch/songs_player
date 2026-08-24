package com.amitbharat.songsplayer.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playback_history")
public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public long songId;
    public long playedAt;
    public long playbackDuration;

    public HistoryEntity(long songId, long playedAt, long playbackDuration) {
        this.songId = songId;
        this.playedAt = playedAt;
        this.playbackDuration = playbackDuration;
    }
}
