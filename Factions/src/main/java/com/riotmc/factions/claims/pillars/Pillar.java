package com.riotmc.factions.claims.pillars;

import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.util.Players;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface Pillar {
    Player getViewer();

    Material getMaterial();

    BLocatable getStartLocation();

    List<BLocatable> getBlocks();

    boolean isDrawn();

    void setDrawn(boolean b);

    default void draw() {
        if (isDrawn()) {
            return;
        }

        final int startY = (int)getStartLocation().getY();
        final int finishY = (startY + 32);
        int y = startY;

        while (y < finishY) {
            final Block block = getStartLocation().getBukkit().getWorld().getBlockAt((int)getStartLocation().getX(), y, (int)getStartLocation().getZ());

            if (block == null || !(block.getType().equals(Material.AIR) || block.getType().equals(Material.CAVE_AIR))) {
                y++;
                continue;
            }

            final BLocatable location = new BLocatable(block);

            if (y % 3 == 0) {
                Players.sendBlockChange(getViewer(), location.getBukkit().getLocation(), getMaterial());
            } else {
                Players.sendBlockChange(getViewer(), location.getBukkit().getLocation(), Material.GLASS);
            }

            getBlocks().add(location);

            y++;
        }

        setDrawn(true);
    }

    default void hide() {
        if (!isDrawn() || getBlocks().isEmpty()) {
            return;
        }

        getBlocks().forEach(location -> {
            final Block block = location.getBukkit();
            Players.sendBlockChange(getViewer(), block.getLocation(), block.getType());
        });

        setDrawn(false);
    }
}