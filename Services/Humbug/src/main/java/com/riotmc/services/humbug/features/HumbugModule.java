package com.riotmc.services.humbug.features;

import com.riotmc.services.humbug.HumbugService;

public interface HumbugModule {
    HumbugService getHumbug();

    String getName();

    boolean isEnabled();

    void loadValues();

    void start();

    void stop();
}
