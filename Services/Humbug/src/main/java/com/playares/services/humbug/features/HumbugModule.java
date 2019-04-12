package com.playares.services.humbug.features;

import com.playares.services.humbug.HumbugService;

public interface HumbugModule {
    HumbugService getHumbug();

    String getName();

    boolean isEnabled();

    void loadValues();

    void start();

    void stop();
}
