package com.amitbharat.songsplayer.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.amitbharat.songsplayer.data.local.entity.SongEntity;

import java.util.List;

@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongs(List<SongEntity> songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(SongEntity song);

    @Update
    void updateSong(SongEntity song);

    @Query("SELECT * FROM songs ORDER BY title ASC")
    LiveData<List<SongEntity>> getAllSongs();

    @Query("SELECT * FROM songs ORDER BY title ASC")
    List<SongEntity> getAllSongsSync();

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<SongEntity>> getFavoriteSongs();

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT 50")
    LiveData<List<SongEntity>> getMostPlayedSongs();

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 50")
    LiveData<List<SongEntity>> getRecentlyAddedSongs();

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    SongEntity getSongById(long songId);

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    LiveData<SongEntity> getSongByIdLive(long songId);

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    void updateFavorite(long songId, boolean isFavorite);

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    void incrementPlayCount(long songId);

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    LiveData<List<SongEntity>> searchSongs(String query);

    @Query("DELETE FROM songs WHERE id = :songId")
    void deleteSongById(long songId);
}
