package com.playares.factions.addons;

public interface Addon {
    String getName();

    void prepare();

    void start();

    void stop();
}
