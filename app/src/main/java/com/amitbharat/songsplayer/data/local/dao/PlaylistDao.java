package com.amitbharat.songsplayer.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;
import com.amitbharat.songsplayer.data.local.entity.PlaylistSongCrossRef;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlaylist(PlaylistEntity playlist);

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    LiveData<List<PlaylistEntity>> getAllPlaylists();

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    PlaylistEntity getPlaylistById(long playlistId);

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    void renamePlaylist(long playlistId, String name);

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    void deletePlaylist(long playlistId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlaylistSongCrossRef(PlaylistSongCrossRef crossRef);

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    void removeSongFromPlaylist(long playlistId, long songId);

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    void clearPlaylistSongs(long playlistId);

    @Transaction
    @Query("SELECT s.* FROM songs s INNER JOIN playlist_song_cross_ref ref ON s.id = ref.songId WHERE ref.playlistId = :playlistId ORDER BY ref.orderIndex ASC")
    LiveData<List<SongEntity>> getSongsForPlaylist(long playlistId);

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    int getSongCountForPlaylist(long playlistId);
}
