package com.playares.arena.kit;

import com.playares.arena.Arenas;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {
    @Getter public final Arenas plugin;
    @Getter public final String name;
    @Getter public final ItemStack[] contents;
    @Getter public final ItemStack[] armor;
    @Getter public final int enderpearlCooldown;

    public Kit(Arenas plugin, String name, ItemStack[] contents, ItemStack[] armor) {
        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.armor = armor;
        this.enderpearlCooldown = 16;
    }

    public Kit(Arenas plugin, String name, ItemStack[] contents, ItemStack[] armor, int enderpearlCooldown) {
        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.armor = armor;
        this.enderpearlCooldown = enderpearlCooldown;
    }

    public void giveKit(Player player) {
        Players.resetHealth(player);
        Players.resetWalkSpeed(player);
        Players.resetFlySpeed(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armor);

        player.sendMessage(ChatColor.GOLD + "Loaded Kit" + ChatColor.YELLOW + ": " + name);
    }

    public ItemStack getBook() {
        return new ItemBuilder()
                .setMaterial(Material.ENCHANTED_BOOK)
                .setName(ChatColor.YELLOW + "Load Kit: " + ChatColor.AQUA + name)
                .build();
    }

    public void save() {
        final YamlConfiguration config = plugin.getConfig("arena-kits");

        config.set("kits." + getName() + ".contents", contents);
        config.set("kits." + getName() + ".armor", armor);
        config.set("kits." + getName() + ".enderpearl-cooldown", enderpearlCooldown);

        if (this instanceof ClassKit) {
            final ClassKit classKit = (ClassKit)this;
            config.set("kits." + getName() + ".class", classKit.getAttachedClass().getName());
        }

        plugin.saveConfig("arena-kits", config);

        Logger.print("Saved Kit: " + name);
    }
}