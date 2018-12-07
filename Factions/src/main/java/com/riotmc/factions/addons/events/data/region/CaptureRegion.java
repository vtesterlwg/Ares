package com.riotmc.factions.addons.events.data.region;

import com.google.common.base.Preconditions;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.location.Locatable;
import com.riotmc.commons.bukkit.location.PLocatable;
import lombok.Getter;

public final class CaptureRegion {
    @Getter public final BLocatable cornerA;
    @Getter public final BLocatable cornerB;
    @Getter public final String worldName;

    public CaptureRegion(BLocatable cornerA, BLocatable cornerB) {
        Preconditions.checkArgument(!cornerA.getWorldName().equals(cornerB.getWorldName()), "Corner worlds do not match");
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.worldName = cornerA.getWorldName();
    }

    private double getMinX() {
        return Math.min(cornerA.getX(), cornerB.getX());
    }

    private double getMinY() {
        return Math.min(cornerA.getY(), cornerB.getY());
    }

    private double getMinZ() {
        return Math.min(cornerA.getZ(), cornerB.getZ());
    }

    private double getMaxX() {
        return Math.max(cornerA.getX(), cornerB.getX());
    }

    private double getMaxY() {
        return Math.max(cornerA.getY(), cornerB.getY());
    }

    private double getMaxZ() {
        return Math.max(cornerA.getZ(), cornerB.getZ());
    }

    public boolean inside(Locatable location) {
        if (!location.getWorldName().equals(worldName)) {
            return false;
        }

        double xMin = getMinX();
        double xMax = getMaxX();

        double yMin = getMinY();
        double yMax = getMaxY();

        double zMin = getMinZ();
        double zMax = getMaxZ();

        if (location instanceof PLocatable) {
            xMax++;
            zMax++;
        }

        return
                location.getX() >= xMin && location.getX() <= xMax &&
                        location.getY() >= yMin && location.getY() <= yMax &&
                        location.getZ() >= zMin && location.getZ() <= zMax;
    }
}
