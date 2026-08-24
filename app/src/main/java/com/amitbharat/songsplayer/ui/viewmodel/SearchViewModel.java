package com.amitbharat.songsplayer.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.amitbharat.songsplayer.data.local.entity.SongEntity;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.remote.OpenMusicCatalogEngine;
import com.amitbharat.songsplayer.data.repository.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    public enum FilterType {
        ALL,
        ONLINE,
        LOCAL
    }

    private final MusicRepository musicRepository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<FilterType> filterType = new MutableLiveData<>(FilterType.ALL);
    private final MutableLiveData<List<Song>> onlineSearchResults = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<Song>> localSearchResults;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        this.musicRepository = new MusicRepository(application);

        // Pre-fill online search results with curated YouTube songs
        List<Song> initialSuggestions = MusicRepository.getCuratedOnlineTracks();
        onlineSearchResults.setValue(initialSuggestions);

        localSearchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return Transformations.map(musicRepository.getAllSongs(), this::mapEntities);
            }
            return Transformations.map(musicRepository.searchSongs(query.trim()), this::mapEntities);
        });
    }

    private List<Song> mapEntities(List<SongEntity> entities) {
        List<Song> list = new ArrayList<>();
        if (entities != null) {
            for (SongEntity e : entities) {
                list.add(e.toDomain());
            }
        }
        return list;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        if (query != null && !query.trim().isEmpty()) {
            performYouTubeSearch(query.trim());
        } else {
            List<Song> trending = musicRepository.getOnlineTrendingSongs().getValue();
            if (trending != null && !trending.isEmpty()) {
                onlineSearchResults.setValue(trending);
            } else {
                onlineSearchResults.setValue(MusicRepository.getCuratedOnlineTracks());
            }
        }
    }

    public void setFilterType(FilterType type) {
        filterType.setValue(type);
    }

    public LiveData<FilterType> getFilterType() {
        return filterType;
    }

    public LiveData<List<Song>> getLocalSearchResults() {
        return localSearchResults;
    }

    public LiveData<List<Song>> getOnlineSearchResults() {
        return onlineSearchResults;
    }

    private void performYouTubeSearch(String query) {
        // Step 1: Immediate local search for instant 0ms response
        List<Song> instantMatches = new ArrayList<>();
        List<Song> curated = MusicRepository.getCuratedOnlineTracks();
        String lowerQuery = query.toLowerCase();
        for (Song s : curated) {
            if ((s.getTitle() != null && s.getTitle().toLowerCase().contains(lowerQuery))
                    || (s.getArtist() != null && s.getArtist().toLowerCase().contains(lowerQuery))
                    || (s.getAlbum() != null && s.getAlbum().toLowerCase().contains(lowerQuery))) {
                instantMatches.add(s);
            }
        }
        if (!instantMatches.isEmpty()) {
            onlineSearchResults.setValue(instantMatches);
        }

        // Step 2: Query Universal Music Catalog (80M+ Bollywood, Hindi, English, Punjabi, Classical songs)
        com.amitbharat.songsplayer.data.remote.UniversalStreamEngine.searchMusic(query, 1, liveResults -> {
            if (liveResults != null && !liveResults.isEmpty()) {
                onlineSearchResults.setValue(liveResults);
            }
        });
    }
}
