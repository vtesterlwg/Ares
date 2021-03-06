package com.playares.factions.claims.shields;

import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.util.Players;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Shield {
    Player getViewer();

    Material getMaterial();

    short getData();

    BLocatable getLocation();

    boolean isDrawn();

    void setDrawn(boolean b);

    default void draw() {
        if (isDrawn()) {
            return;
        }

        Players.sendBlockChange(getViewer(), getLocation().getBukkit().getLocation(), getMaterial(), (byte)getData());
        setDrawn(true);
    }

    default void hide() {
        if (!isDrawn()) {
            return;
        }

        final Block block = getLocation().getBukkit();
        Players.sendBlockChange(getViewer(), block.getLocation(), block.getType(), block.getData());

        setDrawn(false);
    }

    enum ShieldType {
        COMBAT, PROTECTION
    }
}