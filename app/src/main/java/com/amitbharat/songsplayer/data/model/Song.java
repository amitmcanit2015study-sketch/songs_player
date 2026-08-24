package com.amitbharat.songsplayer.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Song implements Parcelable {

    private long id;
    private String title;
    private String artist;
    private String album;
    private long albumId;
    private long duration;
    private String dataUri;
    private boolean isOnline;
    private String streamUrl;
    private String artUrl;
    private boolean isFavorite;
    private int playCount;
    private long dateAdded;
    private long size;

    public Song() {}

    public Song(long id, String title, String artist, String album, long albumId,
                long duration, String dataUri, boolean isOnline, String streamUrl,
                String artUrl, boolean isFavorite, int playCount, long dateAdded, long size) {
        this.id = id;
        this.title = title != null ? title : "Unknown Title";
        this.artist = artist != null ? artist : "Unknown Artist";
        this.album = album != null ? album : "Unknown Album";
        this.albumId = albumId;
        this.duration = duration;
        this.dataUri = dataUri;
        this.isOnline = isOnline;
        this.streamUrl = streamUrl;
        this.artUrl = artUrl;
        this.isFavorite = isFavorite;
        this.playCount = playCount;
        this.dateAdded = dateAdded;
        this.size = size;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        albumId = in.readLong();
        duration = in.readLong();
        dataUri = in.readString();
        isOnline = in.readByte() != 0;
        streamUrl = in.readString();
        artUrl = in.readString();
        isFavorite = in.readByte() != 0;
        playCount = in.readInt();
        dateAdded = in.readLong();
        size = in.readLong();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public long getAlbumId() { return albumId; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getDataUri() { return dataUri; }
    public void setDataUri(String dataUri) { this.dataUri = dataUri; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getArtUrl() { return artUrl; }
    public void setArtUrl(String artUrl) { this.artUrl = artUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    /**
     * Gets effective playable URI (dataUri or streamUrl)
     */
    public String getPlayableUri() {
        if (dataUri != null && !dataUri.isEmpty()) {
            return dataUri;
        }
        return streamUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(albumId);
        dest.writeLong(duration);
        dest.writeString(dataUri);
        dest.writeByte((byte) (isOnline ? 1 : 0));
        dest.writeString(streamUrl);
        dest.writeString(artUrl);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeInt(playCount);
        dest.writeLong(dateAdded);
        dest.writeLong(size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id && Objects.equals(title, song.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
