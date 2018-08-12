package com.playares.arena.menu;

import com.playares.arena.Arenas;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.aftermatch.TeamReport;
import com.playares.arena.menu.cont.DuelModeView;
import com.playares.arena.menu.cont.TeamModeView;
import com.playares.arena.menu.cont.TeamView;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public final class MenuHandler {
    @Getter
    public final Arenas plugin;

    public MenuHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void openTeamMenu(Player viewer) {
        final TeamView teamViewer = new TeamView(plugin, viewer, "Teams", 6);
        teamViewer.startUpdater();
        teamViewer.open();
    }

    public void openPlayerReport(Player viewer, PlayerReport report) {
        final Menu menu = new Menu(plugin, viewer, report.getUsername(), 6);

        final ItemStack stats = new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setName(ChatColor.GREEN + "Match Statistics")
                .addLore(Arrays.asList(
                        ChatColor.GOLD + "Health" + ChatColor.YELLOW + ": " + (report.getHealth() / 2),
                        ChatColor.GOLD + "Remaining Health Potions" + ChatColor.YELLOW + ": " + report.getRemainingHealthPotions(),
                        ChatColor.GOLD + "Hits" + ChatColor.YELLOW + ": " + report.getHits(),
                        ChatColor.GOLD + "Damage" + ChatColor.YELLOW + ": " + Math.round(report.getDamage()),
                        ChatColor.GOLD + "Longest Shot" + ChatColor.YELLOW + ": " + Math.round(report.getLongestShot()),
                        ChatColor.GOLD + "Arrows Hit" + ChatColor.YELLOW + ": " + report.getArrowsHit(),
                        ChatColor.GOLD + "Total Arrows" + ChatColor.YELLOW + ": " + report.getArrowsFired(),
                        ChatColor.GOLD + "Bow Accuracy" + ChatColor.YELLOW + ": " + Math.round(report.getAccuracy()) + "%"
                ))
                .build();

        menu.addItem(new ClickableItem(stats, 53, click -> {}));

        for (int i = 0; i < report.getContents().length; i++) {
            final ItemStack item = report.getContents()[i];

            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }

            menu.addItem(new ClickableItem(item, i, click -> {}));
        }

        for (int i = 45; i < report.getArmor().length; i++) {
            final ItemStack item = report.getArmor()[i];

            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }

            menu.addItem(new ClickableItem(item, i, click -> {}));
        }

        menu.open();
    }

    public void openTeamReport(Player viewer, TeamReport report) {
        final Menu menu = new Menu(plugin, viewer, report.getName(), 6);
        int slot = 0;

        final ItemStack stats = new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setName(ChatColor.GREEN + "Team Statistics")
                .addLore(Arrays.asList(
                        ChatColor.GOLD + "Hits" + ChatColor.YELLOW + ": " + report.getHits(),
                        ChatColor.GOLD + "Total Damage" + ChatColor.YELLOW + ": " + Math.round(report.getDamage()),
                        ChatColor.GOLD + "Used Health Potions" + ChatColor.YELLOW + ": " + report.getUsedHealthPotions()
                ))
                .build();

        for (UUID memberId : report.getRoster().keySet()) {
            final String memberName = report.getRoster().get(memberId);
            final ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta meta = (SkullMeta)icon.getItemMeta();

            meta.setDisplayName(ChatColor.AQUA + memberName);
            meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Click to view " + memberName + "'s Inventory"));

            if (Bukkit.getPlayer(memberName) != null) {
                meta.setOwner(memberName);
            }

            icon.setItemMeta(meta);

            menu.addItem(new ClickableItem(icon, slot, click -> {
                viewer.closeInventory();
                viewer.performCommand("am p " + memberId.toString() + " " + report.getMatchId().toString());
            }));

            slot++;
        }

        menu.addItem(new ClickableItem(stats, 49, click -> {}));

        menu.open();
    }

    public void openTeamChallengeMenu(Player viewer, Team team) {
        final TeamModeView teamModeView = new TeamModeView(plugin, viewer, "Select Mode", 3, team);
        int slot = 0;

        for (Mode mode : plugin.getModeManager().getSortedConfiguredModes()) {
            teamModeView.addItem(new ClickableItem(mode.getIcon(), slot, click -> {
                final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(viewer.getUniqueId());

                if (profile.getTeam() == null) {
                    viewer.sendMessage(ChatColor.RED + "You are no longer on a team");
                    return;
                }

                if (!profile.getTeam().getLeader().equals(profile)) {
                    viewer.sendMessage(ChatColor.RED + "You are not the leader of the team");
                    return;
                }

                plugin.getChallengeHandler().sendChallenge(profile.getTeam(), team, mode, new SimplePromise() {
                    @Override
                    public void success() {
                        viewer.sendMessage(ChatColor.GREEN + "Duel request sent");
                        viewer.closeInventory();
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        viewer.sendMessage(ChatColor.RED + reason);
                        viewer.closeInventory();
                    }
                });
            }));

            slot++;
        }

        teamModeView.open();
    }

    public void openDuelChallengeMenu(Player viewer, ArenaPlayer challenged) {
        final DuelModeView duelModeView = new DuelModeView(plugin, viewer, "Select Mode", 3, challenged);
        int slot = 0;

        for (Mode mode : plugin.getModeManager().getSortedConfiguredModes()) {
            duelModeView.addItem(new ClickableItem(mode.getIcon(), slot, click -> {
                final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(viewer.getUniqueId());

                plugin.getChallengeHandler().sendChallenge(profile, challenged, mode, new SimplePromise() {
                    @Override
                    public void success() {
                        viewer.sendMessage(ChatColor.GREEN + "Duel request sent");
                        viewer.closeInventory();
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        viewer.sendMessage(ChatColor.RED + reason);
                        viewer.closeInventory();
                    }
                });
            }));

            slot++;
        }

        duelModeView.open();
    }
}
