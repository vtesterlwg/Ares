package com.riotmc.factions.claims.shields;

import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.util.Players;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Shield {
    Player getViewer();

    Material getMaterial();

    BLocatable getLocation();

    boolean isDrawn();

    void setDrawn(boolean b);

    default void draw() {
        if (isDrawn()) {
            return;
        }

        Players.sendBlockChange(getViewer(), getLocation().getBukkit().getLocation(), getMaterial());
        setDrawn(true);
    }

    default void hide() {
        if (!isDrawn()) {
            return;
        }

        final Block block = getLocation().getBukkit();
        Players.sendBlockChange(getViewer(), block.getLocation(), block.getType());

        setDrawn(false);
    }

    enum ShieldType {
        COMBAT, PROTECTION
    }
}