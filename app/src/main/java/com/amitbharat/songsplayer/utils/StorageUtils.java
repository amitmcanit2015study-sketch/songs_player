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

    /**
     * Copies file from source to destination
     */
    public static boolean copyFile(File source, File destination) {
        if (source == null || !source.exists() || destination == null) return false;
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new java.io.FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
