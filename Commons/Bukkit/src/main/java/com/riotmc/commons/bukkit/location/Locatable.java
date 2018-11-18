package com.riotmc.commons.bukkit.location;

import javax.annotation.Nonnull;

public interface Locatable {
    @Nonnull
    String getWorldName();

    double getX();

    double getY();

    double getZ();

    /**
     * Returns true if the provided location is within the provided distance of this location
     * @param location Location
     * @param distance Max Distance
     * @return True if within provided distance
     */
    default boolean nearby(Locatable location, double distance) {
        if (!location.getWorldName().equalsIgnoreCase(getWorldName())) {
            return false;
        }

        return distance(location) <= distance;
    }

    /**
     * Returns the distance from the provided location
     * @param location Location
     * @return Distance (in blocks)
     */
    default double distance(Locatable location) {
        if (!location.getWorldName().equalsIgnoreCase(getWorldName())) {
            return -1.0;
        }

        return Math.sqrt(
                Math.pow(getX() - location.getX(), 2) +
                Math.pow(getY() - location.getY(), 2) +
                Math.pow(getZ() - location.getZ(), 2));
    }
}