package com.playares.services.serversync;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.serversync.data.Server;
import com.playares.services.serversync.data.ServerDAO;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public final class ServerSyncService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public final Server thisServer;
    @Getter @Setter public List<Server> servers;
    @Getter @Setter public BukkitTask updater;

    public ServerSyncService(AresPlugin owner, Server server) {
        this.owner = owner;
        this.thisServer = server;
    }

    @Override
    public void start() {
        servers = Lists.newCopyOnWriteArrayList();

        updater = new Scheduler(getOwner()).async(() -> {
            push();
            pull();
        }).repeat(0L, 5 * 20L).run();
    }

    @Override
    public void stop() {
        thisServer.setOnlineCount(0);
        thisServer.setStatus(Server.Status.OFFLINE);
        ServerDAO.saveServer(getOwner().getMongo(), thisServer);

        servers.clear();
        servers = null;

        if (updater != null && !updater.isCancelled()) {
            updater.cancel();
            updater = null;
        }
    }

    @Override
    public String getName() {
        return "Server Sync";
    }

    private void push() {
        thisServer.setStatus((Bukkit.getServer().hasWhitelist()) ? Server.Status.WHITELISTED : Server.Status.ONLINE);
        thisServer.setOnlineCount(Bukkit.getOnlinePlayers().size());

        new Scheduler(getOwner()).async(() -> ServerDAO.saveServer(getOwner().getMongo(), thisServer)).run();
    }

    private void pull() {
        new Scheduler(getOwner()).async(() -> {
            servers.clear();
            setServers(ServerDAO.getServers(getOwner().getMongo()));
        }).run();
    }

    public Server getServerByBungeeName(String name) {
        return getServers().stream().filter(server -> server.getBungeeName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ImmutableList<Server> getServersByType(Server.Type type) {
        return ImmutableList.copyOf(getServers().stream().filter(server -> server.getType().equals(type)).collect(Collectors.toList()));
    }

    public ImmutableList<Server> getServersByStatus(Server.Status status) {
        return ImmutableList.copyOf(getServers().stream().filter(server -> server.getStatus().equals(status)).collect(Collectors.toList()));
    }
}