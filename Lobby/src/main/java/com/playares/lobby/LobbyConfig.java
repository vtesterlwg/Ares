package com.playares.lobby;

import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LobbyConfig {
    @Getter public final Lobby plugin;

    @Getter public String databaseURI;
    @Getter public int id;
    @Getter public String bungeeName;
    @Getter public String displayName;
    @Getter public String description;
    @Getter public Server.Type type;

    public LobbyConfig(Lobby plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("config");

        this.databaseURI = config.getString("database");
        this.id = config.getInt("server-data.id");
        this.bungeeName = config.getString("server-data.bungee-name");
        this.displayName = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.display-name"));
        this.description = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.description"));
        this.type = Server.Type.valueOf(config.getString("server-data.type"));
    }
}