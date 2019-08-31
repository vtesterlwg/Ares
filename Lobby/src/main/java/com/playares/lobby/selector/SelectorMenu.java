package com.playares.lobby.selector;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.lobby.Lobby;
import com.playares.lobby.queue.ServerQueue;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.dao.DeathbanDAO;
import com.playares.services.deathban.data.Deathban;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.Rank;
import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public final class SelectorMenu extends Menu {
    @Getter public final Lobby lobby;
    @Getter public BukkitTask updater;

    public SelectorMenu(Lobby plugin, Player player) {
        super(plugin, player, "Server Selector", 6);
        this.lobby = plugin;
    }

    private void update() {
        final UUID uniqueId = player.getUniqueId();
        final Server factions = lobby.getQueueManager().getServerQueues().keySet().stream().filter(server -> server.getType() != null && server.getType().equals(Server.Type.FACTION)).findFirst().orElse(null);
        final Server development = lobby.getQueueManager().getServerQueues().keySet().stream().filter(server -> server.getType() != null && server.getType().equals(Server.Type.DEV)).findFirst().orElse(null);
        final Server arena = lobby.getQueueManager().getServerQueues().keySet().stream().filter(server -> server.getType() != null && server.getType().equals(Server.Type.ARENA)).findFirst().orElse(null);

        if (factions != null) {
            final DeathbanService deathbanService = (DeathbanService)getLobby().getService(DeathbanService.class);
            final ServerQueue queue = getLobby().getQueueManager().getQueue(factions);
            final List<String> lore = Lists.newArrayList();

            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");
            lore.add(factions.getDescription());
            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");

            lore.add(ChatColor.GRAY + "HCFRevival is a Hardcore Factions");
            lore.add(ChatColor.GRAY + "server designed to provide a challenge");
            lore.add(ChatColor.GRAY + "to players that think they have what it");
            lore.add(ChatColor.GRAY + "takes to compete against others in a difficult");
            lore.add(ChatColor.GRAY + "and unforgiving environment.");
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + factions.getStatus().getDisplayName());
            lore.add(ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + factions.getOnlineCount());
            lore.add(ChatColor.GOLD + "Queue" + ChatColor.YELLOW + ": " + queue.getQueue().size());
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GREEN + "Click to join!");

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.DIAMOND_HELMET)
                    .setName(factions.getDisplayName())
                    .addLore(lore)
                    .build();

            addItem(new ClickableItem(icon, 0, click -> new Scheduler(getLobby()).async(() -> {
                final Deathban deathban = DeathbanDAO.getDeathban(getLobby().getMongo(), uniqueId);

                new Scheduler(getLobby()).sync(() -> {
                    if (deathbanService != null && deathban != null) {
                        player.sendMessage(deathbanService.getDeathbanKickMessage(deathban));
                        player.closeInventory();
                        return;
                    }

                    final RankService rankService = (RankService)getLobby().getService(RankService.class);
                    final Rank rank;

                    if (rankService != null) {
                        rank = rankService.getHighestRank(player);
                    } else {
                        rank = null;
                    }

                    queue.add(player.getUniqueId(), rank);

                    player.sendMessage("Adding you to the " + factions.getDisplayName() + ChatColor.RESET + " queue...");
                    player.sendMessage(ChatColor.AQUA + "You are now " + ChatColor.YELLOW + "#" + queue.getPosition(player.getUniqueId()) + ChatColor.AQUA + " in queue to join " + factions.getDisplayName());
                    player.closeInventory();
                }).run();
            }).run()));
        }

        if (development != null) {
            final List<String> lore = Lists.newArrayList();

            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");
            lore.add(development.getDescription());
            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");

            lore.add(ChatColor.GRAY + "Development is our public testing");
            lore.add(ChatColor.GRAY + "server which allows the community");
            lore.add(ChatColor.GRAY + "to test new features before they hit");
            lore.add(ChatColor.GRAY + "live servers.");
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + development.getStatus().getDisplayName());
            lore.add(ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + development.getOnlineCount());
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GREEN + "Click to join!");

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.DIAMOND_HELMET)
                    .setName(development.getDisplayName())
                    .addLore(lore)
                    .build();

            addItem(new ClickableItem(icon, 1, click -> {
                if (!development.getStatus().equals(Server.Status.ONLINE) && !player.hasPermission("lobby.queue.bypasswhitelist")) {
                    player.sendMessage(development.getDisplayName() + ChatColor.RED + " can not be joined at this time");
                    return;
                }

                player.closeInventory();

                development.send(player);
            }));
        }

        if (arena != null) {
            final ServerQueue queue = getLobby().getQueueManager().getQueue(arena);
            final List<String> lore = Lists.newArrayList();

            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");
            lore.add(arena.getDescription());
            lore.add(ChatColor.STRIKETHROUGH + "------------------------------");

            lore.add(ChatColor.GRAY + "Arena allows you and your friends");
            lore.add(ChatColor.GRAY + "to practice our unique combat system");
            lore.add(ChatColor.GRAY + "in a controlled environment with");
            lore.add(ChatColor.GRAY + "1v1s and teamfights.");
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + arena.getStatus().getDisplayName());
            lore.add(ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + arena.getOnlineCount());
            lore.add(ChatColor.GOLD + "Queue" + ChatColor.YELLOW + ": " + queue.getQueue().size());
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GREEN + "Click to join!");

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.DIAMOND_HELMET)
                    .setName(arena.getDisplayName())
                    .addLore(lore)
                    .build();

            addItem(new ClickableItem(icon, 2, click -> {
                final RankService rankService = (RankService)getLobby().getService(RankService.class);
                final Rank rank;

                if (rankService != null) {
                    rank = rankService.getHighestRank(player);
                } else {
                    rank = null;
                }

                queue.add(player.getUniqueId(), rank);

                player.sendMessage("Adding you to the " + arena.getDisplayName() + ChatColor.RESET + " queue...");
                player.sendMessage(ChatColor.AQUA + "You are now " + ChatColor.YELLOW + "#" + queue.getPosition(player.getUniqueId()) + ChatColor.AQUA + " in queue to join " + arena.getDisplayName());
                player.closeInventory();
            }));
        }
    }

    @Override
    public void open() {
        super.open();
        this.updater = new Scheduler(plugin).sync(this::update).repeat(0L, 20L).run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (this.updater != null) {
            this.updater.cancel();
            this.updater = null;
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
