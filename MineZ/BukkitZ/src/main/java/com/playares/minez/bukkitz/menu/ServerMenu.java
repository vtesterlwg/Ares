package com.playares.minez.bukkitz.menu;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZServer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

public final class ServerMenu extends Menu {
    @Getter public final MineZ mz;
    @Getter public BukkitTask updater;

    public ServerMenu(@Nonnull MineZ plugin, @Nonnull Player player) {
        super(plugin, player, "Server Selector", 2);
        this.mz = plugin;
    }

    @Override
    public void open() {
        super.open();

        updater = new Scheduler(plugin).sync(() -> {
            final List<MZServer> servers = Lists.newArrayList(mz.getServerManager().getServers());

            servers.sort(Comparator.comparing(MZServer::getId));

            clearInventory();

            int pos = 0;

            for (MZServer server : mz.getServerManager().getServers()) {
                final ItemBuilder icon = new ItemBuilder()
                        .setName(ChatColor.GOLD + server.getName() + ChatColor.RESET + " " + ChatColor.YELLOW + "(" + server.getServerStatus().getDisplayName() + ChatColor.YELLOW + ")")
                        .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                        .addFlag(ItemFlag.HIDE_ENCHANTS);

                if (server.getServerStatus().equals(MZServer.MZServerStatus.ONLINE)) {
                    icon.setMaterial(Material.LIME_CONCRETE);
                    icon.addLore(ChatColor.YELLOW + "" + server.getOnlineAmount() + ChatColor.GOLD + " currently playing");

                    if (server.isPvE() || server.isPremiumOnly()) {
                        icon.addLore(ChatColor.RESET + " ");

                        if (server.isPvE()) {
                            icon.addLore(ChatColor.GOLD + "PvE Only");
                        }

                        if (server.isPremiumOnly()) {
                            icon.addLore(ChatColor.GOLD + "Premium Members Only");
                            icon.addEnchant(Enchantment.LOYALTY, 1);
                        }
                    }
                }

                if (server.getServerStatus().equals(MZServer.MZServerStatus.WHITELISTED)) {
                    icon.setMaterial(Material.RED_CONCRETE);
                    icon.addLore(ChatColor.RED + "Server is currently whitelisted");
                }

                if (server.getServerStatus().equals(MZServer.MZServerStatus.OFFLINE)) {
                    icon.setMaterial(Material.RED_CONCRETE);
                    icon.addLore(ChatColor.DARK_RED + "Server is offline");
                }

                addItem(new ClickableItem(icon.build(), pos, click -> {
                    final ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    out.writeUTF("Connect");
                    out.writeUTF(server.getBungeeName());

                    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                    player.sendMessage(ChatColor.YELLOW + "Attempting connection to " + ChatColor.GOLD + server.getName());

                    player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                }));

                pos++;
            }
        }).repeat(0L, 3 * 20L).run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (updater != null && !updater.isCancelled()) {
            updater.cancel();
            updater = null;
        }
    }
}
