package com.amitbharat.songsplayer.ui.main;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.ActivityMainBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.amitbharat.songsplayer.ui.offline.OfflineFragment;
import com.amitbharat.songsplayer.ui.online.OnlineFragment;
import com.amitbharat.songsplayer.ui.player.EqualizerDialog;
import com.amitbharat.songsplayer.ui.player.FullPlayerActivity;
import com.amitbharat.songsplayer.ui.player.SleepTimerDialog;
import com.amitbharat.songsplayer.ui.settings.AboutActivity;
import com.amitbharat.songsplayer.ui.settings.SettingsActivity;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.utils.ImageLoader;
import com.amitbharat.songsplayer.utils.PermissionUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private PlaybackService playbackService;
    private boolean isBound = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection serviceConnection = new ServiceConnection() {
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

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = true;
                for (Boolean isGranted : result.values()) {
                    if (!isGranted) {
                        granted = false;
                        break;
                    }
                }
                if (granted) {
                    viewModel.scanLocalMedia();
                } else {
                    Toast.makeText(this, R.string.permission_audio_rationale, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Bind PlaybackService
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Check & request permissions
        String[] required = PermissionUtils.getRequiredPermissions(this);
        if (required.length > 0) {
            permissionLauncher.launch(required);
        } else {
            viewModel.scanLocalMedia();
        }

        setupToolbar();
        setupNavigation();
        setupMiniPlayer();

        // Default to Online Tab
        if (savedInstanceState == null) {
            loadFragment(new OnlineFragment(), false);
        }
    }

    private long lastBackPressedTime = 0;

    private void setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_search) {
                // Focus on Online tab
                binding.bottomNavigation.setSelectedItemId(R.id.nav_online);
                return true;
            } else if (id == R.id.action_sleep_timer) {
                new SleepTimerDialog().show(getSupportFragmentManager(), "sleep_timer");
                return true;
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, com.amitbharat.songsplayer.ui.settings.SettingsActivity.class));
                return true;
            } else if (id == R.id.action_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_online) {
                loadFragment(new OnlineFragment(), false);
                return true;
            } else if (id == R.id.nav_artists) {
                loadFragment(new com.amitbharat.songsplayer.ui.artists.ArtistsFragment(), false);
                return true;
            } else if (id == R.id.nav_offline) {
                loadFragment(new OfflineFragment(), false);
                return true;
            } else if (id == R.id.nav_device) {
                loadFragment(new com.amitbharat.songsplayer.ui.device.DeviceFragment(), false);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomNavigation.getSelectedItemId() != R.id.nav_online) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_online);
            return;
        }

        if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
            // Keep playing music in the background and minimize app
            moveTaskToBack(true);
        } else {
            lastBackPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "Press back again to exit (music will keep playing)", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
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
            } else {
                binding.miniPlayerContainer.setVisibility(View.GONE);
            }
        });

        // Observe Play/Pause State
        PlaybackService.getIsPlayingLive().observe(this, isPlaying -> {
            if (isPlaying != null && isPlaying) {
                binding.miniPlayerLayout.miniPlayPauseButton.setImageResource(R.drawable.ic_pause);
                startProgressUpdater();
            } else {
                binding.miniPlayerLayout.miniPlayPauseButton.setImageResource(R.drawable.ic_play);
                stopProgressUpdater();
            }
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
                viewModel.toggleFavorite(song);
            }
        });

        // Open Full Screen Player on clicking mini player
        binding.miniPlayerLayout.miniPlayerRoot.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullPlayerActivity.class);
            startActivity(intent);
        });
    }

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

    private void startProgressUpdater() {
        stopProgressUpdater();
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdater() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    public void playSong(Song song) {
        if (playbackService != null) {
            playbackService.playSong(song);
        }
    }

    public void playSongList(List<Song> songs, int position) {
        if (playbackService != null) {
            playbackService.playSongList(songs, position);
        }
    }

    public PlaybackService getPlaybackService() {
        return playbackService;
    }

    @Override
    protected void onDestroy() {
        stopProgressUpdater();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        super.onDestroy();
    }
}
