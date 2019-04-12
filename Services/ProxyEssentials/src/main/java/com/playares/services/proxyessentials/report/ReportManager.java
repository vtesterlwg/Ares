package com.playares.services.proxyessentials.report;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;
import java.util.UUID;

public final class ReportManager {
    @Getter
    public final Set<UUID> recentReports;

    public ReportManager() {
        this.recentReports = Sets.newConcurrentHashSet();
    }

    public boolean hasRecentlyReported(ProxiedPlayer player) {
        return recentReports.contains(player.getUniqueId());
    }
}
