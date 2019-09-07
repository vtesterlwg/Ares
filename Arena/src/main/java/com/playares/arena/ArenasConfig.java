package com.playares.arena;

import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ArenasConfig {
    @Getter public final Arenas plugin;

    @Getter public String databaseURI;

    @Getter public int serverId;
    @Getter public String bungeeName;
    @Getter public String displayName;
    @Getter public String description;
    @Getter public Server.Type serverType;
    @Getter public int premiumAllocatedSlots;

    @Getter public int maxTeamSize;
    @Getter public int timerInviteExpire;
    @Getter public int timerChallengeExpire;
    @Getter public int timerAftermatchReportExpire;

    public ArenasConfig(Arenas plugin) {
        this.plugin = plugin;
    }

    public void loadValues() {
        final YamlConfiguration config = plugin.getConfig("config");

        databaseURI = config.getString("database");

        serverId = config.getInt("server-sync.id");
        bungeeName = config.getString("server-sync.bungee-name");
        displayName = config.getString("server-sync.display-name");
        serverType = Server.Type.valueOf(config.getString("server-sync.type"));
        premiumAllocatedSlots = config.getInt("server-sync.premium-allocated-slots");

        maxTeamSize = config.getInt("limits.team-size");
        timerInviteExpire = config.getInt("timers.invite-expire");
        timerChallengeExpire = config.getInt("timers.challenge-expire");
        timerAftermatchReportExpire = config.getInt("timers.report-expire");
    }
}
