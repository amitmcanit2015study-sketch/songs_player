package com.amitbharat.songsplayer.ui.artists;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.remote.UniversalStreamEngine;
import com.amitbharat.songsplayer.databinding.ActivityArtistDetailBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.player.FullPlayerActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.utils.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtistDetailActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private ActivityArtistDetailBinding binding;
    private MainViewModel mainViewModel;
    private DownloadViewModel downloadViewModel;
    private SongAdapter songAdapter;
    private PlaybackService playbackService;
    private boolean isBound = false;
    private final List<Song> artistSongs = new ArrayList<>();
    private Artist artist;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (playbackService != null) {
                long pos = playbackService.getCurrentPosition();
                long dur = playbackService.getDuration();
                if (dur > 0) {
                    int progress = (int) (pos * 1000 / dur);
                    binding.miniPlayerLayout.miniProgressBar.setProgress(progress);
                }
            }
            progressHandler.postDelayed(this, 500);
        }
    };

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
        binding = ActivityArtistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        artist = (Artist) getIntent().getSerializableExtra("artist");
        if (artist == null) {
            finish();
            return;
        }

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        downloadViewModel = new ViewModelProvider(this).get(DownloadViewModel.class);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        setupViews();
        setupMiniPlayer();
        loadArtistSongs();
    }

    private void setupViews() {
        binding.toolbarArtistDetail.setNavigationOnClickListener(v -> finish());
        binding.tvArtistDetailName.setText(artist.getName());
        binding.tvArtistDetailInfo.setText(String.format("%s • Loading songs...", artist.getGenre() != null ? artist.getGenre() : "Artist"));

        ImageLoader.loadAlbumArt(this, 0, artist.getImageUrl(), binding.ivArtistDetailArt);

        songAdapter = new SongAdapter(this);
        binding.rvArtistSongs.setAdapter(songAdapter);

        binding.btnArtistPlayAll.setOnClickListener(v -> {
            if (playbackService != null && !artistSongs.isEmpty()) {
                playbackService.playSongList(artistSongs, 0);
            } else if (artistSongs.isEmpty()) {
                Toast.makeText(this, "No songs available to play", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnArtistShuffle.setOnClickListener(v -> {
            if (playbackService != null && !artistSongs.isEmpty()) {
                List<Song> shuffled = new ArrayList<>(artistSongs);
                Collections.shuffle(shuffled);
                playbackService.playSongList(shuffled, 0);
            } else if (artistSongs.isEmpty()) {
                Toast.makeText(this, "No songs available to play", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMiniPlayer() {
        // Observe Current Song
        PlaybackService.getCurrentSongLive().observe(this, song -> {
            if (song != null) {
                binding.miniPlayerContainer.setVisibility(View.VISIBLE);
                binding.miniPlayerLayout.miniSongTitle.setText(song.getTitle());
                binding.miniPlayerLayout.miniSongArtist.setText(song.getArtist());
                ImageLoader.loadAlbumArt(this, song.getAlbumId(), song.getArtUrl(), binding.miniPlayerLayout.miniAlbumArt);

                if (song.isFavorite()) {
                    binding.miniPlayerLayout.miniFavoriteButton.setImageResource(R.drawable.ic_favorite);
                } else {
                    binding.miniPlayerLayout.miniFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
                }

                Boolean isPlaying = PlaybackService.getIsPlayingLive().getValue();
                songAdapter.setPlayingState(song, isPlaying != null && isPlaying);
            } else {
                binding.miniPlayerContainer.setVisibility(View.GONE);
                songAdapter.setPlayingState(null, false);
            }
        });

        // Observe Play/Pause State
        PlaybackService.getIsPlayingLive().observe(this, isPlaying -> {
            Song song = PlaybackService.getCurrentSongLive().getValue();
            boolean playing = isPlaying != null && isPlaying;
            if (playing) {
                binding.miniPlayerLayout.miniPlayPauseButton.setImageResource(R.drawable.ic_pause);
                startProgressUpdater();
            } else {
                binding.miniPlayerLayout.miniPlayPauseButton.setImageResource(R.drawable.ic_play);
                stopProgressUpdater();
            }
            songAdapter.setPlayingState(song, playing);
        });

        // Mini player controls click listeners
        binding.miniPlayerLayout.miniPlayPauseButton.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.togglePlayPause();
            }
        });

        binding.miniPlayerLayout.miniNextButton.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.playNext();
            }
        });

        binding.miniPlayerLayout.miniFavoriteButton.setOnClickListener(v -> {
            Song song = PlaybackService.getCurrentSongLive().getValue();
            if (song != null) {
                mainViewModel.toggleFavorite(song);
            }
        });

        // Open Full Screen Player on clicking mini player
        binding.miniPlayerLayout.miniPlayerRoot.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullPlayerActivity.class);
            startActivity(intent);
        });
    }

    private void startProgressUpdater() {
        stopProgressUpdater();
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdater() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void loadArtistSongs() {
        binding.progressLoadingArtistSongs.setVisibility(View.VISIBLE);
        binding.tvEmptyArtistSongs.setVisibility(View.GONE);

        // 1. First look for local scanned songs by this artist
        mainViewModel.getAllSongs().observe(this, allSongs -> {
            if (allSongs != null) {
                for (Song s : allSongs) {
                    if (s.getArtist() != null && s.getArtist().toLowerCase().contains(artist.getName().toLowerCase())) {
                        boolean exists = false;
                        for (Song existing : artistSongs) {
                            if (existing.getId() == s.getId() || existing.getTitle().equalsIgnoreCase(s.getTitle())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            artistSongs.add(s);
                        }
                    }
                }
                songAdapter.submitList(new ArrayList<>(artistSongs));
                updateHeaderInfo();
            }
        });

        // 2. Fetch rich online songs catalogue for this artist
        UniversalStreamEngine.searchMusic(artist.getName(), 1, onlineSongs -> {
            runOnUiThread(() -> {
                binding.progressLoadingArtistSongs.setVisibility(View.GONE);
                if (onlineSongs != null && !onlineSongs.isEmpty()) {
                    for (Song s : onlineSongs) {
                        boolean exists = false;
                        for (Song existing : artistSongs) {
                            if (existing.getTitle().equalsIgnoreCase(s.getTitle())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            artistSongs.add(s);
                        }
                    }

                    // Fallback to top track's artwork for artist avatar if artist photo failed
                    if (artist.getImageUrl() == null || artist.getImageUrl().isEmpty()) {
                        ImageLoader.loadAlbumArt(this, 0, onlineSongs.get(0).getArtUrl(), binding.ivArtistDetailArt);
                    }
                }

                songAdapter.submitList(new ArrayList<>(artistSongs));
                updateHeaderInfo();

                if (artistSongs.isEmpty()) {
                    binding.tvEmptyArtistSongs.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyArtistSongs.setVisibility(View.GONE);
                }
            });
        });
    }

    private void updateHeaderInfo() {
        String genre = artist.getGenre() != null ? artist.getGenre() : "Artist";
        binding.tvArtistDetailInfo.setText(String.format("%s • %d songs", genre, artistSongs.size()));
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (playbackService != null && !artistSongs.isEmpty()) {
            // Plays clicked song and queues all artist tracks in sequence
            playbackService.playSongList(artistSongs, position);
        }
    }

    @Override
    public void onFavoriteClick(Song song, int position) {
        mainViewModel.toggleFavorite(song);
    }

    @Override
    public void onMoreClick(Song song, View anchorView, int position) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.song_item_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_play_next) {
                if (playbackService != null) {
                    playbackService.addToQueue(song);
                    Toast.makeText(this, "Added to queue", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.action_add_to_queue) {
                if (playbackService != null) {
                    playbackService.addToQueue(song);
                    Toast.makeText(this, "Added to queue", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.action_download) {
                downloadViewModel.startDownload(song);
                Toast.makeText(this, "Starting download: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_favorite) {
                mainViewModel.toggleFavorite(song);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    protected void onDestroy() {
        stopProgressUpdater();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onDestroy();
    }
}
