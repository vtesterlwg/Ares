package com.playares.civilization.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.civilization.CivManager;
import com.playares.civilization.Civilizations;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PlayerManager implements CivManager {
    @Getter public final Civilizations plugin;
    @Getter public final Set<CivPlayer> playerRepository;

    public PlayerManager(Civilizations plugin) {
        this.plugin = plugin;
        this.playerRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public String getName() {
        return "Player Manager";
    }

    @Override
    public List<String> getDebug() {
        final List<String> debug = Lists.newArrayList();
        debug.add("Loaded Profiles: " + playerRepository.size());
        return debug;
    }

    public CivPlayer load(UUID uniqueId, String username) {
        CivPlayer profile = CivPlayerDAO.get(plugin, plugin.getMongo(), Filters.eq("id", uniqueId));

        if (profile == null) {
            profile = new CivPlayer(plugin, uniqueId, username);
        }

        return profile;
    }

    public void save(CivPlayer player) {
        CivPlayerDAO.save(plugin.getMongo(), player);
    }

    public void saveAll() {
        final long begin = Time.now();

        Logger.warn("Saving " + playerRepository.size() + " player profiles");

        playerRepository.forEach(profile -> CivPlayerDAO.save(plugin.getMongo(), profile));

        final long end = Time.now();

        Logger.print("Saved all players in the repository in " + (end - begin) + "ms");
    }

    public CivPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public CivPlayer getPlayer(String username) {
        return playerRepository.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}