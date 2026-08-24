package com.amitbharat.songsplayer.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.data.local.dao.PlaylistDao;
import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;
import com.amitbharat.songsplayer.data.local.entity.PlaylistSongCrossRef;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;

import java.util.List;

public class PlaylistRepository {

    private final PlaylistDao playlistDao;

    public PlaylistRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.playlistDao = db.playlistDao();
    }

    public LiveData<List<PlaylistEntity>> getAllPlaylists() {
        return playlistDao.getAllPlaylists();
    }

    public LiveData<List<SongEntity>> getSongsForPlaylist(long playlistId) {
        return playlistDao.getSongsForPlaylist(playlistId);
    }

    public void createPlaylist(String name) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PlaylistEntity playlist = new PlaylistEntity(name, System.currentTimeMillis());
            playlistDao.insertPlaylist(playlist);
        });
    }

    public void renamePlaylist(long playlistId, String newName) {
        AppDatabase.databaseWriteExecutor.execute(() -> playlistDao.renamePlaylist(playlistId, newName));
    }

    public void deletePlaylist(long playlistId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            playlistDao.clearPlaylistSongs(playlistId);
            playlistDao.deletePlaylist(playlistId);
        });
    }

    public void addSongToPlaylist(long playlistId, long songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = playlistDao.getSongCountForPlaylist(playlistId);
            PlaylistSongCrossRef ref = new PlaylistSongCrossRef(playlistId, songId, count);
            playlistDao.insertPlaylistSongCrossRef(ref);
        });
    }

    public void removeSongFromPlaylist(long playlistId, long songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> playlistDao.removeSongFromPlaylist(playlistId, songId));
    }
}
