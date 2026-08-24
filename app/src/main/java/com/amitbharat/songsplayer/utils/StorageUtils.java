package com.amitbharat.songsplayer.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public final class StorageUtils {

    private StorageUtils() {}

    /**
     * Gets or creates the application directory for offline music downloads
     */
    public static File getMusicDownloadDir(Context context) {
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (downloadDir == null) {
            downloadDir = new File(context.getFilesDir(), "music_downloads");
        }
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir;
    }

    /**
     * Deletes a local audio file safely
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null) return false;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception ignored) {}
        return false;
    }
}
