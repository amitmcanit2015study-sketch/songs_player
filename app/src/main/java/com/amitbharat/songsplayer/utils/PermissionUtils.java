package com.amitbharat.songsplayer.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public final class PermissionUtils {

    private PermissionUtils() {}

    /**
     * Checks if the app has audio media read permissions based on Android version
     */
    public static boolean hasAudioPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Checks if notification permission is granted (Android 13+)
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Returns all permissions that are not yet granted
     */
    public static String[] getRequiredPermissions(Context context) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasAudioPermission(context)) {
                list.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
            if (!hasNotificationPermission(context)) {
                list.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            if (!hasAudioPermission(context)) {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        return list.toArray(new String[0]);
    }
}
