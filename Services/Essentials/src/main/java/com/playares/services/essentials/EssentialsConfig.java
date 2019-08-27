package com.playares.services.essentials;

import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public final class EssentialsConfig {
    @Getter public final EssentialsService service;

    @Getter @Setter public String globalBroadcastPrefix;
    @Getter @Setter public String playerBroadcastPrefix;

    public EssentialsConfig(EssentialsService service) {
        this.service = service;
    }

    public void load() {
        final YamlConfiguration config = getService().getOwner().getConfig("essentials");

        if (config == null) {
            Logger.error("Failed to find essentials.yml file");
            return;
        }

        globalBroadcastPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("broadcasts.global-prefix"));
        playerBroadcastPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("broadcasts.player-prefix-format"));
    }
}
