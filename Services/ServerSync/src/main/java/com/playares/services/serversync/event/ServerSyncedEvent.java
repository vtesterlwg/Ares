package com.playares.services.serversync.event;

import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public final class ServerSyncedEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final List<Server> servers;

    public ServerSyncedEvent(List<Server> servers) {
        this.servers = servers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}