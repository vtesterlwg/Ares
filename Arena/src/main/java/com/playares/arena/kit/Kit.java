package com.playares.arena.kit;

import com.playares.arena.Arenas;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {
    @Getter public final Arenas plugin;
    @Getter public final String name;
    @Getter public final ItemStack[] contents;
    @Getter public final int enderpearlCooldown;

    public Kit(Arenas plugin, String name, ItemStack[] contents) {
        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.enderpearlCooldown = 16;
    }

    public Kit(Arenas plugin, String name, ItemStack[] contents, int enderpearlCooldown) {
        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.enderpearlCooldown = enderpearlCooldown;
    }

    public void giveKit(Player player) {
        Players.resetHealth(player);
        Players.resetWalkSpeed(player);
        Players.resetFlySpeed(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setContents(contents);

        player.sendMessage(ChatColor.GOLD + "Loaded Kit" + ChatColor.YELLOW + ": " + name);
    }

    public void save() {
        final YamlConfiguration config = plugin.getConfig("arena-kits");

        config.set("kits." + getName() + ".contents", contents);
        config.set("kits." + getName() + ".enderpearl-cooldown", enderpearlCooldown);

        plugin.saveConfig("arena-kits", config);

        Logger.print("Saved Kit: " + name);
    }
}