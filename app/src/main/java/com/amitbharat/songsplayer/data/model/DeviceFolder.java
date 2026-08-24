package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeviceFolder implements Serializable {

    private String folderName;
    private String folderPath;
    private List<Song> songs;
    private boolean isExpanded;

    public DeviceFolder(String folderName, String folderPath, List<Song> songs) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.songs = songs != null ? songs : new ArrayList<>();
        this.isExpanded = false;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public int getSongCount() {
        return songs != null ? songs.size() : 0;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
