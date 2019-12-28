package com.playares.civilization.addons;

public interface CivAddon {
    String getName();

    void prepare();

    void start();

    void stop();
}
