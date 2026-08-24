package com.amitbharat.songsplayer.service;

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
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.repository.MusicRepository;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaybackService extends MediaSessionService {

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

        // Configure ExoPlayer with robust HTTP DataSource for YouTube audio streams
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(25000);

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(httpDataSourceFactory);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
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

        // Configure MediaSession
        Intent sessionIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, sessionIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent)
                .build();

        // Configure Media Notification Provider with playback controls
        setMediaNotificationProvider(new androidx.media3.session.DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(com.amitbharat.songsplayer.SongsPlayerApp.PLAYBACK_CHANNEL_ID)
                .setChannelName(R.string.playback_notification_channel_name)
                .build());

        // Attach audio effects to audio session
        audioEffectsManager.attachAudioSession(player.getAudioSessionId());

        // Connect Sleep Timer Listener
        SleepTimerManager.getInstance().setListener(this::pause);

        // Player Event Listener
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                isPlayingLive.postValue(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    playbackRetryCount = 0;
                    durationLive.postValue(player.getDuration());
                } else if (playbackState == Player.STATE_ENDED) {
                    if (hasSong()) {
                        musicRepository.recordPlayback(songQueue.get(currentQueueIndex), player.getDuration());
                    }
                    playNext();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                if (hasSong() && playbackRetryCount < 2) {
                    playbackRetryCount++;
                    playCurrentIndex();
                }
            }

            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                audioEffectsManager.attachAudioSession(audioSessionId);
            }
        });
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

    public void playSongList(List<Song> songs, int startIndex) {
        if (songs == null || songs.isEmpty()) return;

        songQueue.clear();
        songQueue.addAll(songs);
        queueLive.postValue(new ArrayList<>(songQueue));

        if (startIndex < 0 || startIndex >= songQueue.size()) {
            startIndex = 0;
        }

        currentQueueIndex = startIndex;
        currentQueueIndexLive.postValue(currentQueueIndex);
        playbackRetryCount = 0;

        playCurrentIndex();
    }

    public void playSong(Song song) {
        if (song == null) return;
        List<Song> singleList = new ArrayList<>();
        singleList.add(song);
        playSongList(singleList, 0);
    }

    private void playCurrentIndex() {
        if (currentQueueIndex >= 0 && currentQueueIndex < songQueue.size()) {
            Song song = songQueue.get(currentQueueIndex);
            currentSongLive.postValue(song);

            String uriString = song.getPlayableUri();
            playWithUrl(song, uriString);
        }
    }

    private void playWithUrl(Song song, String uriString) {
        if (uriString != null && !uriString.isEmpty()) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(song.getTitle())
                    .setArtist(song.getArtist())
                    .setAlbumTitle(song.getAlbum())
                    .setArtworkUri(song.getArtUrl() != null ? Uri.parse(song.getArtUrl()) : null)
                    .build();

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(uriString))
                    .setMediaMetadata(metadata)
                    .build();

            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            isPlayingLive.postValue(true);
        }
    }

    public void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    public void play() {
        player.play();
    }

    public void pause() {
        player.pause();
    }

    public void playNext() {
        if (songQueue.isEmpty()) return;

        if (player.getRepeatMode() == Player.REPEAT_MODE_ONE) {
            player.seekTo(0);
            player.play();
            return;
        }

        if (currentQueueIndex + 1 < songQueue.size()) {
            currentQueueIndex++;
            currentQueueIndexLive.postValue(currentQueueIndex);
            playbackRetryCount = 0;
            playCurrentIndex();
        } else if (player.getRepeatMode() == Player.REPEAT_MODE_ALL) {
            currentQueueIndex = 0;
            currentQueueIndexLive.postValue(currentQueueIndex);
            playbackRetryCount = 0;
            playCurrentIndex();
        }
    }

    public void playPrevious() {
        if (songQueue.isEmpty()) return;

        if (player.getCurrentPosition() > 3000) {
            player.seekTo(0);
            return;
        }

        if (currentQueueIndex - 1 >= 0) {
            currentQueueIndex--;
            currentQueueIndexLive.postValue(currentQueueIndex);
            playbackRetryCount = 0;
            playCurrentIndex();
        } else {
            player.seekTo(0);
        }
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    public void toggleShuffle() {
        boolean newShuffle = !player.getShuffleModeEnabled();
        player.setShuffleModeEnabled(newShuffle);
        shuffleModeLive.postValue(newShuffle);
        prefs.edit().putBoolean(Constants.KEY_SHUFFLE_MODE, newShuffle).apply();
    }

    public void toggleRepeatMode() {
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
        player.setPlaybackParameters(new PlaybackParameters(speed));
        playbackSpeedLive.postValue(speed);
        prefs.edit().putFloat(Constants.KEY_PLAYBACK_SPEED, speed).apply();
    }

    public void addToQueue(Song song) {
        if (song == null) return;
        songQueue.add(song);
        queueLive.postValue(new ArrayList<>(songQueue));
    }

    public void playNextInQueue(Song song) {
        if (song == null) return;
        if (currentQueueIndex >= 0 && currentQueueIndex < songQueue.size()) {
            songQueue.add(currentQueueIndex + 1, song);
        } else {
            songQueue.add(song);
        }
        queueLive.postValue(new ArrayList<>(songQueue));
    }

    public void removeFromQueue(int position) {
        if (position >= 0 && position < songQueue.size()) {
            songQueue.remove(position);
            if (position < currentQueueIndex) {
                currentQueueIndex--;
            } else if (position == currentQueueIndex && !songQueue.isEmpty()) {
                if (currentQueueIndex >= songQueue.size()) {
                    currentQueueIndex = 0;
                }
                playbackRetryCount = 0;
                playCurrentIndex();
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
        player.stop();
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
