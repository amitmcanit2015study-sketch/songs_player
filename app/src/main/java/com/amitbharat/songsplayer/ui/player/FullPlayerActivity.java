package com.amitbharat.songsplayer.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.ActivityFullPlayerBinding;
import com.amitbharat.songsplayer.service.PlaybackService;
import com.amitbharat.songsplayer.ui.dialog.DownloadFormatDialog;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.utils.FormatUtils;
import com.amitbharat.songsplayer.utils.ImageLoader;

public class FullPlayerActivity extends AppCompatActivity {

    private ActivityFullPlayerBinding binding;
    private PlaybackService playbackService;
    private DownloadViewModel downloadViewModel;
    private boolean isBound = false;
    private boolean isUserSeeking = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
            updateFullPlayerUI();
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
        binding = ActivityFullPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        downloadViewModel = new ViewModelProvider(this).get(DownloadViewModel.class);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        setupClickListeners();
        setupSeekBar();
        setupObservers();
    }

    private void setupClickListeners() {
        binding.btnPlayerBack.setOnClickListener(v -> finish());

        binding.btnPlayerMenu.setOnClickListener(v -> {
            Song currentSong = PlaybackService.getCurrentSongLive().getValue();
            if (currentSong == null) return;

            PopupMenu popup = new PopupMenu(this, binding.btnPlayerMenu);
            popup.getMenu().add(0, 1, 0, "Download (MP3 / MP4)");
            popup.getMenu().add(0, 2, 1, currentSong.isFavorite() ? "Remove from Favorites" : "Add to Favorites");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    DownloadFormatDialog dialog = new DownloadFormatDialog(currentSong, (selectedSong, isVideo) -> {
                        downloadViewModel.startDownload(selectedSong, isVideo);
                        Toast.makeText(this, isVideo ? "Downloading video (.mp4)..." : "Downloading audio (.mp3)...", Toast.LENGTH_SHORT).show();
                    });
                    dialog.show(getSupportFragmentManager(), "DownloadFormatDialog");
                    return true;
                } else if (item.getItemId() == 2) {
                    if (playbackService != null) {
                        currentSong.setFavorite(!currentSong.isFavorite());
                        binding.btnPlayerFavorite.setImageResource(currentSong.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });

        binding.fabPlayerPlayPause.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.togglePlayPause();
            }
        });

        binding.btnPlayerNext.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.playNext();
            }
        });

        binding.btnPlayerPrevious.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.playPrevious();
            }
        });

        binding.btnPlayerShuffle.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.toggleShuffle();
            }
        });

        binding.btnPlayerRepeat.setOnClickListener(v -> {
            if (playbackService != null) {
                playbackService.toggleRepeatMode();
            }
        });

        binding.btnBottomEqualizer.setOnClickListener(v -> {
            if (playbackService != null) {
                new EqualizerDialog(playbackService.getAudioEffectsManager()).show(getSupportFragmentManager(), "equalizer");
            }
        });

        binding.btnBottomSpeed.setOnClickListener(v -> {
            if (playbackService != null) {
                new PlaybackSpeedDialog(playbackService).show(getSupportFragmentManager(), "speed");
            }
        });

        binding.btnBottomTimer.setOnClickListener(v -> {
            new SleepTimerDialog().show(getSupportFragmentManager(), "sleep_timer");
        });

        binding.btnBottomQueue.setOnClickListener(v -> {
            if (playbackService != null) {
                new QueueBottomSheetDialog(playbackService).show(getSupportFragmentManager(), "queue");
            }
        });
    }

    private void setupSeekBar() {
        binding.playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && playbackService != null) {
                    long duration = playbackService.getDuration();
                    long targetMs = (duration * progress) / 1000;
                    binding.tvCurrentTime.setText(FormatUtils.formatDuration(targetMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (playbackService != null) {
                    long duration = playbackService.getDuration();
                    long targetMs = (duration * seekBar.getProgress()) / 1000;
                    playbackService.seekTo(targetMs);
                }
                isUserSeeking = false;
            }
        });
    }

    private void setupObservers() {
        PlaybackService.getCurrentSongLive().observe(this, song -> {
            if (song != null) {
                binding.tvPlayerTitle.setText(song.getTitle());
                binding.tvPlayerArtist.setText(String.format("%s • %s", song.getArtist(), song.getAlbum()));
                ImageLoader.loadAlbumArt(this, song.getAlbumId(), song.getArtUrl(), binding.playerAlbumArt);

                if (song.isFavorite()) {
                    binding.btnPlayerFavorite.setImageResource(R.drawable.ic_favorite);
                } else {
                    binding.btnPlayerFavorite.setImageResource(R.drawable.ic_favorite_border);
                }
            } else {
                finish();
            }
        });

        PlaybackService.getIsPlayingLive().observe(this, isPlaying -> {
            if (isPlaying != null && isPlaying) {
                binding.fabPlayerPlayPause.setImageResource(R.drawable.ic_pause);
                startProgressUpdater();
            } else {
                binding.fabPlayerPlayPause.setImageResource(R.drawable.ic_play);
                stopProgressUpdater();
            }
        });

        PlaybackService.getDurationLive().observe(this, duration -> {
            if (duration != null && duration > 0) {
                binding.tvTotalDuration.setText(FormatUtils.formatDuration(duration));
            }
        });

        PlaybackService.getRepeatModeLive().observe(this, mode -> {
            if (mode == null) return;
            if (mode == Player.REPEAT_MODE_OFF) {
                binding.btnPlayerRepeat.setImageResource(R.drawable.ic_repeat);
                binding.btnPlayerRepeat.setColorFilter(getResources().getColor(R.color.outline_dark));
            } else if (mode == Player.REPEAT_MODE_ALL) {
                binding.btnPlayerRepeat.setImageResource(R.drawable.ic_repeat);
                binding.btnPlayerRepeat.setColorFilter(getResources().getColor(R.color.primary));
            } else {
                binding.btnPlayerRepeat.setImageResource(R.drawable.ic_repeat_one);
                binding.btnPlayerRepeat.setColorFilter(getResources().getColor(R.color.primary));
            }
        });

        PlaybackService.getShuffleModeLive().observe(this, shuffle -> {
            if (shuffle != null && shuffle) {
                binding.btnPlayerShuffle.setColorFilter(getResources().getColor(R.color.primary));
            } else {
                binding.btnPlayerShuffle.setColorFilter(getResources().getColor(R.color.outline_dark));
            }
        });
    }

    private void updateFullPlayerUI() {
        if (playbackService != null) {
            long duration = playbackService.getDuration();
            long position = playbackService.getCurrentPosition();
            if (duration > 0) {
                binding.tvTotalDuration.setText(FormatUtils.formatDuration(duration));
                binding.tvCurrentTime.setText(FormatUtils.formatDuration(position));
                int progress = (int) (position * 1000 / duration);
                binding.playerSeekBar.setProgress(progress);
            }
        }
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (playbackService != null && !isUserSeeking) {
                long pos = playbackService.getCurrentPosition();
                long dur = playbackService.getDuration();
                if (dur > 0) {
                    binding.tvCurrentTime.setText(FormatUtils.formatDuration(pos));
                    int progress = (int) (pos * 1000 / dur);
                    binding.playerSeekBar.setProgress(progress);
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
