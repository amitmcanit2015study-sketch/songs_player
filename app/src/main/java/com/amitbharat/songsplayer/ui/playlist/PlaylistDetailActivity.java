package com.amitbharat.songsplayer.ui.playlist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.ActivityPlaylistDetailBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.PlaylistViewModel;
import com.amitbharat.songsplayer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private ActivityPlaylistDetailBinding binding;
    private PlaylistViewModel playlistViewModel;
    private MainViewModel mainViewModel;
    private SongAdapter songAdapter;
    private PlaybackService playbackService;
    private boolean isBound = false;
    private final List<Song> playlistSongs = new ArrayList<>();
    private long playlistId;
    private String playlistName;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        playlistId = getIntent().getLongExtra(Constants.EXTRA_PLAYLIST_ID, 0);
        playlistName = getIntent().getStringExtra(Constants.EXTRA_PLAYLIST_NAME);

        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        binding.tvPlaylistTitle.setText(playlistName != null ? playlistName : "Playlist");
        binding.toolbarPlaylistDetail.setNavigationOnClickListener(v -> finish());

        songAdapter = new SongAdapter(this);
        binding.rvPlaylistSongs.setAdapter(songAdapter);

        loadSongs();

        binding.fabPlayAll.setOnClickListener(v -> {
            if (playbackService != null && !playlistSongs.isEmpty()) {
                playbackService.playSongList(playlistSongs, 0);
            }
        });
    }

    private void loadSongs() {
        if (playlistId == -1) {
            // Favorites
            mainViewModel.getFavoriteSongs().observe(this, songs -> updateSongs(songs));
        } else if (playlistId == -2) {
            // Recently Played
            mainViewModel.getRecentlyPlayedSongs().observe(this, songs -> updateSongs(songs));
        } else if (playlistId == -3) {
            // Most Played
            mainViewModel.getMostPlayedSongs().observe(this, songs -> updateSongs(songs));
        } else if (playlistId == -4) {
            // Recently Added
            mainViewModel.getRecentlyAddedSongs().observe(this, songs -> updateSongs(songs));
        } else {
            // Custom Playlist
            playlistViewModel.getSongsForPlaylist(playlistId).observe(this, songs -> updateSongs(songs));
        }
    }

    private void updateSongs(List<Song> songs) {
        playlistSongs.clear();
        if (songs != null) {
            playlistSongs.addAll(songs);
        }
        songAdapter.submitList(new ArrayList<>(playlistSongs));
        binding.tvPlaylistSongCount.setText(String.format("%d songs", playlistSongs.size()));
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (playbackService != null && !playlistSongs.isEmpty()) {
            playbackService.playSongList(playlistSongs, position);
        }
    }

    @Override
    public void onFavoriteClick(Song song, int position) {
        mainViewModel.toggleFavorite(song);
    }

    @Override
    public void onMoreClick(Song song, View anchorView, int position) {
        // More options
    }

    @Override
    protected void onDestroy() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onDestroy();
    }
}
