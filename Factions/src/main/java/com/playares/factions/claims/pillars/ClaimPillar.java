package com.playares.factions.claims.pillars;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimPillar implements Pillar {
    @Getter
    public final Player viewer;

    @Getter
    public final BLocatable startLocation;

    @Getter @Setter
    public boolean drawn;

    public ClaimPillar(Player viewer, BLocatable startLocation) {
        this.viewer = viewer;
        this.startLocation = startLocation;
        this.drawn = false;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLD_BLOCK;
    }

    @Override
    public List<BLocatable> getBlocks() {
        return Lists.newArrayList();
    }
}