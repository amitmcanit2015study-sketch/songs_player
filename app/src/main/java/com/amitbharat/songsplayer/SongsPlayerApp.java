package com.amitbharat.songsplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.google.android.material.color.DynamicColors;

public class SongsPlayerApp extends Application {

    public static final String PLAYBACK_CHANNEL_ID = "playback_channel";
    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";
    private static SongsPlayerApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Apply Material You Dynamic Colors across all activities if supported (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Create Notification Channels for Playback & Downloads (Android 8.0+)
        createNotificationChannels();
    }

    public static SongsPlayerApp getInstance() {
        return instance;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                // Playback notification channel with public lockscreen visibility
                NotificationChannel playbackChannel = new NotificationChannel(
                        PLAYBACK_CHANNEL_ID,
                        "Music Playback",
                        NotificationManager.IMPORTANCE_LOW
                );
                playbackChannel.setDescription("Shows active playback controls and song info");
                playbackChannel.setShowBadge(true);
                playbackChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                playbackChannel.setSound(null, null);
                playbackChannel.enableVibration(false);
                manager.createNotificationChannel(playbackChannel);

                // Download notification channel
                NotificationChannel downloadChannel = new NotificationChannel(
                        DOWNLOAD_CHANNEL_ID,
                        "Song Downloads",
                        NotificationManager.IMPORTANCE_LOW
                );
                downloadChannel.setDescription("Shows background song download progress");
                downloadChannel.setShowBadge(false);
                downloadChannel.setSound(null, null);
                manager.createNotificationChannel(downloadChannel);
            }
        }
    }
}
