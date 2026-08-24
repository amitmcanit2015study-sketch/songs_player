package com.amitbharat.songsplayer.data.mediastore;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.data.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaStoreScanner {

    private final Context context;

    public MediaStoreScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Scans local audio files from device MediaStore
     */
    public List<Song> scanDeviceSongs() {
        List<Song> songsList = new ArrayList<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.SIZE
        };

        // Filter out ringtones/notifications and files shorter than 10 seconds
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DURATION + " >= 10000";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    long duration = cursor.getLong(durationCol);
                    long dateAdded = cursor.getLong(dateCol);
                    long size = cursor.getLong(sizeCol);

                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    String dataUri = contentUri.toString();

                    if (dataCol != -1) {
                        String filePath = cursor.getString(dataCol);
                        if (filePath != null && new File(filePath).exists()) {
                            dataUri = filePath;
                        }
                    }

                    Song song = new Song(
                            id,
                            title != null ? title : "Unknown Title",
                            artist != null ? artist : "Unknown Artist",
                            album != null ? album : "Unknown Album",
                            albumId,
                            duration,
                            dataUri,
                            false,
                            null,
                            null,
                            false,
                            0,
                            dateAdded,
                            size
                    );

                    songsList.add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return songsList;
    }

    /**
     * Extracts distinct albums from the song list
     */
    public List<Album> extractAlbums(List<Song> songs) {
        Map<Long, Album> albumMap = new HashMap<>();
        for (Song song : songs) {
            long albumId = song.getAlbumId();
            if (!albumMap.containsKey(albumId)) {
                albumMap.put(albumId, new Album(
                        albumId,
                        song.getAlbum(),
                        song.getArtist(),
                        null,
                        1,
                        0
                ));
            } else {
                Album album = albumMap.get(albumId);
                if (album != null) {
                    album.setTrackCount(album.getTrackCount() + 1);
                }
            }
        }
        List<Album> albums = new ArrayList<>(albumMap.values());
        Collections.sort(albums, (a1, a2) -> a1.getTitle().compareToIgnoreCase(a2.getTitle()));
        return albums;
    }

    /**
     * Extracts distinct artists from the song list
     */
    public List<Artist> extractArtists(List<Song> songs) {
        Map<String, Artist> artistMap = new HashMap<>();
        long idCounter = 1;
        for (Song song : songs) {
            String artistName = song.getArtist();
            if (!artistMap.containsKey(artistName)) {
                artistMap.put(artistName, new Artist(idCounter++, artistName, 1, 1));
            } else {
                Artist artist = artistMap.get(artistName);
                if (artist != null) {
                    artist.setTrackCount(artist.getTrackCount() + 1);
                }
            }
        }
        List<Artist> artists = new ArrayList<>(artistMap.values());
        Collections.sort(artists, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        return artists;
    }

    /**
     * Scans all audio files on device (songs, recordings, whatsapp voice notes, downloads)
     * and groups them folder-wise.
     */
    public List<com.amitbharat.songsplayer.data.model.DeviceFolder> scanDeviceAudioFolders() {
        Map<String, List<Song>> folderSongsMap = new HashMap<>();
        Map<String, String> folderPathMap = new HashMap<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.SIZE
        };

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String displayName = nameCol != -1 ? cursor.getString(nameCol) : null;
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    long duration = cursor.getLong(durationCol);
                    long dateAdded = cursor.getLong(dateCol);
                    long size = cursor.getLong(sizeCol);

                    String filePath = dataCol != -1 ? cursor.getString(dataCol) : null;
                    String finalTitle = displayName != null && !displayName.isEmpty() ? displayName : (title != null ? title : "Audio File");

                    String parentFolderName = "Device Audio";
                    String parentFolderPath = "";

                    if (filePath != null && !filePath.isEmpty()) {
                        File file = new File(filePath);
                        File parent = file.getParentFile();
                        if (parent != null) {
                            parentFolderName = parent.getName();
                            parentFolderPath = parent.getAbsolutePath();
                        }
                    }

                    if (!folderSongsMap.containsKey(parentFolderName)) {
                        folderSongsMap.put(parentFolderName, new ArrayList<>());
                        folderPathMap.put(parentFolderName, parentFolderPath);
                    }

                    Song song = new Song(
                            id,
                            finalTitle,
                            artist != null ? artist : parentFolderName,
                            album != null ? album : parentFolderName,
                            albumId,
                            duration,
                            filePath != null ? filePath : ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString(),
                            false,
                            null,
                            null,
                            false,
                            0,
                            dateAdded,
                            size
                    );

                    folderSongsMap.get(parentFolderName).add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<com.amitbharat.songsplayer.data.model.DeviceFolder> folders = new ArrayList<>();
        for (Map.Entry<String, List<Song>> entry : folderSongsMap.entrySet()) {
            folders.add(new com.amitbharat.songsplayer.data.model.DeviceFolder(entry.getKey(), folderPathMap.get(entry.getKey()), entry.getValue()));
        }

        Collections.sort(folders, (f1, f2) -> f1.getFolderName().compareToIgnoreCase(f2.getFolderName()));
        return folders;
    }
}
