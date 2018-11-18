package com.riotmc.arena.stats;

public interface ArcherStatisticHolder {
    double getLongestShot();

    int getArrowHits();

    int getTotalArrowsFired();

    double getAccuracy();

    void setLongestShot(double distance);
}
