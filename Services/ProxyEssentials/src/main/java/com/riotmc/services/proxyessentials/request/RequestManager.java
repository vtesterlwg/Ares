package com.riotmc.services.proxyessentials.request;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;
import java.util.UUID;

public final class RequestManager {
    @Getter
    public final Set<UUID> recentRequests;

    public RequestManager() {
        this.recentRequests = Sets.newConcurrentHashSet();
    }

    public boolean hasRecentlyRequested(ProxiedPlayer player) {
        return recentRequests.contains(player.getUniqueId());
    }
}
