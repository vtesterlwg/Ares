package com.playares.arena.menu;

import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.duel.TeamDuelRequest;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.List;

public final class TeamMenu extends Menu {
    @Getter public final Arenas plugin;
    @Getter public BukkitTask updater;

    public TeamMenu(@Nonnull Arenas plugin, @Nonnull Player player) {
        super(plugin, player, "Other Teams", 6);
        this.plugin = plugin;
        this.updater = new Scheduler(plugin).sync(this::update).repeat(0L, 3 * 20L).run();
    }

    private void update() {
        if (!isOpen()) {
            return;
        }

        final List<ClickableItem> elements = Lists.newArrayList();
        final Team duelingTeam = plugin.getTeamManager().getTeam(player.getName());
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (duelingTeam == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You are no longer on a team");
            return;
        }

        new Scheduler(plugin).async(() -> {
            for (Team team : plugin.getTeamManager().getAvailableTeams()) {
                if (team.getMembers().contains(profile)) {
                    continue;
                }

                final List<String> lore = Lists.newArrayList();
                final OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(team.getLeader().getUniqueId());

                for (ArenaPlayer member : team.getMembers()) {
                    lore.add(ChatColor.GOLD + member.getUsername());
                }

                new Scheduler(plugin).sync(() -> {
                    final ItemStack icon = new ItemBuilder()
                            .setMaterial(Material.SKULL_ITEM)
                            .setData((short)3)
                            .setName(ChatColor.GOLD + team.getLeader().getUsername() + ChatColor.DARK_AQUA + " (" + ChatColor.YELLOW + team.getMembers().size() + ChatColor.DARK_AQUA + ")")
                            .addLore(lore)
                            .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .build();

                    if (offlineLeader != null) {
                        final SkullMeta skullMeta = (SkullMeta)icon.getItemMeta();
                        skullMeta.setOwningPlayer(offlineLeader);
                        icon.setItemMeta(skullMeta);
                    }

                    elements.add(new ClickableItem(icon, elements.size(), c1 -> {
                        player.closeInventory();

                        final TeamDuelRequest existing = plugin.getDuelManager().getPendingDuelRequest(duelingTeam, team.getLeader().getUsername());

                        if (existing != null) {
                            player.sendMessage(ChatColor.RED + "Please wait a moment before sending another teamfight request to this team");
                            return;
                        }

                        final Menu menu = new Menu(plugin, player, "Duel Team " + team.getLeader().getUsername(), 1);

                        for (MatchmakingQueue queues : plugin.getQueueManager().getMatchmakingQueues()) {
                            menu.addItem(new ClickableItem(queues.getIcon(), queues.getQueueType().getIconPosition(), c2 -> {
                                player.closeInventory();

                                final TeamDuelRequest teamDuelRequest = new TeamDuelRequest(plugin, duelingTeam, team, queues.getQueueType());
                                plugin.getDuelManager().addRequest(teamDuelRequest);

                                duelingTeam.sendMessage(" ");
                                duelingTeam.sendMessage(ChatColor.YELLOW + "Your team has challenged " + ChatColor.AQUA + "Team " + team.getLeader().getUsername() + ChatColor.YELLOW + " to a " + ChatColor.GOLD + queues.getQueueType().getDisplayName() + ChatColor.YELLOW + " teamfight");
                                duelingTeam.sendMessage(ChatColor.GRAY + "Awaiting their response...");
                                duelingTeam.sendMessage(" ");

                                team.getLeader().getPlayer().sendMessage(
                                        new ComponentBuilder("Team " + player.getName())
                                        .color(net.md_5.bungee.api.ChatColor.AQUA)
                                        .append(" has challenged your team to a ")
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .append(queues.getQueueType().getDisplayName())
                                        .color(net.md_5.bungee.api.ChatColor.GOLD)
                                        .append(" teamfight!")
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .append(" [Accept]")
                                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/td accept " + player.getName()))
                                        .create());

                                team.getAvailableMembers().forEach(member -> {
                                    if (!team.isLeader(member)) {
                                        member.getPlayer().sendMessage(ChatColor.AQUA + "Team " + player.getName() + ChatColor.YELLOW + " has challenged your team to a " +
                                                ChatColor.GOLD + queues.getQueueType().getDisplayName() + ChatColor.YELLOW + " teamfight!");
                                    }
                                });
                            }));
                        }

                        menu.open();
                    }));
                }).run();
            }

            new Scheduler(getPlugin()).sync(() -> {
                clearInventory();
                elements.forEach(this::addItem);
            }).run();
        }).run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (updater != null) {
            updater.cancel();
            updater = null;
        }
    }
}