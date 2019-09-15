package com.playares.arena.report;

import com.google.common.collect.Lists;
import com.playares.arena.match.Match;
import com.playares.arena.match.TeamMatch;
import com.playares.arena.match.UnrankedMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public final class ReportHandler {
    @Getter public final ReportManager manager;

    public void printReports(Match match) {
        match.getPlayers().forEach(player -> {
            player.getPlayer().sendMessage(" ");
            player.getPlayer().sendMessage(ChatColor.AQUA + "Match Inventories" + ChatColor.GRAY + " (Click name to view)");

            if (match instanceof UnrankedMatch) {
                final UnrankedMatch unrankedMatch = (UnrankedMatch)match;
                final ArenaPlayer winner = unrankedMatch.getWinner();

                if (winner == null) {
                    player.getPlayer().sendMessage(ChatColor.RED + "Failed to generate winner...");
                } else {
                    if (winner.equals(unrankedMatch.getPlayerA())) {
                        player.getPlayer().sendMessage(
                                new ComponentBuilder("Winner: ")
                                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                                        .append(unrankedMatch.getPlayerA().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar " + unrankedMatch.getPlayerA().getActiveReport().getUniqueId().toString()))
                                        .append(" - ")
                                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                                        .append("Loser: ")
                                        .color(net.md_5.bungee.api.ChatColor.RED)
                                        .append(unrankedMatch.getPlayerB().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar " + unrankedMatch.getPlayerB().getActiveReport().getUniqueId().toString()))
                                        .create());
                    } else {
                        player.getPlayer().sendMessage(
                                new ComponentBuilder("Winner: ")
                                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                                        .append(unrankedMatch.getPlayerB().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar " + unrankedMatch.getPlayerB().getActiveReport().getUniqueId().toString()))
                                        .append(" - ")
                                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                                        .append("Loser: ")
                                        .color(net.md_5.bungee.api.ChatColor.RED)
                                        .append(unrankedMatch.getPlayerA().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar " + unrankedMatch.getPlayerA().getActiveReport().getUniqueId().toString()))
                                        .create());
                    }
                }
            }

            else if (match instanceof TeamMatch) {
                final TeamMatch teamMatch = (TeamMatch)match;
                final Team winner = teamMatch.getWinner();

                if (winner == null) {
                    player.getPlayer().sendMessage(ChatColor.RED + "Failed to generate winner...");
                } else {
                    if (winner.equals(teamMatch.getTeamA())) {
                        player.getPlayer().sendMessage(
                                new ComponentBuilder("Winner: ")
                                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                                        .append("Team " + teamMatch.getTeamA().getLeader().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar team " + teamMatch.getTeamA().getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                        .append(" - ")
                                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                                        .append("Loser: ")
                                        .color(net.md_5.bungee.api.ChatColor.RED)
                                        .append("Team " + teamMatch.getTeamB().getLeader().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar team " + teamMatch.getTeamB().getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                        .create());
                    } else {
                        player.getPlayer().sendMessage(
                                new ComponentBuilder("Winner: ")
                                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                                        .append("Team " + teamMatch.getTeamB().getLeader().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar team " + teamMatch.getTeamB().getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                        .append(" - ")
                                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                                        .append("Loser: ")
                                        .color(net.md_5.bungee.api.ChatColor.RED)
                                        .append("Team " + teamMatch.getTeamA().getLeader().getUsername())
                                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ar team " + teamMatch.getTeamA().getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                        .create());
                    }
                }
            }

            player.getPlayer().sendMessage(" ");
        });
    }

    public void openTeamReports(Player player, UUID matchId, Team team) {
        final Menu teamMenu = new Menu(manager.getPlugin(), player, "Team " + team.getLeader().getUsername(), 6);
        int posA = 0;

        for (ArenaPlayer member : team.getMembers()) {
            final int finalPos = posA;
            final PlayerReport report = manager.getReportByPlayer(member, matchId);
            final List<String> lore = Lists.newArrayList();

            if (report == null) {
                continue;
            }

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setName(ChatColor.GOLD + member.getUsername())
                    .addLore(lore)
                    .setData((short)3)
                    .build();

            final SkullMeta meta = (SkullMeta)icon.getItemMeta();

            new Scheduler(getManager().getPlugin()).async(() -> {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUniqueId());

                new Scheduler(getManager().getPlugin()).sync(() -> {
                    meta.setOwningPlayer(offlinePlayer);
                    icon.setItemMeta(meta);

                    teamMenu.addItem(new ClickableItem(icon, finalPos, click -> {
                        player.closeInventory();

                        final Menu menu = new Menu(manager.getPlugin(), player, report.getPlayer().getUsername(), 6);
                        final List<String> statsLore = Lists.newArrayList();
                        final List<String> swordLore = Lists.newArrayList();
                        final List<String> bowLore = Lists.newArrayList();
                        int posB = 0;

                        for (ItemStack content : report.getContents()) {
                            if (content == null) {
                                posB++;
                                continue;
                            }

                            menu.addItem(new ClickableItem(content, posB, null));
                            posB++;
                        }

                        swordLore.add(ChatColor.GOLD + "Sword Hits" + ChatColor.YELLOW + ": " + ChatColor.WHITE + report.getSwordHits());
                        swordLore.add(ChatColor.GOLD + "Total Damage" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", report.getDamage()));

                        bowLore.add(ChatColor.GOLD + "Bow Accuracy" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", report.getBowAccuracy()) + "%");

                        statsLore.add(ChatColor.GOLD + "Health" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", (report.getHealth() / 2)));
                        statsLore.add(ChatColor.GOLD + "Food" + ChatColor.YELLOW + ": " + ChatColor.WHITE + (report.getFood() / 2)+ "/10");
                        statsLore.add(ChatColor.GOLD + "Health Potions" + ChatColor.YELLOW + ": " + ChatColor.WHITE + report.getRemainingHealthPotions());

                        final ItemStack swordIcon = new ItemBuilder()
                                .setMaterial(Material.DIAMOND_SWORD)
                                .setName(ChatColor.GREEN + "Sword Statistics")
                                .addLore(swordLore)
                                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .build();

                        final ItemStack bowIcon = new ItemBuilder()
                                .setMaterial(Material.BOW)
                                .setName(ChatColor.GREEN + "Bow Statistics")
                                .addLore(bowLore)
                                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .build();

                        final ItemStack statsIcon = new ItemBuilder()
                                .setMaterial(Material.SKULL_ITEM)
                                .setData((short)((report.getHealth() <= 0.0) ? 0 : 3))
                                .setName(ChatColor.GREEN + report.getPlayer().getUsername())
                                .addLore(statsLore)
                                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .build();

                        final ItemStack returnIcon = new ItemBuilder()
                                .setMaterial(Material.LEVER)
                                .setName(ChatColor.RED + "Back to Team " + team.getLeader().getUsername())
                                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .build();

                        // 48 sword
                        menu.addItem(new ClickableItem(swordIcon, 48, null));
                        menu.addItem(new ClickableItem(statsIcon, 49, null));
                        menu.addItem(new ClickableItem(bowIcon, 50, null));
                        menu.addItem(new ClickableItem(returnIcon, 53, c2 -> {
                            player.closeInventory();
                            player.performCommand("ar team " + team.getUniqueId().toString() + " " + matchId.toString());
                        }));

                        menu.open();
                    }));
                }).run();
            }).run();

            posA++;
        }

        teamMenu.open();
    }

    public void openReport(Player player, UUID uniqueId, SimplePromise promise) {
        final Report report = manager.getReportById(uniqueId);

        if (report == null) {
            promise.failure("Report not found");
            return;
        }

        if (report instanceof PlayerReport) {
            final PlayerReport playerReport = (PlayerReport)report;
            final Menu menu = new Menu(manager.getPlugin(), player, playerReport.getPlayer().getUsername(), 6);
            final List<String> statsLore = Lists.newArrayList();
            final List<String> swordLore = Lists.newArrayList();
            final List<String> bowLore = Lists.newArrayList();
            int pos = 0;

            for (ItemStack content : playerReport.getContents()) {
                if (content == null) {
                    pos++;
                    continue;
                }

                menu.addItem(new ClickableItem(content, pos, null));
                pos++;
            }

            swordLore.add(ChatColor.GOLD + "Sword Hits" + ChatColor.YELLOW + ": " + ChatColor.WHITE + playerReport.getSwordHits());
            swordLore.add(ChatColor.GOLD + "Total Damage" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", playerReport.getDamage()));

            bowLore.add(ChatColor.GOLD + "Bow Accuracy" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", playerReport.getBowAccuracy()) + "%");

            statsLore.add(ChatColor.GOLD + "Health" + ChatColor.YELLOW + ": " + ChatColor.WHITE + String.format("%.2f", (playerReport.getHealth() / 2)));
            statsLore.add(ChatColor.GOLD + "Food" + ChatColor.YELLOW + ": " + ChatColor.WHITE + (playerReport.getFood() / 2)+ "/10");
            statsLore.add(ChatColor.GOLD + "Health Potions" + ChatColor.YELLOW + ": " + ChatColor.WHITE + playerReport.getRemainingHealthPotions());

            final ItemStack swordIcon = new ItemBuilder()
                    .setMaterial(Material.DIAMOND_SWORD)
                    .setName(ChatColor.GREEN + "Sword Statistics")
                    .addLore(swordLore)
                    .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            final ItemStack bowIcon = new ItemBuilder()
                    .setMaterial(Material.BOW)
                    .setName(ChatColor.GREEN + "Bow Statistics")
                    .addLore(bowLore)
                    .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            final ItemStack statsIcon = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setData((short)((playerReport.getHealth() <= 0.0) ? 0 : 3))
                    .setName(ChatColor.GREEN + playerReport.getPlayer().getUsername())
                    .addLore(statsLore)
                    .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .build();

            // 48 sword
            menu.addItem(new ClickableItem(swordIcon, 48, null));
            menu.addItem(new ClickableItem(statsIcon, 49, null));
            menu.addItem(new ClickableItem(bowIcon, 50, null));

            menu.open();
        }
    }
}
