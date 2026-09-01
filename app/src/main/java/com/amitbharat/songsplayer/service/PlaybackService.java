package com.amitbharat.songsplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.session.CommandButton;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.SongsPlayerApp;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.repository.MusicRepository;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.utils.Constants;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class PlaybackService extends MediaSessionService {

    public static final String ACTION_PLAY_PAUSE = "com.amitbharat.songsplayer.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.amitbharat.songsplayer.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.amitbharat.songsplayer.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "com.amitbharat.songsplayer.ACTION_STOP";
    public static final int NOTIFICATION_ID = 1001;

    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private ExoPlayer player;
    private MediaSession mediaSession;
    private AudioEffectsManager audioEffectsManager;
    private MusicRepository musicRepository;
    private SharedPreferences prefs;

    // Queue & Playback State LiveData
    private static final MutableLiveData<Song> currentSongLive = new MutableLiveData<>(null);
    private static final MutableLiveData<Boolean> isPlayingLive = new MutableLiveData<>(false);
    private static final MutableLiveData<Long> currentPositionLive = new MutableLiveData<>(0L);
    private static final MutableLiveData<Long> durationLive = new MutableLiveData<>(0L);
    private static final MutableLiveData<List<Song>> queueLive = new MutableLiveData<>(new ArrayList<>());
    private static final MutableLiveData<Integer> currentQueueIndexLive = new MutableLiveData<>(-1);
    private static final MutableLiveData<Integer> repeatModeLive = new MutableLiveData<>(Player.REPEAT_MODE_OFF);
    private static final MutableLiveData<Boolean> shuffleModeLive = new MutableLiveData<>(false);
    private static final MutableLiveData<Float> playbackSpeedLive = new MutableLiveData<>(1.0f);

    private final List<Song> songQueue = new ArrayList<>();
    private int currentQueueIndex = -1;
    private int playbackRetryCount = 0;

    public static LiveData<Song> getCurrentSongLive() { return currentSongLive; }
    public static LiveData<Boolean> getIsPlayingLive() { return isPlayingLive; }
    public static LiveData<Long> getCurrentPositionLive() { return currentPositionLive; }
    public static LiveData<Long> getDurationLive() { return durationLive; }
    public static LiveData<List<Song>> getQueueLive() { return queueLive; }
    public static LiveData<Integer> getCurrentQueueIndexLive() { return currentQueueIndexLive; }
    public static LiveData<Integer> getRepeatModeLive() { return repeatModeLive; }
    public static LiveData<Boolean> getShuffleModeLive() { return shuffleModeLive; }
    public static LiveData<Float> getPlaybackSpeedLive() { return playbackSpeedLive; }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        musicRepository = new MusicRepository(getApplication());
        audioEffectsManager = new AudioEffectsManager(this);

        // Configure ExoPlayer with robust HTTP DataSource and keep-alive headers
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(20000)
                .setReadTimeoutMs(30000)
                .setKeepPostFor302Redirects(true);

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(httpDataSourceFactory);

        // Configure LoadControl to buffer 3 minutes ahead for smooth long song playback
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        30000,   // minBufferMs (30s)
                        180000,  // maxBufferMs (180s = 3 minutes buffer ahead)
                        1500,    // bufferForPlaybackMs
                        3000     // bufferForPlaybackAfterRebufferMs
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();

        // WAKE_MODE_NETWORK acquires both CPU WakeLock and WiFiLock for unbroken background streaming
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build();

        // Restore playback settings
        int repeatMode = prefs.getInt(Constants.KEY_REPEAT_MODE, Player.REPEAT_MODE_OFF);
        boolean shuffleMode = prefs.getBoolean(Constants.KEY_SHUFFLE_MODE, false);
        float speed = prefs.getFloat(Constants.KEY_PLAYBACK_SPEED, 1.0f);

        player.setRepeatMode(repeatMode);
        player.setShuffleModeEnabled(shuffleMode);
        player.setPlaybackParameters(new PlaybackParameters(speed));

        repeatModeLive.postValue(repeatMode);
        shuffleModeLive.postValue(shuffleMode);
        playbackSpeedLive.postValue(speed);

        // Configure MediaSession with explicit player commands for System Media Notification & Lock Screen
        MediaSession.Callback callback = new MediaSession.Callback() {
            @NonNull
            @Override
            public MediaSession.ConnectionResult onConnect(
                    @NonNull MediaSession session,
                    @NonNull MediaSession.ControllerInfo controller) {
                MediaSession.ConnectionResult connectionResult =
                        MediaSession.Callback.super.onConnect(session, controller);
                Player.Commands playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                        .add(Player.COMMAND_PLAY_PAUSE)
                        .add(Player.COMMAND_SEEK_TO_NEXT)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                        .add(Player.COMMAND_SET_REPEAT_MODE)
                        .add(Player.COMMAND_SET_SHUFFLE_MODE)
                        .add(Player.COMMAND_STOP)
                        .build();
                return new MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(connectionResult.availableSessionCommands)
                        .setAvailablePlayerCommands(playerCommands)
                        .build();
            }
        };

        Intent sessionIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, sessionIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent)
                .setCallback(callback)
                .build();

        // Custom Media Notification Provider ensuring visibility in status bar, notification shade, and lockscreen
        setMediaNotificationProvider(new MediaNotification.Provider() {
            @NonNull
            @Override
            public MediaNotification createNotification(
                    @NonNull MediaSession mediaSession,
                    @NonNull ImmutableList<CommandButton> customLayout,
                    @NonNull MediaNotification.ActionFactory actionFactory,
                    @NonNull MediaNotification.Provider.Callback onNotificationChangedCallback) {
                return new MediaNotification(NOTIFICATION_ID, buildNotification());
            }

            @Override
            public boolean handleCustomCommand(
                    @NonNull MediaSession session,
                    @NonNull String action,
                    @NonNull android.os.Bundle extras) {
                return false;
            }
        });

        // Attach audio effects to audio session
        audioEffectsManager.attachAudioSession(player.getAudioSessionId());

        // Connect Sleep Timer Listener
        SleepTimerManager.getInstance().setListener(this::pause);

        // Player Event Listener for seamless gapless playback and auto-next
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                isPlayingLive.postValue(isPlaying);
                updateForegroundNotification();
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                int index = player.getCurrentMediaItemIndex();
                Song currentSong = null;
                if (mediaItem != null && mediaItem.localConfiguration != null && mediaItem.localConfiguration.tag instanceof Song) {
                    currentSong = (Song) mediaItem.localConfiguration.tag;
                } else if (index >= 0 && index < songQueue.size()) {
                    currentSong = songQueue.get(index);
                }

                if (index >= 0 && index < songQueue.size()) {
                    currentQueueIndex = index;
                } else if (currentSong != null) {
                    int found = songQueue.indexOf(currentSong);
                    if (found >= 0) currentQueueIndex = found;
                }

                if (currentQueueIndex >= 0) {
                    currentQueueIndexLive.postValue(currentQueueIndex);
                }

                if (currentSong != null) {
                    currentSongLive.postValue(currentSong);
                    playbackRetryCount = 0;
                    musicRepository.recordPlayback(currentSong, player.getDuration());
                    updateForegroundNotification();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    playbackRetryCount = 0;
                    durationLive.postValue(player.getDuration());
                    updateForegroundNotification();
                } else if (playbackState == Player.STATE_ENDED) {
                    if (hasSong()) {
                        musicRepository.recordPlayback(songQueue.get(currentQueueIndex), player.getDuration());
                    }
                    if (player.getRepeatMode() == Player.REPEAT_MODE_ONE) {
                        player.seekTo(0);
                        player.prepare();
                        player.play();
                    } else if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem();
                        player.prepare();
                        player.play();
                    } else if (currentQueueIndex + 1 < songQueue.size()) {
                        playNext();
                    } else if (!songQueue.isEmpty()) {
                        // At the end of queue, cycle back to the first song for uninterrupted playback
                        currentQueueIndex = 0;
                        currentQueueIndexLive.postValue(0);
                        if (player.getMediaItemCount() > 0) {
                            player.seekToDefaultPosition(0);
                            player.prepare();
                            player.play();
                        }
                    }
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                error.printStackTrace();
                if (hasSong() && playbackRetryCount < 1) {
                    playbackRetryCount++;
                    player.prepare();
                    player.play();
                } else {
                    // Automatically skip problematic song so queue playback continues seamlessly
                    playbackRetryCount = 0;
                    playNext();
                }
            }

            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                audioEffectsManager.attachAudioSession(audioSessionId);
            }
        });
    }

    public Notification buildNotification() {
        Song currentSong = (currentQueueIndex >= 0 && currentQueueIndex < songQueue.size())
                ? songQueue.get(currentQueueIndex) : null;

        String title = (currentSong != null && currentSong.getTitle() != null)
                ? currentSong.getTitle() : getString(R.string.app_name);
        String artist = (currentSong != null && currentSong.getArtist() != null)
                ? currentSong.getArtist() : "Playing Music";
        String album = (currentSong != null && currentSong.getAlbum() != null)
                ? currentSong.getAlbum() : "";

        Intent sessionIntent = new Intent(this, MainActivity.class);
        sessionIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, sessionIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent prevIntent = new Intent(this, PlaybackService.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseIntent = new Intent(this, PlaybackService.class).setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPausePending = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, PlaybackService.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, PlaybackService.class).setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        boolean isPlaying = player != null && player.isPlaying();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SongsPlayerApp.PLAYBACK_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(album)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(stopPending)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, isPlaying ? "Pause" : "Play", playPausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending);

        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2);

        builder.setStyle(mediaStyle);

        return builder.build();
    }

    public void updateForegroundNotification() {
        try {
            Notification notification = buildNotification();
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_PLAY_PAUSE.equals(action)) {
                togglePlayPause();
            } else if (ACTION_NEXT.equals(action)) {
                playNext();
            } else if (ACTION_PREVIOUS.equals(action)) {
                playPrevious();
            } else if (ACTION_STOP.equals(action)) {
                if (player != null) {
                    player.pause();
                }
                stopForeground(STOP_FOREGROUND_REMOVE);
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (player == null || !player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
        }
    }

    private MediaItem createMediaItem(Song song) {
        if (song == null) return null;
        String uriString = song.getPlayableUri();
        if (uriString == null || uriString.isEmpty()) return null;

        Uri artworkUri = null;
        if (song.getArtUrl() != null && !song.getArtUrl().trim().isEmpty()) {
            artworkUri = Uri.parse(song.getArtUrl());
        } else if (song.getAlbumId() > 0) {
            artworkUri = Uri.parse("content://media/external/audio/albumart/" + song.getAlbumId());
        }

        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtist(song.getArtist())
                .setAlbumTitle(song.getAlbum())
                .setArtworkUri(artworkUri)
                .build();

        return new MediaItem.Builder()
                .setUri(Uri.parse(uriString))
                .setMediaId(String.valueOf(song.getId()))
                .setTag(song)
                .setMediaMetadata(metadata)
                .build();
    }

    public void playSongList(List<Song> songs, int startIndex) {
        if (songs == null || songs.isEmpty()) return;

        List<Song> validSongs = new ArrayList<>();
        List<MediaItem> mediaItems = new ArrayList<>();
        int validStartIndex = 0;

        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            MediaItem mi = createMediaItem(s);
            if (mi != null) {
                if (i == startIndex) {
                    validStartIndex = validSongs.size();
                }
                validSongs.add(s);
                mediaItems.add(mi);
            }
        }

        if (validSongs.isEmpty() || player == null) return;

        songQueue.clear();
        songQueue.addAll(validSongs);
        queueLive.postValue(new ArrayList<>(songQueue));

        if (validStartIndex < 0 || validStartIndex >= songQueue.size()) {
            validStartIndex = 0;
        }

        currentQueueIndex = validStartIndex;
        currentQueueIndexLive.postValue(currentQueueIndex);
        playbackRetryCount = 0;

        player.setMediaItems(mediaItems, validStartIndex, 0);
        player.prepare();
        player.play();
        isPlayingLive.postValue(true);

        Song currentSong = songQueue.get(currentQueueIndex);
        currentSongLive.postValue(currentSong);
        updateForegroundNotification();
    }

    public void playSong(Song song) {
        if (song == null) return;
        List<Song> singleList = new ArrayList<>();
        singleList.add(song);
        playSongList(singleList, 0);
    }

    public void togglePlayPause() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    public void play() {
        if (player != null) {
            player.play();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void playNext() {
        if (player == null || songQueue.isEmpty()) return;

        if (player.getRepeatMode() == Player.REPEAT_MODE_ONE) {
            player.seekTo(0);
            player.prepare();
            player.play();
            return;
        }

        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
            player.prepare();
            player.play();
        } else if (player.getRepeatMode() == Player.REPEAT_MODE_ALL && !songQueue.isEmpty()) {
            player.seekToDefaultPosition(0);
            player.prepare();
            player.play();
        } else if (currentQueueIndex + 1 < songQueue.size()) {
            currentQueueIndex++;
            currentQueueIndexLive.postValue(currentQueueIndex);
            playSongList(songQueue, currentQueueIndex);
        } else if (!songQueue.isEmpty()) {
            currentQueueIndex = 0;
            currentQueueIndexLive.postValue(0);
            player.seekToDefaultPosition(0);
            player.prepare();
            player.play();
        }
    }

    public void playPrevious() {
        if (player == null || songQueue.isEmpty()) return;

        if (player.getCurrentPosition() > 3000) {
            player.seekTo(0);
            player.prepare();
            player.play();
            return;
        }

        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem();
            player.prepare();
            player.play();
        } else if (currentQueueIndex - 1 >= 0) {
            currentQueueIndex--;
            currentQueueIndexLive.postValue(currentQueueIndex);
            playSongList(songQueue, currentQueueIndex);
        } else {
            player.seekTo(0);
            player.prepare();
            player.play();
        }
    }

    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
        }
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    public void toggleShuffle() {
        if (player == null) return;
        boolean newShuffle = !player.getShuffleModeEnabled();
        player.setShuffleModeEnabled(newShuffle);
        shuffleModeLive.postValue(newShuffle);
        prefs.edit().putBoolean(Constants.KEY_SHUFFLE_MODE, newShuffle).apply();
    }

    public void toggleRepeatMode() {
        if (player == null) return;
        int nextMode;
        int current = player.getRepeatMode();
        if (current == Player.REPEAT_MODE_OFF) {
            nextMode = Player.REPEAT_MODE_ALL;
        } else if (current == Player.REPEAT_MODE_ALL) {
            nextMode = Player.REPEAT_MODE_ONE;
        } else {
            nextMode = Player.REPEAT_MODE_OFF;
        }
        player.setRepeatMode(nextMode);
        repeatModeLive.postValue(nextMode);
        prefs.edit().putInt(Constants.KEY_REPEAT_MODE, nextMode).apply();
    }

    public void setPlaybackSpeed(float speed) {
        if (player == null) return;
        player.setPlaybackParameters(new PlaybackParameters(speed));
        playbackSpeedLive.postValue(speed);
        prefs.edit().putFloat(Constants.KEY_PLAYBACK_SPEED, speed).apply();
    }

    public void addToQueue(Song song) {
        if (song == null) return;
        songQueue.add(song);
        MediaItem mi = createMediaItem(song);
        if (mi != null && player != null) {
            player.addMediaItem(mi);
        }
        queueLive.postValue(new ArrayList<>(songQueue));
    }

    public void playNextInQueue(Song song) {
        if (song == null) return;
        int nextIndex = (currentQueueIndex >= 0 && currentQueueIndex < songQueue.size()) ? currentQueueIndex + 1 : songQueue.size();
        songQueue.add(nextIndex, song);
        MediaItem mi = createMediaItem(song);
        if (mi != null && player != null) {
            player.addMediaItem(nextIndex, mi);
        }
        queueLive.postValue(new ArrayList<>(songQueue));
    }

    public void removeFromQueue(int position) {
        if (position >= 0 && position < songQueue.size()) {
            songQueue.remove(position);
            if (player != null && position < player.getMediaItemCount()) {
                player.removeMediaItem(position);
            }
            if (position < currentQueueIndex) {
                currentQueueIndex--;
            } else if (position == currentQueueIndex && !songQueue.isEmpty()) {
                if (currentQueueIndex >= songQueue.size()) {
                    currentQueueIndex = 0;
                }
            }
            queueLive.postValue(new ArrayList<>(songQueue));
            currentQueueIndexLive.postValue(currentQueueIndex);
        }
    }

    public void clearQueue() {
        songQueue.clear();
        currentQueueIndex = -1;
        queueLive.postValue(new ArrayList<>());
        currentQueueIndexLive.postValue(-1);
        if (player != null) {
            player.clearMediaItems();
            player.stop();
        }
        currentSongLive.postValue(null);
    }

    public AudioEffectsManager getAudioEffectsManager() {
        return audioEffectsManager;
    }

    private boolean hasSong() {
        return currentQueueIndex >= 0 && currentQueueIndex < songQueue.size();
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        if (audioEffectsManager != null) {
            audioEffectsManager.release();
            audioEffectsManager = null;
        }
        super.onDestroy();
    }
}

