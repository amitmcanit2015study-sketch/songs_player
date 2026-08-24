package com.amitbharat.songsplayer.data.model;

import java.io.Serializable;

public class EqualizerPreset implements Serializable {

    private String name;
    private short[] bandLevels;
    private short bassBoost;
    private short virtualizer;

    public EqualizerPreset(String name, short[] bandLevels, short bassBoost, short virtualizer) {
        this.name = name;
        this.bandLevels = bandLevels;
        this.bassBoost = bassBoost;
        this.virtualizer = virtualizer;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public short[] getBandLevels() { return bandLevels; }
    public void setBandLevels(short[] bandLevels) { this.bandLevels = bandLevels; }

    public short getBassBoost() { return bassBoost; }
    public void setBassBoost(short bassBoost) { this.bassBoost = bassBoost; }

    public short getVirtualizer() { return virtualizer; }
    public void setVirtualizer(short virtualizer) { this.virtualizer = virtualizer; }

    @Override
    public String toString() {
        return name;
    }
}
