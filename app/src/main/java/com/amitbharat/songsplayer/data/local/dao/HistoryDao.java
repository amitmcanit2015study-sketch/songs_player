package com.amitbharat.songsplayer.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.amitbharat.songsplayer.data.local.entity.HistoryEntity;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryEntity history);

    @Transaction
    @Query("SELECT s.* FROM songs s INNER JOIN playback_history h ON s.id = h.songId ORDER BY h.playedAt DESC LIMIT 50")
    LiveData<List<SongEntity>> getRecentlyPlayedSongs();

    @Query("DELETE FROM playback_history")
    void clearHistory();
}
