package com.amitbharat.songsplayer.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends AndroidViewModel {

    private final PlaylistRepository playlistRepository;
    private final LiveData<List<PlaylistEntity>> playlists;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        this.playlistRepository = new PlaylistRepository(application);
        this.playlists = playlistRepository.getAllPlaylists();
    }

    public LiveData<List<PlaylistEntity>> getPlaylists() {
        return playlists;
    }

    public LiveData<List<Song>> getSongsForPlaylist(long playlistId) {
        return Transformations.map(playlistRepository.getSongsForPlaylist(playlistId), entities -> {
            List<Song> songs = new ArrayList<>();
            if (entities != null) {
                for (SongEntity e : entities) {
                    songs.add(e.toDomain());
                }
            }
            return songs;
        });
    }

    public void createPlaylist(String name) {
        playlistRepository.createPlaylist(name);
    }

    public void renamePlaylist(long playlistId, String newName) {
        playlistRepository.renamePlaylist(playlistId, newName);
    }

    public void deletePlaylist(long playlistId) {
        playlistRepository.deletePlaylist(playlistId);
    }

    public void addSongToPlaylist(long playlistId, long songId) {
        playlistRepository.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(long playlistId, long songId) {
        playlistRepository.removeSongFromPlaylist(playlistId, songId);
    }
}
