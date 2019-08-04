package com.playares.factions.claims.pillars;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class MapPillar implements Pillar {
    @Getter public final Player viewer;
    @Getter public final BLocatable startLocation;
    @Getter public final List<BLocatable> blocks;
    @Getter public final Material material;
    @Getter @Setter public boolean drawn;

    public MapPillar(Player viewer, BLocatable startLocation, Material material) {
        this.viewer = viewer;
        this.startLocation = startLocation;
        this.blocks = Lists.newArrayList();
        this.material = material;
        this.drawn = false;
    }
}