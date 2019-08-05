package com.playares.factions.addons.mining;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

@AllArgsConstructor
public final class Findable {
    @Getter public final Material type;
    @Getter public final Material foundType;
    @Getter public final double rate;
    @Getter public final World.Environment environment;
    @Getter public final int minHeight;
    @Getter public final int maxHeight;
    @Getter public final int minVeinSize;
    @Getter public final int maxVeinSize;
    @Getter public final boolean broadcast;
    @Getter public final boolean message;
    @Getter public final ChatColor color;

    public String getName() {
        return StringUtils.capitaliseAllWords(type.name().toLowerCase().replace("_", " "));
    }

    public boolean isObtainable(Block block) {
        if (!block.getType().equals(foundType)) {
            return false;
        }

        if (!block.getWorld().getEnvironment().equals(environment)) {
            return false;
        }

        if (block.getY() <= minHeight || block.getY() >= maxHeight) {
            return false;
        }

        return true;
    }
}