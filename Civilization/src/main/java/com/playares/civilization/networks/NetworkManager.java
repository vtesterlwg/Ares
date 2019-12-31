package com.playares.civilization.networks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.civilization.CivManager;
import com.playares.civilization.Civilizations;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NetworkManager implements CivManager {
    @Getter public final Civilizations plugin;
    @Getter public final Set<Network> networkRepository;

    public NetworkManager(Civilizations plugin) {
        this.plugin = plugin;
        this.networkRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public String getName() {
        return "Network Manager";
    }

    @Override
    public List<String> getDebug() {
        final List<String> debug = Lists.newArrayList();
        debug.add("Loaded Networks: " + networkRepository.size());
        return debug;
    }

    public void load() {
        networkRepository.addAll(NetworkDAO.get(plugin, plugin.getMongo()));
    }

    public void save() {
        NetworkDAO.save(plugin.getMongo(), networkRepository);
    }

    public void save(Network network) {
        NetworkDAO.save(plugin.getMongo(), network);
    }

    public Network getNetwork(UUID uniqueId) {
        return networkRepository.stream().filter(network -> network.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Network getNetwork(String name) {
        return networkRepository.stream().filter(network -> network.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Collection<Network> getNetworks(Player player) {
        return networkRepository.stream().filter(network -> network.isMember(player)).collect(Collectors.toList());
    }
}