package com.playares.factions.claims.shields;

import com.playares.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ProtectionShield implements Shield {
    @Getter public final Player viewer;
    @Getter public final Material material;
    @Getter public final short data;
    @Getter public final BLocatable location;
    @Getter @Setter
    public boolean drawn;

    public ProtectionShield(Player viewer, BLocatable location) {
        this.viewer = viewer;
        this.material = Material.STAINED_GLASS;
        this.data = (short)11;
        this.location = location;
        this.drawn = false;
    }
}
