package com.playares.minez.bukkitz.data.manager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZServer;
import com.playares.minez.bukkitz.data.dao.ServerDAO;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.stream.Collectors;

public final class ServerManager {
    @Getter public final MineZ plugin;
    @Getter public final MZServer thisServer;
    @Getter public final Set<MZServer> servers;
    @Getter public BukkitTask updater;

    public ServerManager(MineZ plugin) {
        this.plugin = plugin;
        this.thisServer = new MZServer(plugin.getMZConfig().getServerId(), plugin.getMZConfig().getBungeeName());
        this.servers = Sets.newConcurrentHashSet();

        this.updater = new Scheduler(plugin).sync(() -> {
            final boolean whitelisted = Bukkit.getServer().hasWhitelist();
            final int onlineCount = Bukkit.getServer().getOnlinePlayers().size();

            new Scheduler(plugin).async(() -> {
                ImmutableSet<MZServer> pulledServers = ServerDAO.pullServers(thisServer.getId(), plugin.getMongo());

                thisServer.setBungeeName(plugin.getMZConfig().getBungeeName());
                thisServer.setServerStatus(whitelisted ? MZServer.MZServerStatus.WHITELISTED : MZServer.MZServerStatus.ONLINE);
                thisServer.setOnlineAmount(onlineCount);
                thisServer.setPvE(plugin.getMZConfig().isPvE());
                thisServer.setPremiumOnly(plugin.getMZConfig().isPremiumOnly());

                ServerDAO.pushServer(plugin.getMongo(), thisServer);

                if (pulledServers != null) {
                    pulledServers.forEach(pulledServer -> {
                        final MZServer existingServer = getServer(pulledServer.getId());

                        if (existingServer != null) {
                            existingServer.setBungeeName(pulledServer.getBungeeName());
                            existingServer.setServerStatus(pulledServer.getServerStatus());
                            existingServer.setOnlineAmount(pulledServer.getOnlineAmount());
                            existingServer.setPvE(pulledServer.isPvE());
                            existingServer.setPremiumOnly(pulledServer.isPremiumOnly());
                        } else {
                            servers.add(pulledServer);
                        }
                    });
                }
            }).run();
        }).repeat(0L, 5 * 20L).run();
    }

    public void closeServer() {
        thisServer.setOnlineAmount(0);
        thisServer.setServerStatus(MZServer.MZServerStatus.OFFLINE);

        ServerDAO.pushServer(plugin.getMongo(), thisServer);

        if (updater != null && !updater.isCancelled()) {
            updater.cancel();
            this.updater = null;
        }
    }

    public MZServer getServer(int serverId) {
        return servers.stream().filter(server -> server.getId() == serverId).findFirst().orElse(null);
    }

    public ImmutableSet<MZServer> getServerByStatus(MZServer.MZServerStatus status) {
        return ImmutableSet.copyOf(servers.stream().filter(server -> server.getServerStatus().equals(status)).collect(Collectors.toSet()));
    }
}