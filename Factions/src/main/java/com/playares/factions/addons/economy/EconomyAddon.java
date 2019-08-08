package com.playares.factions.addons.economy;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.economy.command.EconomyCommand;
import com.playares.factions.addons.economy.data.EconomyDataHandler;
import com.playares.factions.addons.economy.shop.ShopHandler;
import com.playares.factions.addons.economy.shop.ShopListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class EconomyAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public final EconomyDataHandler handler;
    @Getter public final ShopHandler shopHandler;

    @Getter @Setter public boolean enabled;
    @Getter @Setter public double startingBalance;
    @Getter @Setter public double minPayAmount;

    public EconomyAddon(Factions plugin) {
        this.plugin = plugin;
        this.handler = new EconomyDataHandler(this);
        this.shopHandler = new ShopHandler(this);
    }

    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.enabled = config.getBoolean("economy.enabled");
        this.startingBalance = config.getDouble("economy.starting-balance");
        this.minPayAmount = config.getDouble("economy.min-pay-amount");
    }

    @Override
    public void start() {
        plugin.registerCommand(new EconomyCommand(this));

        plugin.registerListener(new ShopListener(this));
    }

    @Override
    public void stop() {}
}