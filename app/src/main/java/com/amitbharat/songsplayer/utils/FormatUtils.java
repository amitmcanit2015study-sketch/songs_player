package com.amitbharat.songsplayer.utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class FormatUtils {

    private FormatUtils() {}

    /**
     * Formats duration milliseconds into MM:SS or HH:MM:SS
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            millis = 0;
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
    }

    /**
     * Formats raw byte size into readable MB/KB string
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1.0) {
            return String.format(Locale.getDefault(), "%.1f MB", mb);
        } else {
            double kb = bytes / 1024.0;
            return String.format(Locale.getDefault(), "%.0f KB", kb);
        }
    }

    /**
     * Formats play count string
     */
    public static String formatPlayCount(int count) {
        if (count == 1) {
            return "1 play";
        }
        return count + " plays";
    }
}
