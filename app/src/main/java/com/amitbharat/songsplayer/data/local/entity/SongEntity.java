package com.amitbharat.songsplayer.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.amitbharat.songsplayer.data.model.Song;

@Entity(tableName = "songs")
public class SongEntity {

    @PrimaryKey
    public long id;
    public String title;
    public String artist;
    public String album;
    public long albumId;
    public long duration;
    public String dataUri;
    public boolean isOnline;
    public String streamUrl;
    public String artUrl;
    public boolean isFavorite;
    public int playCount;
    public long dateAdded;
    public long size;

    public SongEntity() {}

    public static SongEntity fromDomain(Song song) {
        SongEntity entity = new SongEntity();
        entity.id = song.getId();
        entity.title = song.getTitle();
        entity.artist = song.getArtist();
        entity.album = song.getAlbum();
        entity.albumId = song.getAlbumId();
        entity.duration = song.getDuration();
        entity.dataUri = song.getDataUri();
        entity.isOnline = song.isOnline();
        entity.streamUrl = song.getStreamUrl();
        entity.artUrl = song.getArtUrl();
        entity.isFavorite = song.isFavorite();
        entity.playCount = song.getPlayCount();
        entity.dateAdded = song.getDateAdded();
        entity.size = song.getSize();
        return entity;
    }

    public Song toDomain() {
        return new Song(
                id, title, artist, album, albumId, duration,
                dataUri, isOnline, streamUrl, artUrl, isFavorite,
                playCount, dateAdded, size
        );
    }
}
