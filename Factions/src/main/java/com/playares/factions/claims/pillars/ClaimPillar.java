package com.playares.factions.claims.pillars;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimPillar implements Pillar {
    @Getter public final Player viewer;
    @Getter public final ClaimPillarType type;
    @Getter public final BLocatable startLocation;
    @Getter public final List<BLocatable> blocks;
    @Getter @Setter public boolean drawn;

    public ClaimPillar(Player viewer, ClaimPillarType type, BLocatable startLocation) {
        this.viewer = viewer;
        this.type = type;
        this.startLocation = startLocation;
        this.blocks = Lists.newArrayList();
        this.drawn = false;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLD_BLOCK;
    }

    public enum ClaimPillarType {
        A, B
    }
}