package com.amitbharat.songsplayer.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.data.local.dao.HistoryDao;
import com.amitbharat.songsplayer.data.local.dao.SongDao;
import com.amitbharat.songsplayer.data.local.entity.HistoryEntity;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;
import com.amitbharat.songsplayer.data.mediastore.MediaStoreScanner;
import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.remote.OpenMusicCatalogEngine;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {

    private final SongDao songDao;
    private final HistoryDao historyDao;
    private final MediaStoreScanner mediaStoreScanner;
    private final MutableLiveData<List<Song>> onlineTrendingSongs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Album>> localAlbums = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Artist>> localArtists = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);

    public MusicRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.songDao = db.songDao();
        this.historyDao = db.historyDao();
        this.mediaStoreScanner = new MediaStoreScanner(application);

        List<Song> curated = getCuratedOnlineTracks();
        onlineTrendingSongs.setValue(curated);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<SongEntity> entities = new ArrayList<>();
            for (Song s : curated) {
                entities.add(SongEntity.fromDomain(s));
            }
            songDao.insertSongs(entities);
        });

        fetchOnlineTrending();
    }

    public static List<Song> getCuratedOnlineTracks() {
        List<Song> list = new ArrayList<>();

        list.add(new Song(
                2001L,
                "Gehra Hua (From \"Dhurandhar\")",
                "Arijit Singh, Shashwat Sachdev, Irshad Kamil",
                "Dhurandhar",
                0,
                362000,
                null,
                true,
                "https://aac.saavncdn.com/450/f467e05e2825cec2203546333e0d0550_320.mp4",
                "https://c.saavncdn.com/450/Gehra-Hua-From-Dhurandhar-Hindi-2025-20251205154217-500x500.webp",
                false,
                56110917,
                System.currentTimeMillis(),
                14480000
        ));

        list.add(new Song(
                2002L,
                "Tum Hi Ho (From \"Aashiqui 2\")",
                "Arijit Singh, Mithoon",
                "Aashiqui 2",
                0,
                262000,
                null,
                true,
                "https://aac.saavncdn.com/840/c9e70fb62d66fa6e14f6b7cdbc56cc06_320.mp4",
                "https://c.saavncdn.com/840/Aashiqui-2-Hindi-2013-500x500.jpg",
                false,
                89200000,
                System.currentTimeMillis(),
                10480000
        ));

        list.add(new Song(
                2003L,
                "Hass Hass",
                "Diljit Dosanjh, Sia, Greg Kurstin",
                "Hass Hass",
                0,
                154000,
                null,
                true,
                "https://aac.saavncdn.com/245/fd196de0f557e19e2e8d42150d34cf57_320.mp4",
                "https://c.saavncdn.com/245/Hass-Hass-Punjabi-2023-20231026053424-500x500.jpg",
                false,
                42000000,
                System.currentTimeMillis(),
                6160000
        ));

        list.add(new Song(
                2004L,
                "Believer",
                "Imagine Dragons",
                "Evolve",
                0,
                204000,
                null,
                true,
                "https://aac.saavncdn.com/248/46944eb7b4b31f5b0abf5eb2e1be2d25_320.mp4",
                "https://c.saavncdn.com/248/Evolve-English-2017-500x500.jpg",
                false,
                78000000,
                System.currentTimeMillis(),
                8160000
        ));

        list.add(new Song(
                2005L,
                "Chura Ke Dil Mera",
                "Kumar Sanu, Alka Yagnik, Anu Malik",
                "Main Khiladi Tu Anari",
                0,
                473000,
                null,
                true,
                "https://aac.saavncdn.com/474/fc90bd930001b9e5eb98ac005cce18be_320.mp4",
                "https://c.saavncdn.com/474/Main-Khiladi-Tu-Anari-Hindi-1994-500x500.jpg",
                false,
                31000000,
                System.currentTimeMillis(),
                18920000
        ));

        list.add(new Song(
                2006L,
                "Apna Bana Le",
                "Arijit Singh, Sachin-Jigar, Amitabh Bhattacharya",
                "Bhediya",
                0,
                261000,
                null,
                true,
                "https://aac.saavncdn.com/085/8763ba5bceea680e6c1e95cfc925567b_320.mp4",
                "https://c.saavncdn.com/085/Bhediya-Hindi-2022-20221124110323-500x500.jpg",
                false,
                64000000,
                System.currentTimeMillis(),
                10440000
        ));

        return list;
    }

    public LiveData<List<SongEntity>> getAllSongs() {
        return songDao.getAllSongs();
    }

    public LiveData<List<SongEntity>> getFavoriteSongs() {
        return songDao.getFavoriteSongs();
    }

    public LiveData<List<SongEntity>> getMostPlayedSongs() {
        return songDao.getMostPlayedSongs();
    }

    public LiveData<List<SongEntity>> getRecentlyAddedSongs() {
        return songDao.getRecentlyAddedSongs();
    }

    public LiveData<List<SongEntity>> getRecentlyPlayedSongs() {
        return historyDao.getRecentlyPlayedSongs();
    }

    public LiveData<List<Song>> getOnlineTrendingSongs() {
        return onlineTrendingSongs;
    }

    public LiveData<List<Album>> getLocalAlbums() {
        return localAlbums;
    }

    public LiveData<List<Artist>> getLocalArtists() {
        return localArtists;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public void scanLocalMedia() {
        isScanning.postValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Song> scannedSongs = mediaStoreScanner.scanDeviceSongs();
                if (!scannedSongs.isEmpty()) {
                    List<SongEntity> entities = new ArrayList<>();
                    for (Song song : scannedSongs) {
                        entities.add(SongEntity.fromDomain(song));
                    }
                    songDao.insertSongs(entities);

                    List<Album> albums = mediaStoreScanner.extractAlbums(scannedSongs);
                    List<Artist> artists = mediaStoreScanner.extractArtists(scannedSongs);
                    localAlbums.postValue(albums);
                    localArtists.postValue(artists);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isScanning.postValue(false);
            }
        });
    }

    public void fetchOnlineTrending() {
        // Fetch trending music using Universal Music Engine (80M+ catalog with 320kbps CDNs)
        com.amitbharat.songsplayer.data.remote.UniversalStreamEngine.fetchTrendingMusic(songs -> {
            if (songs != null && !songs.isEmpty()) {
                onlineTrendingSongs.postValue(songs);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    List<SongEntity> entities = new ArrayList<>();
                    for (Song s : songs) {
                        entities.add(SongEntity.fromDomain(s));
                    }
                    songDao.insertSongs(entities);
                });
            }
        });
    }

    public void toggleFavorite(Song song) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean newFav = !song.isFavorite();
            song.setFavorite(newFav);
            songDao.updateFavorite(song.getId(), newFav);
        });
    }

    public void recordPlayback(Song song, long duration) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            songDao.incrementPlayCount(song.getId());
            historyDao.insertHistory(new HistoryEntity(song.getId(), System.currentTimeMillis(), duration));
        });
    }

    public LiveData<List<SongEntity>> searchSongs(String query) {
        return songDao.searchSongs(query);
    }
}
