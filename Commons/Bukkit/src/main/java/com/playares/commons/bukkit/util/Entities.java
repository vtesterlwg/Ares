package com.playares.commons.bukkit.util;

import com.playares.commons.bukkit.AresPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

public final class Entities {
    public static void addMeta(AresPlugin plugin, Entity entity, String key, String value) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    public static boolean hasMeta(Entity entity, String value) {
        return entity.hasMetadata(value);
    }

    public static boolean isArthropod(EntityType type) {
        switch (type) {
            case SPIDER: return true;
            case CAVE_SPIDER: return true;
            case SILVERFISH: return true;
            case ENDERMITE: return true;
        }

        return false;
    }

    public static boolean isUndead(EntityType type) {
        switch (type) {
            case SKELETON: return true;
            case ZOMBIE: return true;
            case WITHER: return true;
            case PIG_ZOMBIE: return true;
            case ZOMBIE_VILLAGER: return true;
            case ZOMBIE_HORSE: return true;
            case SKELETON_HORSE: return true;
        }

        return false;
    }

    private Entities() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}