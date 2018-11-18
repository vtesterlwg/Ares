package com.riotmc.factions.claims.shields;

import com.riotmc.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class CombatShield implements Shield {
    @Getter public final Player viewer;
    @Getter public final Material material;
    @Getter public final BLocatable location;
    @Getter @Setter public boolean drawn;

    public CombatShield(Player viewer, BLocatable location) {
        this.viewer = viewer;
        this.material = Material.RED_STAINED_GLASS;
        this.location = location;
        this.drawn = false;
    }
}