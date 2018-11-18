package com.riotmc.services.essentials.vanish;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class VanishManager {
    @Getter
    public final Set<UUID> vanished;

    public VanishManager() {
        this.vanished = Sets.newConcurrentHashSet();
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean shouldSee(Player viewer, Player seen) {
        if (!isVanished(seen)) {
            return true;
        }

        return viewer.hasPermission("essentials.vanish");
    }
}
