package com.amitbharat.songsplayer.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.amitbharat.songsplayer.data.local.entity.SongEntity;
import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.repository.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final MusicRepository musicRepository;
    private final LiveData<List<Song>> allSongs;
    private final LiveData<List<Song>> favoriteSongs;
    private final LiveData<List<Song>> mostPlayedSongs;
    private final LiveData<List<Song>> recentlyAddedSongs;
    private final LiveData<List<Song>> recentlyPlayedSongs;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.musicRepository = new MusicRepository(application);

        allSongs = Transformations.map(musicRepository.getAllSongs(), this::mapEntitiesToDomain);
        favoriteSongs = Transformations.map(musicRepository.getFavoriteSongs(), this::mapEntitiesToDomain);
        mostPlayedSongs = Transformations.map(musicRepository.getMostPlayedSongs(), this::mapEntitiesToDomain);
        recentlyAddedSongs = Transformations.map(musicRepository.getRecentlyAddedSongs(), this::mapEntitiesToDomain);
        recentlyPlayedSongs = Transformations.map(musicRepository.getRecentlyPlayedSongs(), this::mapEntitiesToDomain);
    }

    private List<Song> mapEntitiesToDomain(List<SongEntity> entities) {
        List<Song> songs = new ArrayList<>();
        if (entities != null) {
            for (SongEntity entity : entities) {
                songs.add(entity.toDomain());
            }
        }
        return songs;
    }

    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    public LiveData<List<Song>> getFavoriteSongs() {
        return favoriteSongs;
    }

    public LiveData<List<Song>> getMostPlayedSongs() {
        return mostPlayedSongs;
    }

    public LiveData<List<Song>> getRecentlyAddedSongs() {
        return recentlyAddedSongs;
    }

    public LiveData<List<Song>> getRecentlyPlayedSongs() {
        return recentlyPlayedSongs;
    }

    public LiveData<List<Song>> getOnlineTrendingSongs() {
        return musicRepository.getOnlineTrendingSongs();
    }

    public LiveData<List<Album>> getLocalAlbums() {
        return musicRepository.getLocalAlbums();
    }

    public LiveData<List<Artist>> getLocalArtists() {
        return musicRepository.getLocalArtists();
    }

    public LiveData<Boolean> getIsScanning() {
        return musicRepository.getIsScanning();
    }

    public void scanLocalMedia() {
        musicRepository.scanLocalMedia();
    }

    public void fetchOnlineTrending() {
        musicRepository.fetchOnlineTrending();
    }

    public void toggleFavorite(Song song) {
        musicRepository.toggleFavorite(song);
    }
}
